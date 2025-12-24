package com.rifqi.industrialweighbridge.domain.model

import com.rifqi.industrialweighbridge.db.UserRole

/**
 * Domain model representing an authenticated user. This is a clean domain object without database
 * dependencies.
 */
data class User(val id: Long, val username: String, val role: UserRole) {
    val isAdmin: Boolean
        get() = role == UserRole.ADMIN
    val isOperator: Boolean
        get() = role == UserRole.OPERATOR
}
