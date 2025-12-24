package com.rifqi.industrialweighbridge.infrastructure

import com.rifqi.industrialweighbridge.engine.CompletedTransaction

/**
 * Interface for printing service.
 */
interface PrinterService {

    /**
     * Prints a weighing ticket for a completed transaction.
     *
     * @param transaction The completed transaction data
     * @param settings Print settings (format, copies, etc.)
     * @return True if print was successful
     */
    suspend fun printTicket(
            transaction: CompletedTransaction,
            settings: PrintSettings = PrintSettings()
    ): PrintResult

    /**
     * Previews a ticket without printing.
     *
     * @param transaction The transaction data
     * @return Path to the preview file (PDF)
     */
    suspend fun generatePreview(transaction: CompletedTransaction): String?

    /** Gets list of available printers. */
    fun getAvailablePrinters(): List<PrinterInfo>

    /** Gets the default printer. */
    fun getDefaultPrinter(): PrinterInfo?
}

/** Print settings configuration. */
data class PrintSettings(
        val printerName: String? = null, // null = use default
        val copies: Int = 1,
        val showPreview: Boolean = true,
        val paperSize: PaperSize = PaperSize.A5
)

/** Paper size options. */
enum class PaperSize {
    A4,
    A5,
    LETTER,
    CUSTOM
}

/** Information about a printer. */
data class PrinterInfo(val name: String, val isDefault: Boolean, val isAvailable: Boolean)

/** Result of a print operation. */
sealed class PrintResult {
    data object Success : PrintResult()
    data class Failure(val error: String) : PrintResult()
    data object Cancelled : PrintResult()
}
