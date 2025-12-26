package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.engine.AuthState
import com.rifqi.industrialweighbridge.engine.AuthenticationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI State for Login Screen */
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false
)

/**
 * ViewModel for Login Screen. Handles user input and delegates authentication to
 * AuthenticationManager.
 */
class LoginViewModel(private val authenticationManager: AuthenticationManager) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Expose auth state from AuthenticationManager
    val authState: StateFlow<AuthState> = authenticationManager.authState

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun login() {
        val currentState = _uiState.value

        // Validate input
        if (currentState.username.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Username tidak boleh kosong")
            return
        }
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Password tidak boleh kosong")
            return
        }

        scope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            val success =
                authenticationManager.login(
                    username = currentState.username,
                    password = currentState.password
                )

            if (!success) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Username atau password salah"
                    )
            } else {
                // Reset form on success
                _uiState.value = LoginUiState()
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        authenticationManager.clearError()
    }
}
