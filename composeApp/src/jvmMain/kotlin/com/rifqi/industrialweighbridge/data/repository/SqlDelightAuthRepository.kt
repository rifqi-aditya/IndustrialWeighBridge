package com.rifqi.industrialweighbridge.data.repository

import com.rifqi.industrialweighbridge.db.UserRole
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.domain.model.User
import com.rifqi.industrialweighbridge.domain.repository.AuthRepository
import org.mindrot.jbcrypt.BCrypt

/** SQLDelight implementation of AuthRepository. Uses BCrypt for secure password hashing. */
class SqlDelightAuthRepository(private val db: WeighbridgeDatabase) : AuthRepository {

    private val queries = db.weighbridgeQueries

    override suspend fun authenticate(username: String, password: String): User? {
        val dbUser = queries.selectUserByUsername(username).executeAsOneOrNull() ?: return null

        // Verify password using BCrypt
        return if (BCrypt.checkpw(password, dbUser.password_hash)) {
            User(id = dbUser.id, username = dbUser.username, role = dbUser.role)
        } else {
            null
        }
    }

    override suspend fun createUser(username: String, password: String, role: UserRole): Boolean {
        return try {
            // Check if user already exists
            if (userExists(username)) {
                return false
            }

            // Hash password using BCrypt (cost factor 12 for good security)
            val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12))

            queries.insertUser(username = username, password_hash = hashedPassword, role = role)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun userExists(username: String): Boolean {
        return queries.selectUserByUsername(username).executeAsOneOrNull() != null
    }

    override suspend fun getUserById(id: Long): User? {
        val dbUser = queries.selectUserById(id).executeAsOneOrNull() ?: return null
        return User(id = dbUser.id, username = dbUser.username, role = dbUser.role)
    }

    // === User Management ===

    override suspend fun getAllUsers(): List<User> {
        return queries.selectAllUsers().executeAsList().map { dbUser ->
            User(id = dbUser.id, username = dbUser.username, role = dbUser.role)
        }
    }

    override suspend fun updateUserPassword(userId: Long, newPassword: String): Boolean {
        return try {
            val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12))
            queries.updateUserPassword(password_hash = hashedPassword, id = userId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateUserRole(userId: Long, newRole: UserRole): Boolean {
        return try {
            queries.updateUserRole(role = newRole, id = userId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteUser(userId: Long): Boolean {
        return try {
            queries.deleteUser(id = userId)
            true
        } catch (e: Exception) {
            false
        }
    }
}
