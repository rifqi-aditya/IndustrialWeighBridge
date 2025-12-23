package com.rifqi.industrialweighbridge.infrastructure

import com.rifqi.industrialweighbridge.engine.WeightReading
import kotlinx.coroutines.flow.Flow

/**
 * Interface for serial communication with weighing scale device.
 *
 * According to HLD Section 3.3: "Infrastructure Layer menyediakan layanan teknis tingkat rendah
 * yang bersifat stateless dan business-agnostic."
 *
 * This interface defines the contract for reading weight data from hardware. Implementations will
 * use jSerialComm library on JVM platform.
 */
interface SerialCommunicationHandler {

    /** Gets list of available serial ports. */
    suspend fun getAvailablePorts(): List<SerialPortInfo>

    /**
     * Connects to the specified serial port.
     *
     * @param portName The name/path of the serial port (e.g., "COM3" or "/dev/ttyUSB0")
     * @param config Configuration for the serial connection
     * @return True if connection successful
     */
    suspend fun connect(portName: String, config: SerialConfig = SerialConfig()): Boolean

    /** Disconnects from the current serial port. */
    suspend fun disconnect()

    /** Checks if currently connected to a serial port. */
    fun isConnected(): Boolean

    /** Gets the current connection status as a flow. */
    fun connectionStatus(): Flow<ConnectionStatus>

    /**
     * Gets continuous weight readings from the connected device. Emits new readings as they are
     * received from the scale.
     */
    fun weightReadings(): Flow<WeightReading>

    /** Gets the last known weight reading. */
    fun getLastReading(): WeightReading?
}

/** Information about a serial port. */
data class SerialPortInfo(val name: String, val description: String, val isAvailable: Boolean)

/** Configuration for serial port connection. */
data class SerialConfig(
        val baudRate: Int = 9600,
        val dataBits: Int = 8,
        val stopBits: Int = 1,
        val parity: Parity = Parity.NONE,
        val readTimeoutMs: Int = 1000
)

/** Parity options for serial communication. */
enum class Parity {
    NONE,
    ODD,
    EVEN,
    MARK,
    SPACE
}

/** Connection status for serial port. */
enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}
