package com.rifqi.industrialweighbridge.infrastructure

import kotlinx.datetime.Instant

/**
 * Interface for audit logging.
 *
 * According to HLD Section 4.3: "Core Weighing Engine mencatat aktivitas ini ke Audit Log" (for
 * manual mode)
 *
 * And Section 4.4: "Aktivitas kesalahan dicatat ke dalam Audit Log jika bersifat kritis."
 */
interface AuditLogger {

    /**
     * Logs an audit event.
     *
     * @param action The type of action being logged
     * @param description Detailed description of the event
     * @param metadata Additional key-value pairs for context
     */
    suspend fun log(
            action: AuditAction,
            description: String,
            metadata: Map<String, String> = emptyMap()
    )

    /** Gets audit log entries within a date range. */
    suspend fun getEntries(
            from: Instant,
            to: Instant,
            actionFilter: AuditAction? = null
    ): List<AuditEntry>

    /** Gets all audit log entries. */
    suspend fun getAllEntries(): List<AuditEntry>
}

/** Types of audit actions. */
enum class AuditAction {
    /** Manual mode was activated */
    MANUAL_MODE_ACTIVATED,

    /** Manual mode was deactivated */
    MANUAL_MODE_DEACTIVATED,

    /** Manual weight entry was made */
    MANUAL_WEIGHT_ENTRY,

    /** Weight-in transaction completed */
    WEIGH_IN_COMPLETED,

    /** Weight-out transaction completed */
    WEIGH_OUT_COMPLETED,

    /** Transaction was cancelled */
    TRANSACTION_CANCELLED,

    /** Device connection established */
    DEVICE_CONNECTED,

    /** Device connection lost */
    DEVICE_DISCONNECTED,

    /** Critical error occurred */
    CRITICAL_ERROR,

    /** Settings were changed */
    SETTINGS_CHANGED,

    /** User logged in */
    USER_LOGIN,

    /** User logged out */
    USER_LOGOUT
}

/** An audit log entry. */
data class AuditEntry(
        val id: Long,
        val timestamp: Instant,
        val action: AuditAction,
        val description: String,
        val metadata: Map<String, String>
)
