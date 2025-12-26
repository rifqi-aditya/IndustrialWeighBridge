package com.rifqi.industrialweighbridge.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rifqi.industrialweighbridge.auditlog.AuditLogDatabase
import com.rifqi.industrialweighbridge.data.repository.SqlDelightAuthRepository
import com.rifqi.industrialweighbridge.domain.repository.AuthRepository
import com.rifqi.industrialweighbridge.infrastructure.AuditLogger
import com.rifqi.industrialweighbridge.infrastructure.JvmPrinterService
import com.rifqi.industrialweighbridge.infrastructure.JvmSerialCommunicationHandler
import com.rifqi.industrialweighbridge.infrastructure.PrinterService
import com.rifqi.industrialweighbridge.infrastructure.SerialCommunicationHandler
import com.rifqi.industrialweighbridge.infrastructure.SqlDelightAuditLogger
import java.io.File
import org.koin.dsl.module

/**
 * JVM-specific Koin module.
 *
 * Contains platform-specific implementations that require JVM APIs (Serial communication, Printing,
 * BCrypt, Database drivers, etc.)
 */
val jvmModule = module {

    // === Infrastructure Layer - JVM Implementations ===

    // Serial Communication Handler (for RS-232/USB weighing scale)
    single<SerialCommunicationHandler> { JvmSerialCommunicationHandler() }

    // Printer Service (for ticket printing)
    single<PrinterService> {
        JvmPrinterService(
            companySettings = {
                get<com.rifqi.industrialweighbridge.data.repository.SettingsRepository>()
                    .loadCompanySettings()
            },
            domainPrintSettings = {
                get<com.rifqi.industrialweighbridge.data.repository.SettingsRepository>()
                    .loadPrintSettings()
            }
        )
    }

    // === Audit Log Database (Separate from main DB) ===

    single<AuditLogDatabase> {
        // Create separate database file for audit logs
        val dbPath = File(System.getProperty("user.dir"), "audit_log.db").absolutePath
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")

        // Create tables if they don't exist
        try {
            AuditLogDatabase.Schema.create(driver)
        } catch (e: Exception) {
            // Tables already exist, ignore
        }

        AuditLogDatabase(driver)
    }

    // Audit Logger - SQLite backed (persistent) with 90-day retention
    single<AuditLogger> {
        val logger = SqlDelightAuditLogger(database = get())

        // Run 90-day retention cleanup on startup
        kotlinx.coroutines.runBlocking {
            val retentionDays = 90L
            val cutoffDate =
                java.time.Instant.now()
                    .minus(java.time.Duration.ofDays(retentionDays))
                    .toString()

            try {
                logger.deleteLogsOlderThan(cutoffDate)
                println("[AUDIT] Retention cleanup completed - removed logs older than 90 days")
            } catch (e: Exception) {
                println("[AUDIT] Retention cleanup error: ${e.message}")
            }
        }

        logger
    }

    // === Authentication - JVM Implementation (requires BCrypt) ===

    single<AuthRepository> { SqlDelightAuthRepository(db = get()) }
}
