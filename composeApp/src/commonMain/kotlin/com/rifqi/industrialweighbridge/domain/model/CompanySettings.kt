package com.rifqi.industrialweighbridge.domain.model

/** Company information settings for ticket printing */
data class CompanySettings(
        val companyName: String = "",
        val companyAddress: String = "",
        val companyPhone: String = "",
        val companyFax: String = ""
)
