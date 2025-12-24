package com.rifqi.industrialweighbridge.engine

/**
 * Weight reading from the scale device. Used by Infrastructure Layer to communicate raw weight
 * data.
 */
data class WeightReading(
    val weight: Double,
    val unit: WeightUnit = WeightUnit.KILOGRAM,
    val isStable: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/** Weight measurement units. */
enum class WeightUnit {
    KILOGRAM,
    TON,
    POUND
}
