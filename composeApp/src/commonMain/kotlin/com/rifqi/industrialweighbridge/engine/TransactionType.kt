package com.rifqi.industrialweighbridge.engine

/**
 * Represents the type of weighing transaction.
 *
 * According to HLD:
 * - INBOUND (Penerimaan): First Weight = Gross, Second Weight = Tare
 * - OUTBOUND (Pengiriman): First Weight = Tare, Second Weight = Gross
 */
enum class TransactionType {
    /**
     * Inbound transaction (receiving goods). Vehicle enters with goods (heavy) and leaves empty
     * (light). First Weight = Gross, Second Weight = Tare
     */
    INBOUND,

    /**
     * Outbound transaction (sending goods). Vehicle enters empty (light) and leaves with goods
     * (heavy). First Weight = Tare, Second Weight = Gross
     */
    OUTBOUND
}
