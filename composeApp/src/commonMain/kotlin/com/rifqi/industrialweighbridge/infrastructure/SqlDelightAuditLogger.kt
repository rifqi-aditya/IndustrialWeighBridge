package com.rifqi.industrialweighbridge.infrastructure

import com.rifqi.industrialweighbridge.auditlog.ActionLog
import com.rifqi.industrialweighbridge.auditlog.AuditLogDatabase
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SQLite-backed implementation of AuditLogger.
 *
 * Uses a separate database (audit_log.db) to avoid impacting main database performance. Provides
 * persistence so logs survive app restarts.
 */
class SqlDelightAuditLogger(private val database: AuditLogDatabase) : AuditLogger {

    private val queries = database.auditLogQueries

    override suspend fun log(
        action: AuditAction,
        username: String,
        description: String,
        entityType: String?,
        entityId: String?,
        details: String?
    ) =
        withContext(Dispatchers.IO) {
            // Use java.time.Instant for timestamp (built-in JVM, no external
            // dependency)
            val timestamp = Instant.now().toString()

            queries.insertLog(
                timestamp = timestamp,
                action = action.name,
                username = username,
                entity_type = entityType,
                entity_id = entityId,
                description = description,
                details = details
            )

            // Also log to console for debugging
            println("[AUDIT] $timestamp: $action by $username - $description")
        }

    override suspend fun getAllEntries(): List<AuditEntry> =
        withContext(Dispatchers.IO) {
            queries.selectAllLogs().executeAsList().map { it.toAuditEntry() }
        }

    override suspend fun getEntriesByDateRange(from: String, to: String): List<AuditEntry> =
        withContext(Dispatchers.IO) {
            queries.selectLogsByDateRange(from, to).executeAsList().map {
                it.toAuditEntry()
            }
        }

    override suspend fun getEntriesByAction(action: AuditAction): List<AuditEntry> =
        withContext(Dispatchers.IO) {
            queries.selectLogsByAction(action.name).executeAsList().map {
                it.toAuditEntry()
            }
        }

    override suspend fun getEntriesByUsername(username: String): List<AuditEntry> =
        withContext(Dispatchers.IO) {
            queries.selectLogsByUsername(username).executeAsList().map {
                it.toAuditEntry()
            }
        }

    override suspend fun getRecentEntries(limit: Int): List<AuditEntry> =
        withContext(Dispatchers.IO) {
            queries.selectRecentLogs(limit.toLong()).executeAsList().map {
                it.toAuditEntry()
            }
        }

    override suspend fun deleteLogsOlderThan(date: String) =
        withContext(Dispatchers.IO) { queries.deleteLogsOlderThan(date) }

    override suspend fun countEntries(): Long =
        withContext(Dispatchers.IO) { queries.countLogs().executeAsOne() }

    // Extension function to convert generated ActionLog to domain AuditEntry
    private fun ActionLog.toAuditEntry(): AuditEntry =
        AuditEntry(
            id = this.id,
            timestamp = this.timestamp,
            action = AuditAction.valueOf(this.action),
            username = this.username,
            entityType = this.entity_type,
            entityId = this.entity_id,
            description = this.description,
            details = this.details
        )
}
