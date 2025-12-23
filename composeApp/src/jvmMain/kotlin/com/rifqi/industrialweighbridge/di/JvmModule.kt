package com.rifqi.industrialweighbridge.di

import com.rifqi.industrialweighbridge.infrastructure.JvmPrinterService
import com.rifqi.industrialweighbridge.infrastructure.JvmSerialCommunicationHandler
import com.rifqi.industrialweighbridge.infrastructure.PrinterService
import com.rifqi.industrialweighbridge.infrastructure.SerialCommunicationHandler
import org.koin.dsl.module

/**
 * JVM-specific Koin module.
 *
 * Contains platform-specific implementations that require JVM APIs (Serial communication, Printing,
 * etc.)
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
}
