package com.rifqi.industrialweighbridge.engine

import com.rifqi.industrialweighbridge.db.UserRole
import com.rifqi.industrialweighbridge.domain.model.User
import com.rifqi.industrialweighbridge.domain.repository.AuthRepository
import com.rifqi.industrialweighbridge.infrastructure.AuditAction
import com.rifqi.industrialweighbridge.infrastructure.AuditLogger
import com.rifqi.industrialweighbridge.infrastructure.EntityType
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Authentication state sealed class. */
sealed class AuthState {
    object NotAuthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Authentication Manager - Manages user authentication state and session persistence.
 *
 * Responsibilities:
 * - Handle login/logout flow
 * - Persist session using multiplatform-settings
 * - Provide authentication state to the app
 * - Auto-create default admin user on first run
 * - Log authentication events for audit trail
 */
class AuthenticationManager(
        private val authRepository: AuthRepository,
        private val auditLogger: AuditLogger
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val settings: Settings = Settings()

    // Session keys
    private companion object {
        const val KEY_USER_ID = "auth_user_id"
        const val KEY_IS_LOGGED_IN = "auth_is_logged_in"
        const val DEFAULT_ADMIN_USERNAME = "admin"
        const val DEFAULT_ADMIN_PASSWORD = "admin123"
    }

    // Authentication state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Convenience properties
    val currentUser: User?
        get() = (_authState.value as? AuthState.Authenticated)?.user
    val isLoggedIn: Boolean
        get() = _authState.value is AuthState.Authenticated
    val isAdmin: Boolean
        get() = currentUser?.isAdmin == true

    /**
     * Initialize authentication - restore session or show login. Should be called when app starts.
     */
    fun initialize() {
        scope.launch {
            try {
                // Ensure default admin exists
                ensureDefaultAdmin()

                // Try to restore session
                val savedUserId = settings.getLongOrNull(KEY_USER_ID)
                val isLoggedIn = settings.getBoolean(KEY_IS_LOGGED_IN, false)

                if (isLoggedIn && savedUserId != null) {
                    val user = authRepository.getUserById(savedUserId)
                    if (user != null) {
                        _authState.value = AuthState.Authenticated(user)
                        return@launch
                    }
                }

                // No valid session, require login
                _authState.value = AuthState.NotAuthenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to initialize: ${e.message}")
            }
        }
    }

    /** Login with username and password. */
    suspend fun login(username: String, password: String): Boolean {
        _authState.value = AuthState.Loading

        return try {
            val user = authRepository.authenticate(username, password)
            if (user != null) {
                // Save session
                settings.putLong(KEY_USER_ID, user.id)
                settings.putBoolean(KEY_IS_LOGGED_IN, true)

                _authState.value = AuthState.Authenticated(user)

                // Log successful login
                println("[DEBUG] About to log LOGIN action for user: ${user.username}")
                auditLogger.log(
                        action = AuditAction.LOGIN,
                        username = user.username,
                        description = "User '${user.username}' logged in successfully",
                        entityType = EntityType.USER.name,
                        entityId = user.id.toString()
                )
                println("[DEBUG] LOGIN action logged successfully")

                true
            } else {
                _authState.value = AuthState.Error("Username atau password salah")

                // Log failed login attempt
                auditLogger.log(
                        action = AuditAction.LOGIN_FAILED,
                        username = username,
                        description = "Failed login attempt for username '$username'"
                )

                false
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Login gagal: ${e.message}")

            // Log failed login with error
            auditLogger.log(
                    action = AuditAction.LOGIN_FAILED,
                    username = username,
                    description = "Login error for '$username': ${e.message}"
            )

            false
        }
    }

    /** Logout current user. */
    fun logout() {
        val logoutUser = currentUser

        // Clear session
        settings.remove(KEY_USER_ID)
        settings.putBoolean(KEY_IS_LOGGED_IN, false)

        _authState.value = AuthState.NotAuthenticated

        // Log logout event
        logoutUser?.let { user ->
            scope.launch {
                auditLogger.log(
                        action = AuditAction.LOGOUT,
                        username = user.username,
                        description = "User '${user.username}' logged out",
                        entityType = EntityType.USER.name,
                        entityId = user.id.toString()
                )
            }
        }
    }

    /** Clear error state. */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.NotAuthenticated
        }
    }

    /** Ensures default admin user exists on first run. */
    private suspend fun ensureDefaultAdmin() {
        if (!authRepository.userExists(DEFAULT_ADMIN_USERNAME)) {
            authRepository.createUser(
                    username = DEFAULT_ADMIN_USERNAME,
                    password = DEFAULT_ADMIN_PASSWORD,
                    role = UserRole.ADMIN
            )
        }
    }
}
