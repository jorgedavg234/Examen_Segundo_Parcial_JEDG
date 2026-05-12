package com.example.sentryapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sentryapp.screens.SentryScreen
import com.example.sentryapp.ui.theme.SentryAppTheme
import java.io.IOException
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var btSocket: BluetoothSocket? = null
    private var listeningThread: Thread? = null

    // Estado reactivo que Compose observa
    var motion by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SentryAppTheme {
                Scaffold(modifier = androidx.compose.ui.Modifier.fillMaxSize()) { innerPadding ->
                    SentryScreen(
                        innerPadding   = innerPadding,
                        motion         = motion,
                        onLedOn        = { sendData("1") },
                        onLedOff       = { sendData("0") },
                        onConnect      = { connectBT("HC-05") },
                        onDisconnect   = {
                            sendData("0")
                            disconnectBT()
                        }
                    )
                }
            }
        }
    }

    // ── Parser ────────────────────────────────────────────────────────────────
    private fun parseSensorData(line: String) {
        try {
            val parts = line.split(",")
            for (part in parts) {
                val kv = part.split(":")
                if (kv.size == 2) {
                    when (kv[0].trim()) {
                        "M" -> motion = kv[1].trim() == "1"
                    }
                }
            }
            Log.d("SentryBT", "Movimiento: $motion")
        } catch (e: Exception) {
            Log.w("SentryBT", "Error parseando: $line", e)
        }
    }

    // ── Hilo de escucha ───────────────────────────────────────────────────────
    private fun startListening() {
        listeningThread = Thread {
            val buffer = StringBuilder()
            val inputStream = btSocket?.inputStream ?: return@Thread
            try {
                while (!Thread.currentThread().isInterrupted) {
                    val byte = inputStream.read()
                    if (byte == -1) break
                    val char = byte.toChar()
                    if (char == '\n') {
                        val line = buffer.toString().trim()
                        parseSensorData(line)
                        buffer.clear()
                    } else {
                        buffer.append(char)
                    }
                }
            } catch (_: IOException) { }
        }
        listeningThread?.start()
    }

    // ── Bluetooth ─────────────────────────────────────────────────────────────
    @SuppressLint("MissingPermission")
    private fun connectBT(deviceName: String): Boolean {
        return try {
            val btAdapter = getSystemService(BluetoothManager::class.java)?.adapter
            if (btAdapter == null) {
                Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_SHORT).show()
                return false
            }
            val device = btAdapter.bondedDevices.firstOrNull { it.name == deviceName }
            if (device == null) {
                Toast.makeText(this, "Empareja el HC-05 primero", Toast.LENGTH_LONG).show()
                return false
            }
            btSocket = device.createRfcommSocketToServiceRecord(myUUID)
            btSocket?.connect()
            startListening()
            Toast.makeText(this, "Conectado a $deviceName", Toast.LENGTH_SHORT).show()
            true
        } catch (e: IOException) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            btSocket = null
            false
        }
    }

    private fun stopListening() {
        listeningThread?.interrupt()
        listeningThread = null
    }

    private fun disconnectBT() {
        stopListening()
        try { btSocket?.close() } catch (_: IOException) { }
        btSocket = null
    }

    private fun sendData(value: String) {
        try {
            btSocket?.outputStream?.write(value.toByteArray())
        } catch (e: Exception) {
            Toast.makeText(this, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}