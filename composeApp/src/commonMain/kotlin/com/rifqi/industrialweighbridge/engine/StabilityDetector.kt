package com.rifqi.industrialweighbridge.engine

/**
 * Detects weight stability based on configuration parameters.
 *
 * According to HLD Section 4.1: "Core Weighing Engine melakukan evaluasi stabilitas berat
 * berdasarkan parameter konfigurasi."
 *
 * Uses a sliding window approach to analyze variance of weight readings.
 */
class StabilityDetector(private val config: StabilityConfig = StabilityConfig()) {
    private val readings = mutableListOf<Double>()

    /**
     * Adds a new weight reading and checks stability.
     *
     * @param weight The current weight reading in kg
     * @return True if weight is considered stable
     */
    fun addReading(weight: Double): Boolean {
        readings.add(weight)

        // Keep only the last N readings based on window size
        while (readings.size > config.windowSize) {
            readings.removeAt(0)
        }

        return isStable()
    }

    /**
     * Checks if the current readings indicate stable weight.
     *
     * Stability is determined by:
     * 1. Having enough readings (at least windowSize)
     * 2. All readings within tolerance of the average
     */
    fun isStable(): Boolean {
        if (readings.size < config.windowSize) {
            return false
        }

        val average = readings.average()

        // Check if all readings are within tolerance of the average
        return readings.all { reading -> kotlin.math.abs(reading - average) <= config.toleranceKg }
    }

    /**
     * Gets the current stable weight value.
     *
     * @return The average of readings if stable, null otherwise
     */
    fun getStableWeight(): Double? {
        return if (isStable()) readings.average() else null
    }

    /** Gets the latest weight reading. */
    fun getLatestWeight(): Double? = readings.lastOrNull()

    /** Clears all readings. Call when starting a new weighing operation. */
    fun reset() {
        readings.clear()
    }
}

/** Configuration for stability detection. */
data class StabilityConfig(
    /** Number of readings to consider for stability check */
    val windowSize: Int = 5,

    /** Maximum allowed deviation from average in kg */
    val toleranceKg: Double = 2.0,

    /** Minimum weight threshold in kg (to avoid noise at zero) */
    val minimumWeightKg: Double = 50.0
)
