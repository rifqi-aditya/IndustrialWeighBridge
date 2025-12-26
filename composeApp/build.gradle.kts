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
            implementation(compose.materialIconsExtended)
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

            // 3. Navigation (Voyager)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tabNavigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)
            
            // Date/Time handling
            implementation(libs.kotlinx.datetime)
            
            // 4. Settings/Preferences (for storing app settings)
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.1.1")
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

            // Date/Time (must be in jvmMain for runtime)
            implementation(libs.kotlinx.datetime)
            
            // Ensure kotlinx-datetime is actually bundled (explicit dependency)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

            // --- NEW: Desktop Specific Drivers ---

            // 3. Database Driver (Mesin SQLite)
            implementation(libs.sqldelight.driver.sqlite)

            // 4. Hardware Communication (Serial Port / RS232)
            implementation(libs.jserialcomm)
            
            // 5. PDF Generation (Print Ticket) - HTML to PDF
            implementation("org.apache.pdfbox:pdfbox:2.0.29")
            implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")
            
            // 6. BCrypt for password hashing
            implementation("org.mindrot:jbcrypt:0.4")
        }
    }
}

// --- Konfigurasi SQLDelight (Generate Database Code) ---
sqldelight {
    databases {
        // Main database for weighbridge operations
        register("WeighbridgeDatabase") {
            packageName.set("com.rifqi.industrialweighbridge.db")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
        // Separate database for audit logging (to not impact main DB performance)
        register("AuditLogDatabase") {
            packageName.set("com.rifqi.industrialweighbridge.auditlog")
            srcDirs.setFrom("src/commonMain/sqldelight-auditlog")
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
