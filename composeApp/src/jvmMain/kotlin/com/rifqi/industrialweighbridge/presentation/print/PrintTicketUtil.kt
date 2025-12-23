package com.rifqi.industrialweighbridge.presentation.print

import com.rifqi.industrialweighbridge.data.repository.SettingsRepository
import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.domain.model.CompanySettings
import com.rifqi.industrialweighbridge.domain.model.PrintSettings
import java.awt.Desktop
import java.io.File

/** Utility object for printing tickets */
object PrintTicketUtil {

    /** Generates and opens a PDF ticket for printing */
    fun printTicket(
            transaction: SelectAllTransactions,
            companySettings: CompanySettings,
            printSettings: PrintSettings
    ): Result<File> {
        return TicketPdfGenerator.generateTicket(transaction, companySettings, printSettings)
                .onSuccess { file ->
                    // Open PDF with default system viewer
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file)
                    }
                }
    }

    /**
     * Generates and opens a PDF ticket using settings from repository This is a convenience method
     * that loads settings automatically
     */
    fun printTicket(
            transaction: SelectAllTransactions,
            settingsRepository: SettingsRepository
    ): Result<File> {
        val companySettings = settingsRepository.loadCompanySettings()
        val printSettings = settingsRepository.loadPrintSettings()
        return printTicket(transaction, companySettings, printSettings)
    }
}
