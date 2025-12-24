package com.rifqi.industrialweighbridge.domain.repository

import com.rifqi.industrialweighbridge.db.UserRole
import com.rifqi.industrialweighbridge.domain.model.User

/**
 * Repository interface for authentication operations. Follows the repository pattern for clean
 * architecture.
 */
interface AuthRepository {
    /**
     * Authenticates a user with username and password.
     * @return User if credentials are valid, null otherwise
     */
    suspend fun authenticate(username: String, password: String): User?

    /**
     * Creates a new user account.
     * @return true if successful, false if username already exists
     */
    suspend fun createUser(username: String, password: String, role: UserRole): Boolean

    /** Checks if a user with the given username exists. */
    suspend fun userExists(username: String): Boolean

    /** Gets a user by their ID. */
    suspend fun getUserById(id: Long): User?
}
