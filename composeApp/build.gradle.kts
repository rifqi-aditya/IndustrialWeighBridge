import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqldelight) // Plugin Database
}

kotlin {
    // Target Platform: JVM (Desktop)
    jvm()

    sourceSets {
        commonMain.dependencies {
            // --- UI Libraries (Bawaan) ---
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // --- NEW: Business Logic Libraries ---

            // 1. Dependency Injection (Agar kode rapi & modular)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // 2. Database Extensions (Agar bisa observe data secara real-time)
            implementation(libs.sqldelight.coroutines)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)

            // Tambahkan ini untuk test Flow/Suspend functions
            implementation(libs.kotlinx.coroutines.test)

            // Tambahkan driver SQLite agar Test bisa jalan di database memori
            implementation(libs.sqldelight.driver.sqlite)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            // Dispatcher khusus UI Desktop (Swing)
            implementation(libs.kotlinx.coroutinesSwing)

            // --- NEW: Desktop Specific Drivers ---

            // 3. Database Driver (Mesin SQLite)
            implementation(libs.sqldelight.driver.sqlite)

            // 4. Hardware Communication (Serial Port / RS232)
            implementation(libs.jserialcomm)
        }
    }
}

// --- Konfigurasi SQLDelight (Generate Database Code) ---
sqldelight {
    databases {
        // Gunakan 'register' (bukan 'create') untuk menghindari error tipe inferensi
        register("WeighbridgeDatabase") {
            packageName.set("com.rifqi.industrialweighbridge.db")
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.rifqi.industrialweighbridge.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.rifqi.industrialweighbridge"
            packageVersion = "1.0.0"
        }
    }
}
