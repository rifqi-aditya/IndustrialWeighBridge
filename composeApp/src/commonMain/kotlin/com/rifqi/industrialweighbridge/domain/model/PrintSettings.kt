package com.rifqi.industrialweighbridge.domain.model

/** Paper size options for ticket printing */
enum class PaperSize(val displayName: String, val widthMm: Float, val heightMm: Float) {
    A4("A4", 210f, 297f),
    A5("A5", 148f, 210f),
    THERMAL_80MM("Thermal 80mm", 80f, 200f),
    THERMAL_58MM("Thermal 58mm", 58f, 150f)
}

/**
 * Print/ticket settings configuration (layout and formatting only) Company information is stored
 * separately in CompanySettings
 */
data class PrintSettings(
        val paperSize: PaperSize = PaperSize.A4,
        val showLogo: Boolean = true,
        val footerText: String = ""
)
