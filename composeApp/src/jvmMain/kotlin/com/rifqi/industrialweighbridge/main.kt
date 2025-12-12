package com.rifqi.industrialweighbridge

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.di.appModule
import com.rifqi.industrialweighbridge.di.databaseModule
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin

fun main() = application {
    // 1. Inisialisasi Koin
    initKoin()

    // 2. [BARU] Paksa Koin untuk membuat Database SEKARANG JUGA
    // Baris ini akan memicu kode di DatabaseModule untuk jalan -> File .db akan dibuat.
    val db = get().get<WeighbridgeDatabase>()
    println("Database berhasil dibuat/dimuat!")

    // 3. Tampilkan Jendela Aplikasi
    Window(
        onCloseRequest = ::exitApplication,
        title = "Industrial WeighBridge System",
    ) {
        // App()
    }
}

fun initKoin() {
    startKoin {
        modules(
            appModule,      // Repository & Use Cases
            databaseModule  // SqlDriver & Database Setup
        )
    }
}