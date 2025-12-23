package com.rifqi.industrialweighbridge.data.repository

import com.rifqi.industrialweighbridge.domain.model.CompanySettings
import com.rifqi.industrialweighbridge.domain.model.PaperSize
import com.rifqi.industrialweighbridge.domain.model.PrintSettings
import com.russhwolf.settings.Settings

/** Repository for storing and retrieving app settings using multiplatform-settings */
class SettingsRepository {
    private val settings: Settings = Settings()

    // Keys for Company Settings
    private companion object {
        const val KEY_COMPANY_NAME = "company_name"
        const val KEY_COMPANY_ADDRESS = "company_address"
        const val KEY_COMPANY_PHONE = "company_phone"
        const val KEY_COMPANY_FAX = "company_fax"

        const val KEY_PAPER_SIZE = "paper_size"
        const val KEY_SHOW_LOGO = "show_logo"
        const val KEY_FOOTER_TEXT = "footer_text"
    }

    // ==================== Company Settings ====================

    fun saveCompanySettings(companySettings: CompanySettings) {
        settings.putString(KEY_COMPANY_NAME, companySettings.companyName)
        settings.putString(KEY_COMPANY_ADDRESS, companySettings.companyAddress)
        settings.putString(KEY_COMPANY_PHONE, companySettings.companyPhone)
        settings.putString(KEY_COMPANY_FAX, companySettings.companyFax)
    }

    fun loadCompanySettings(): CompanySettings {
        return CompanySettings(
                companyName = settings.getStringOrNull(KEY_COMPANY_NAME) ?: "",
                companyAddress = settings.getStringOrNull(KEY_COMPANY_ADDRESS) ?: "",
                companyPhone = settings.getStringOrNull(KEY_COMPANY_PHONE) ?: "",
                companyFax = settings.getStringOrNull(KEY_COMPANY_FAX) ?: ""
        )
    }

    // ==================== Print Settings ====================

    fun savePrintSettings(printSettings: PrintSettings) {
        settings.putString(KEY_PAPER_SIZE, printSettings.paperSize.name)
        settings.putBoolean(KEY_SHOW_LOGO, printSettings.showLogo)
        settings.putString(KEY_FOOTER_TEXT, printSettings.footerText)
    }

    fun loadPrintSettings(): PrintSettings {
        val paperSizeName = settings.getStringOrNull(KEY_PAPER_SIZE)
        val paperSize =
                paperSizeName?.let {
                    try {
                        PaperSize.valueOf(it)
                    } catch (e: Exception) {
                        PaperSize.A4
                    }
                }
                        ?: PaperSize.A4

        return PrintSettings(
                paperSize = paperSize,
                showLogo = settings.getBoolean(KEY_SHOW_LOGO, true),
                footerText = settings.getStringOrNull(KEY_FOOTER_TEXT) ?: ""
        )
    }
}
