package com.rifqi.industrialweighbridge.presentation.print

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.domain.model.CompanySettings
import com.rifqi.industrialweighbridge.domain.model.PaperSize
import com.rifqi.industrialweighbridge.domain.model.PrintSettings
import com.rifqi.industrialweighbridge.domain.utils.DateTimeUtils
import com.rifqi.industrialweighbridge.engine.TransactionType
import com.rifqi.industrialweighbridge.presentation.utils.WeightFormatter
import java.io.File
import java.io.FileOutputStream

/**
 * Generates PDF tickets using HTML template - MUCH EASIER TO MODIFY!
 *
 * Just edit the HTML/CSS below to change the ticket layout.
 */
object TicketPdfGenerator {

    /** Generates a PDF ticket for a transaction */
    fun generateTicket(
            transaction: SelectAllTransactions,
            companySettings: CompanySettings,
            printSettings: PrintSettings,
            outputDir: File = File(System.getProperty("user.home"), "WeighBridge/tickets")
    ): Result<File> {
        return try {
            outputDir.mkdirs()
            val outputFile = File(outputDir, "${transaction.ticket_number}.pdf")

            // Generate HTML from template
            val html = buildTicketHtml(transaction, companySettings, printSettings)

            // Convert HTML to PDF
            FileOutputStream(outputFile).use { os ->
                PdfRendererBuilder().useFastMode().withHtmlContent(html, null).toStream(os).run()
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * HTML Template - Dot Matrix Printer Compatible (Black & White)
     *
     * Designed for industrial weighbridge use with standard dot matrix printers.
     */
    private fun buildTicketHtml(
            transaction: SelectAllTransactions,
            company: CompanySettings,
            settings: PrintSettings
    ): String {
        val paperWidth =
                when (settings.paperSize) {
                    PaperSize.A4 -> "210mm"
                    PaperSize.A5 -> "148mm"
                    PaperSize.THERMAL_80MM -> "80mm"
                    PaperSize.THERMAL_58MM -> "58mm"
                }

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <style>
        @page {
            size: $paperWidth auto;
            margin: 10mm;
        }
        body {
            font-family: "Courier New", Courier, monospace;
            font-size: 12px;
            margin: 0;
            padding: 0;
            color: #000;
        }
        .header {
            border-bottom: 1px solid #000;
            padding-bottom: 10px;
            margin-bottom: 15px;
        }
        .header-table {
            width: 100%;
        }
        .header-logo {
            width: 80px;
            vertical-align: middle;
        }
        .logo-placeholder {
            width: 60px;
            height: 40px;
            border: 1px dashed #000;
            text-align: center;
            line-height: 40px;
            font-size: 10px;
        }
        .header-info {
            text-align: center;
            vertical-align: middle;
        }
        .company-name {
            font-size: 16px;
            margin-bottom: 3px;
        }
        .company-address {
            font-size: 10px;
        }
        .ticket-number {
            font-size: 12px;
            text-align: right;
            margin-bottom: 15px;
        }
        .manual-badge {
            border: 1px solid #000;
            padding: 1px 5px;
            font-size: 9px;
        }
        .info-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 10px;
        }
        .info-table td {
            padding: 3px 0;
            vertical-align: top;
        }
        .info-table .label {
            width: 100px;
        }
        .separator {
            border-top: 1px solid #000;
            margin: 15px 0;
        }
        .signature-container {
            width: 100%;
            margin-top: 30px;
        }
        .signature-row {
            width: 100%;
        }
        .signature-cell {
            width: 50%;
            text-align: center;
            vertical-align: top;
            padding: 0 10px;
        }
        .signature-title {
            margin-bottom: 40px;
        }
        .signature-name {
            margin-top: 5px;
        }
        .weight-separator-cell {
            border-top: 1px solid #000;
            padding-top: 3px;
        }
        .keterangan-section {
            margin-top: 10px;
        }
        .keterangan-label {
            margin-bottom: 5px;
        }
        .keterangan-space {
            min-height: 30px;
        }
        .footer {
            margin-top: 15px;
            font-size: 9px;
            text-align: center;
            border-top: 1px solid #000;
            padding-top: 8px;
        }
    </style>
</head>
<body>
    <!-- HEADER WITH LOGO ON LEFT -->
    <div class="header">
        <table class="header-table">
            <tr>
                ${if (settings.showLogo) """<td class="header-logo"><div class="logo-placeholder">[LOGO]</div></td>""" else ""}
                <td class="header-info">
                    <div class="company-name">${company.companyName.ifBlank { "WEIGHBRIDGE" }}</div>
                    <div class="company-address">${company.companyAddress}</div>
                    ${if (company.companyPhone.isNotBlank() || company.companyFax.isNotBlank()) """
                    <div class="company-address">
                        ${if (company.companyPhone.isNotBlank()) "Telp: ${company.companyPhone}" else ""}
                        ${if (company.companyPhone.isNotBlank() && company.companyFax.isNotBlank()) " | " else ""}
                        ${if (company.companyFax.isNotBlank()) "Fax: ${company.companyFax}" else ""}
                    </div>
                    """ else ""}
                </td>
            </tr>
        </table>
    </div>

    <!-- TICKET NUMBER -->
    <div class="ticket-number">
        No Tiket: ${transaction.ticket_number}
        ${if (transaction.is_manual == 1L) """<span class="manual-badge">Manual</span>""" else ""}
    </div>

    <!-- TRANSACTION INFO -->
    <table class="info-table">
        <tr>
            <td class="label">${if (transaction.transaction_type == TransactionType.INBOUND) "Supplier" else "Customer"}</td>
            <td>: ${transaction.partner_name ?: "-"}</td>
        </tr>
        <tr>
            <td class="label">Sopir</td>
            <td>: ${transaction.driver_name ?: "-"}</td>
        </tr>
        <tr>
            <td class="label">Barang</td>
            <td>: ${transaction.product_name ?: "-"}</td>
        </tr>
        <tr>
            <td class="label">No Polisi</td>
            <td>: ${transaction.plate_number ?: "-"}</td>
        </tr>
        <tr>
            <td class="label">No PO/DO</td>
            <td>: -</td>
        </tr>
    </table>

    <!-- TIMESTAMPS -->
    <table class="info-table">
        <tr>
            <td class="label">Waktu In</td>
            <td>: ${DateTimeUtils.formatForDisplay(transaction.weigh_in_timestamp)}</td>
        </tr>
        <tr>
            <td class="label">Waktu Out</td>
            <td>: ${transaction.weigh_out_timestamp?.let { DateTimeUtils.formatForDisplay(it) } ?: "-"}</td>
        </tr>
    </table>

    <!-- WEIGHT SECTION (with separator matching value width) -->
    <table class="info-table">
        <tr>
            <td class="label">Gross</td>
            <td>: ${WeightFormatter.formatWeight(transaction.weigh_in_weight)}</td>
        </tr>
        <tr>
            <td class="label">Tare</td>
            <td style="padding-bottom: 5px;">: <span style="display: inline-block; border-bottom: 1px solid #000; padding-bottom: 3px;">${transaction.weigh_out_weight?.let { WeightFormatter.formatWeight(it) } ?: "-"}</span></td>
        </tr>
        <tr>
            <td class="label">Netto</td>
            <td>: ${transaction.net_weight?.let { WeightFormatter.formatWeight(it) } ?: "-"}</td>
        </tr>
    </table>

    <!-- SIGNATURE SECTION -->
    <table class="signature-container">
        <tr class="signature-row">
            <td class="signature-cell">
                <div class="signature-title">Sopir</div>
                <div class="signature-name">( ${transaction.driver_name ?: ".................."} )</div>
            </td>
            <td class="signature-cell">
                <div class="signature-title">Operator</div>
                <div class="signature-name">( .................. )</div>
            </td>
        </tr>
    </table>

    <!-- SEPARATOR LINE -->
    <div class="separator"></div>

    <!-- KETERANGAN SECTION (no box) -->
    <div class="keterangan-section">
        <div class="keterangan-label">Keterangan:</div>
        <div class="keterangan-space"></div>
    </div>

    <!-- FOOTER -->
    ${if (settings.footerText.isNotBlank()) """
    <div class="footer">${settings.footerText}</div>
    """ else ""}
</body>
</html>
        """.trimIndent()
    }
}
