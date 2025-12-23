package com.rifqi.industrialweighbridge.infrastructure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * In-memory implementation of AuditLogger.
 *
 * For production, this could be replaced with a database-backed implementation. Currently stores
 * audit entries in memory for simplicity.
 */
class InMemoryAuditLogger : AuditLogger {

    private val entries = mutableListOf<AuditEntry>()
    private var nextId: Long = 1

    override suspend fun log(
            action: AuditAction,
            description: String,
            metadata: Map<String, String>
    ) =
            withContext(Dispatchers.Default) {
                val entry =
                        AuditEntry(
                                id = nextId++,
                                timestamp = Clock.System.now(),
                                action = action,
                                description = description,
                                metadata = metadata
                        )
                entries.add(entry)

                // Also log to console for debugging
                println("[AUDIT] ${entry.timestamp}: ${entry.action} - ${entry.description}")
            }

    override suspend fun getEntries(
            from: Instant,
            to: Instant,
            actionFilter: AuditAction?
    ): List<AuditEntry> =
            withContext(Dispatchers.Default) {
                entries.filter { entry ->
                    entry.timestamp >= from &&
                            entry.timestamp <= to &&
                            (actionFilter == null || entry.action == actionFilter)
                }
            }

    override suspend fun getAllEntries(): List<AuditEntry> =
            withContext(Dispatchers.Default) { entries.toList() }
}
