package com.rifqi.industrialweighbridge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.di.appModule
import com.rifqi.industrialweighbridge.di.databaseModule
import com.rifqi.industrialweighbridge.di.jvmModule
import com.rifqi.industrialweighbridge.presentation.components.WindowTitleBar
import com.rifqi.industrialweighbridge.presentation.navigation.MainNavigationScreen
import com.rifqi.industrialweighbridge.presentation.theme.WeighBridgeTheme
import java.awt.Frame
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin

fun main() = application {
    // 1. Inisialisasi Koin
    initKoin()

    // 2. Paksa Koin untuk membuat Database SEKARANG JUGA
    val db = get().get<WeighbridgeDatabase>()
    println("Database berhasil dibuat/dimuat!")

    val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)

    // 3. Tampilkan Jendela Aplikasi dengan Custom Title Bar
    Window(
            onCloseRequest = ::exitApplication,
            title = "Industrial WeighBridge System",
            state = windowState,
            undecorated = true, // Hilangkan title bar default Windows
            resizable = true
    ) {
        WeighBridgeTheme {
            Column(modifier = Modifier.fillMaxSize()) {
                // Custom Title Bar
                WindowTitleBar(
                        window = window,
                        onMinimize = { window.isMinimized = true },
                        onMaximize = {
                            if (window.extendedState == Frame.MAXIMIZED_BOTH) {
                                window.extendedState = Frame.NORMAL
                            } else {
                                window.extendedState = Frame.MAXIMIZED_BOTH
                            }
                        },
                        onClose = ::exitApplication
                )

                // Main Content
                MainNavigationScreen()
            }
        }
    }
}

fun initKoin() {
    startKoin {
        modules(
                appModule, // Repository, Use Cases, ViewModels & Engine
                databaseModule, // SqlDriver & Database Setup
                jvmModule // JVM-specific: Serial Port, Printer
        )
    }
}
