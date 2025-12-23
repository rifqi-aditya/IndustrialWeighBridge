package com.rifqi.industrialweighbridge.infrastructure

import com.rifqi.industrialweighbridge.engine.WeightReading
import com.rifqi.industrialweighbridge.engine.WeightUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * JVM implementation of SerialCommunicationHandler.
 *
 * This is a simplified implementation that will be enhanced later when connecting to actual
 * hardware.
 *
 * For now, it provides:
 * - Port listing using jSerialComm
 * - Simulated weight readings for testing
 */
class JvmSerialCommunicationHandler : SerialCommunicationHandler {

    private var isConnected = false
    private var currentConfig: SerialConfig = SerialConfig()
    private var connectedPortName: String? = null

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    private var lastReading: WeightReading? = null

    override suspend fun getAvailablePorts(): List<SerialPortInfo> =
            withContext(Dispatchers.IO) {
                try {
                    // Try to use jSerialComm to list ports
                    val serialPortClass = Class.forName("com.fazecast.jSerialComm.SerialPort")
                    val getCommPortsMethod = serialPortClass.getMethod("getCommPorts")
                    val ports = getCommPortsMethod.invoke(null) as Array<*>

                    ports.mapNotNull { port ->
                        if (port == null) return@mapNotNull null
                        val getSystemPortNameMethod = port.javaClass.getMethod("getSystemPortName")
                        val getDescriptivePortNameMethod =
                                port.javaClass.getMethod("getDescriptivePortName")
                        val isOpenMethod = port.javaClass.getMethod("isOpen")

                        SerialPortInfo(
                                name = getSystemPortNameMethod.invoke(port) as String,
                                description = getDescriptivePortNameMethod.invoke(port) as String,
                                isAvailable = !(isOpenMethod.invoke(port) as Boolean)
                        )
                    }
                } catch (e: Exception) {
                    // If jSerialComm is not available, return empty list
                    println("Warning: Could not list serial ports: ${e.message}")
                    emptyList()
                }
            }

    override suspend fun connect(portName: String, config: SerialConfig): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    _connectionStatus.value = ConnectionStatus.CONNECTING

                    // For now, just simulate a successful connection
                    // Actual jSerialComm connection will be implemented when hardware is available
                    connectedPortName = portName
                    currentConfig = config
                    isConnected = true
                    _connectionStatus.value = ConnectionStatus.CONNECTED

                    println("Serial port $portName connected (simulated)")
                    true
                } catch (e: Exception) {
                    _connectionStatus.value = ConnectionStatus.ERROR
                    println("Failed to connect to serial port: ${e.message}")
                    false
                }
            }

    override suspend fun disconnect() =
            withContext(Dispatchers.IO) {
                isConnected = false
                connectedPortName = null
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                println("Serial port disconnected")
            }

    override fun isConnected(): Boolean = isConnected

    override fun connectionStatus(): Flow<ConnectionStatus> = _connectionStatus

    override fun weightReadings(): Flow<WeightReading> = flow {
        // Simulated weight readings for testing
        // Replace with actual serial port reading when hardware is connected
        while (isConnected) {
            // Simulate a weight reading every 500ms
            val simulatedWeight = (1000..5000).random().toDouble()
            val reading =
                    WeightReading(
                            weight = simulatedWeight,
                            unit = WeightUnit.KILOGRAM,
                            isStable = true,
                            timestamp = System.currentTimeMillis()
                    )
            lastReading = reading
            emit(reading)
            delay(500)
        }
    }

    override fun getLastReading(): WeightReading? = lastReading
}
