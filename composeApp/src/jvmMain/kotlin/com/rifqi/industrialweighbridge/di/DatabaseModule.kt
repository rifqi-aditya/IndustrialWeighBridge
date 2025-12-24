// FILE: composeApp/src/jvmMain/kotlin/com/rifqi/industrialweighbridge/di/DatabaseModule.kt

package com.rifqi.industrialweighbridge.di

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rifqi.industrialweighbridge.db.User
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.db.WeighingTransaction
import java.io.File
import org.koin.dsl.module

val databaseModule = module {
    // 1. Definisikan Driver (Mesin Penghubung ke File Database)
    single<SqlDriver> {
        // Nama file database yang akan muncul di folder project
        val dbFileName = "weighbridge.db"
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbFileName")

        // Cek apakah file sudah ada? Jika belum, buat tabel-tabelnya otomatis.
        if (!File(dbFileName).exists()) {
            WeighbridgeDatabase.Schema.create(driver)
        }

        driver
    }

    // 2. Definisikan Database Utama
    single {
        WeighbridgeDatabase(
                driver = get(),
                UserAdapter = User.Adapter(roleAdapter = EnumColumnAdapter()),
                WeighingTransactionAdapter =
                        WeighingTransaction.Adapter(
                                statusAdapter = EnumColumnAdapter(),
                                transaction_typeAdapter = EnumColumnAdapter()
                        )
        )
    }
}
