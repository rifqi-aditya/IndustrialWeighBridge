package com.rifqi.industrialweighbridge.infrastructure

/**
 * Interface for audit logging.
 *
 * Logs all user actions for troubleshooting, debugging, and audit purposes
 */
interface AuditLogger {

    /**
     * Logs an action event.
     *
     * @param action The type of action being logged
     * @param username The user who performed the action
     * @param description Human-readable description of the event
     * @param entityType The type of entity involved (e.g., TRANSACTION, VEHICLE)
     * @param entityId The identifier of the entity involved
     * @param details Additional details as JSON string
     */
    suspend fun log(
        action: AuditAction,
        username: String,
        description: String,
        entityType: String? = null,
        entityId: String? = null,
        details: String? = null
    )

    /** Gets all audit log entries (newest first). */
    suspend fun getAllEntries(): List<AuditEntry>

    /** Gets audit log entries within a date range (ISO 8601 format). */
    suspend fun getEntriesByDateRange(from: String, to: String): List<AuditEntry>

    /** Gets audit log entries by action type. */
    suspend fun getEntriesByAction(action: AuditAction): List<AuditEntry>

    /** Gets audit log entries by username. */
    suspend fun getEntriesByUsername(username: String): List<AuditEntry>

    /** Gets recent entries with limit. */
    suspend fun getRecentEntries(limit: Int): List<AuditEntry>

    /** Deletes logs older than specified date (ISO 8601 format, for retention policy). */
    suspend fun deleteLogsOlderThan(date: String)

    /** Counts total log entries. */
    suspend fun countEntries(): Long
}

/** Types of audit actions - Standard Level. Covers Authentication + CRUD operations. */
enum class AuditAction {
    // === Authentication ===
    /** User successfully logged in */
    LOGIN,

    /** User logged out */
    LOGOUT,

    /** Login attempt failed */
    LOGIN_FAILED,

    // === Transactions (Core Business) ===
    /** Weigh-in transaction completed */
    WEIGH_IN_COMPLETED,

    /** Weigh-out transaction completed */
    WEIGH_OUT_COMPLETED,

    /** Transaction was cancelled */
    TRANSACTION_CANCELLED,

    /** Manual mode was enabled */
    MANUAL_MODE_ENABLED,

    /** Manual mode was disabled */
    MANUAL_MODE_DISABLED,

    // === Master Data - Vehicle ===
    /** Vehicle created */
    VEHICLE_CREATED,

    /** Vehicle updated */
    VEHICLE_UPDATED,

    /** Vehicle deleted */
    VEHICLE_DELETED,

    // === Master Data - Driver ===
    /** Driver created */
    DRIVER_CREATED,

    /** Driver updated */
    DRIVER_UPDATED,

    /** Driver deleted */
    DRIVER_DELETED,

    // === Master Data - Product ===
    /** Product created */
    PRODUCT_CREATED,

    /** Product updated */
    PRODUCT_UPDATED,

    /** Product deleted */
    PRODUCT_DELETED,

    // === Master Data - Partner ===
    /** Partner (Supplier/Customer) created */
    PARTNER_CREATED,

    /** Partner updated */
    PARTNER_UPDATED,

    /** Partner deleted */
    PARTNER_DELETED,

    // === User Management ===
    /** New user created by admin */
    USER_CREATED,

    /** User deleted by admin */
    USER_DELETED,

    // === Settings ===
    /** Application settings changed */
    SETTINGS_CHANGED
}

/** Entity types for categorizing audit log entries. */
enum class EntityType {
    USER,
    VEHICLE,
    DRIVER,
    PRODUCT,
    PARTNER,
    TRANSACTION,
    SETTINGS
}

/** An audit log entry. */
data class AuditEntry(
    val id: Long,
    /** Timestamp in ISO 8601 format (e.g., 2025-12-26T15:30:00Z) */
    val timestamp: String,
    val action: AuditAction,
    val username: String,
    val entityType: String?,
    val entityId: String?,
    val description: String,
    val details: String?
)
