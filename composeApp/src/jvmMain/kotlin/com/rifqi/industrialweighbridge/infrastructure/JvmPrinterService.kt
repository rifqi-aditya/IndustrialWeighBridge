package com.rifqi.industrialweighbridge.infrastructure

import com.rifqi.industrialweighbridge.domain.model.CompanySettings
import com.rifqi.industrialweighbridge.domain.model.PrintSettings as DomainPrintSettings
import com.rifqi.industrialweighbridge.engine.CompletedTransaction
import java.awt.Desktop
import java.io.File
import javax.print.PrintServiceLookup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JVM implementation of PrinterService.
 *
 * For now, this is a simplified implementation that will be enhanced when full printing integration
 * is needed.
 */
class JvmPrinterService(
        private val companySettings: () -> CompanySettings,
        private val domainPrintSettings: () -> DomainPrintSettings
) : com.rifqi.industrialweighbridge.infrastructure.PrinterService {

    override suspend fun printTicket(
            transaction: CompletedTransaction,
            settings: PrintSettings
    ): PrintResult =
            withContext(Dispatchers.IO) {
                try {
                    // Generate a simple text-based ticket for now
                    // Full PDF generation will use TicketPdfGenerator later
                    val ticketContent = buildTicketContent(transaction)

                    val outputDir = File(System.getProperty("user.home"), "WeighBridge/tickets")
                    if (!outputDir.exists()) {
                        outputDir.mkdirs()
                    }

                    val ticketFile = File(outputDir, "ticket_${transaction.ticketNumber}.txt")
                    ticketFile.writeText(ticketContent)

                    // Open file for preview
                    if (settings.showPreview && Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(ticketFile)
                    }

                    println("Ticket generated: ${ticketFile.absolutePath}")
                    PrintResult.Success
                } catch (e: Exception) {
                    PrintResult.Failure(e.message ?: "Unknown error during printing")
                }
            }

    override suspend fun generatePreview(transaction: CompletedTransaction): String? =
            withContext(Dispatchers.IO) {
                try {
                    val ticketContent = buildTicketContent(transaction)

                    val outputDir = File(System.getProperty("user.home"), "WeighBridge/tickets")
                    if (!outputDir.exists()) {
                        outputDir.mkdirs()
                    }

                    val ticketFile = File(outputDir, "preview_${transaction.ticketNumber}.txt")
                    ticketFile.writeText(ticketContent)
                    ticketFile.absolutePath
                } catch (e: Exception) {
                    null
                }
            }

    override fun getAvailablePrinters(): List<PrinterInfo> {
        return PrintServiceLookup.lookupPrintServices(null, null).map { service ->
            PrinterInfo(
                    name = service.name,
                    isDefault = service == PrintServiceLookup.lookupDefaultPrintService(),
                    isAvailable = true
            )
        }
    }

    override fun getDefaultPrinter(): PrinterInfo? {
        val defaultService = PrintServiceLookup.lookupDefaultPrintService()
        return defaultService?.let {
            PrinterInfo(name = it.name, isDefault = true, isAvailable = true)
        }
    }

    /**
     * Builds a simple text-based ticket content. This will be replaced with PDF generation later.
     */
    private fun buildTicketContent(transaction: CompletedTransaction): String {
        val company = companySettings()

        return buildString {
            appendLine("=".repeat(50))
            appendLine(company.companyName.ifEmpty { "Industrial WeighBridge" })
            if (company.companyAddress.isNotEmpty()) appendLine(company.companyAddress)
            if (company.companyPhone.isNotEmpty()) appendLine("Tel: ${company.companyPhone}")
            appendLine("=".repeat(50))
            appendLine()
            appendLine("WEIGHING TICKET")
            appendLine("-".repeat(50))
            appendLine("Ticket No    : ${transaction.ticketNumber}")
            appendLine("Transaction  : ${transaction.transactionType}")
            appendLine("-".repeat(50))
            appendLine("Gross Weight : ${String.format("%,.2f", transaction.grossWeight)} kg")
            appendLine("Tare Weight  : ${String.format("%,.2f", transaction.tareWeight)} kg")
            appendLine("=".repeat(50))
            appendLine("NET WEIGHT   : ${String.format("%,.2f", transaction.netWeight)} kg")
            appendLine("=".repeat(50))
            appendLine()
            if (transaction.isManualEntry) {
                appendLine("* Manual Entry *")
            }
            appendLine()
            appendLine("Thank you for using our service")
        }
    }
}
