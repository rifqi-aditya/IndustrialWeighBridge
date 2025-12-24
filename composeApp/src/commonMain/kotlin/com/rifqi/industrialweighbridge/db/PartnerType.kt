package com.rifqi.industrialweighbridge.db

/**
 * Enum for Partner type.
 * - SUPPLIER: Only used for Inbound transactions (receiving goods)
 * - CUSTOMER: Only used for Outbound transactions (sending goods)
 * - BOTH: Can be used for both Inbound and Outbound transactions
 */
enum class PartnerType {
    SUPPLIER,
    CUSTOMER,
    BOTH
}
