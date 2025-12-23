package com.rifqi.industrialweighbridge.di

// Engine Layer
// Infrastructure Layer
import com.rifqi.industrialweighbridge.data.repository.SettingsRepository
import com.rifqi.industrialweighbridge.data.repository.SqlDelightDriverRepository
import com.rifqi.industrialweighbridge.data.repository.SqlDelightProductRepository
import com.rifqi.industrialweighbridge.data.repository.SqlDelightTransactionRepository
import com.rifqi.industrialweighbridge.data.repository.SqlDelightVehicleRepository
import com.rifqi.industrialweighbridge.domain.repository.DriverRepository
import com.rifqi.industrialweighbridge.domain.repository.ProductRepository
import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository
import com.rifqi.industrialweighbridge.domain.repository.VehicleRepository
import com.rifqi.industrialweighbridge.domain.usecase.driver.AddDriverUseCase
import com.rifqi.industrialweighbridge.domain.usecase.driver.DeleteDriverUseCase
import com.rifqi.industrialweighbridge.domain.usecase.driver.GetAllDriversUseCase
import com.rifqi.industrialweighbridge.domain.usecase.driver.UpdateDriverUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.AddProductUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.DeleteProductUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.GetAllProductsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.UpdateProductUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.CreateWeighInUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.GetAllTransactionsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.GetOpenTransactionsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.UpdateWeighOutUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.AddVehicleUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.DeleteVehicleUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.GetAllVehiclesUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.UpdateVehicleUseCase
import com.rifqi.industrialweighbridge.engine.StabilityConfig
import com.rifqi.industrialweighbridge.engine.WeighingEngine
import com.rifqi.industrialweighbridge.infrastructure.AuditLogger
import com.rifqi.industrialweighbridge.infrastructure.InMemoryAuditLogger
import com.rifqi.industrialweighbridge.presentation.viewmodel.DashboardViewModel
import com.rifqi.industrialweighbridge.presentation.viewmodel.DriverViewModel
import com.rifqi.industrialweighbridge.presentation.viewmodel.ProductViewModel
import com.rifqi.industrialweighbridge.presentation.viewmodel.VehicleViewModel
import com.rifqi.industrialweighbridge.presentation.viewmodel.WeighingViewModel
import org.koin.dsl.module

val appModule = module {

    // --- Settings Repository (for app preferences) ---
    single { SettingsRepository() }

    // --- Master Data Repositories (SRP) ---

    single<VehicleRepository> { SqlDelightVehicleRepository(db = get()) }

    single<DriverRepository> { SqlDelightDriverRepository(db = get()) }

    single<ProductRepository> { SqlDelightProductRepository(db = get()) }

    // --- Transaction Repository ---
    single<TransactionRepository> { SqlDelightTransactionRepository(db = get()) }

    // === NEW: Core Weighing Engine (HLD Section 3.2) ===

    // Stability configuration
    single { StabilityConfig(windowSize = 5, toleranceKg = 2.0, minimumWeightKg = 50.0) }

    // Core Weighing Engine - Single source of truth for weighing operations
    single { WeighingEngine(transactionRepository = get(), stabilityConfig = get()) }

    // === NEW: Infrastructure Layer (HLD Section 3.3) ===

    // Audit Logger
    single<AuditLogger> { InMemoryAuditLogger() }

    // Note: SerialCommunicationHandler and PrinterService are JVM-specific
    // They are registered in jvmModule.kt

    // --- Driver Use Cases ---
    factory { GetAllDriversUseCase(get()) }
    factory { AddDriverUseCase(get()) }
    factory { UpdateDriverUseCase(get()) }
    factory { DeleteDriverUseCase(get()) }

    // --- Product Use Cases ---
    factory { GetAllProductsUseCase(get()) }
    factory { AddProductUseCase(get()) }
    factory { UpdateProductUseCase(get()) }
    factory { DeleteProductUseCase(get()) }

    // --- Vehicle Use Cases ---
    factory { GetAllVehiclesUseCase(get()) }
    factory { AddVehicleUseCase(get()) }
    factory { UpdateVehicleUseCase(get()) }
    factory { DeleteVehicleUseCase(get()) }

    // --- Transaction Use Cases ---
    factory { GetAllTransactionsUseCase(get()) }
    factory { GetOpenTransactionsUseCase(get()) }
    factory { CreateWeighInUseCase(get()) }
    factory { UpdateWeighOutUseCase(get()) }

    // --- ViewModels ---
    factory { DriverViewModel(get(), get(), get(), get()) }
    factory { VehicleViewModel(get(), get(), get(), get()) }
    factory { ProductViewModel(get(), get(), get(), get()) }
    // WeighingViewModel now uses WeighingEngine as single source of truth
    factory {
        WeighingViewModel(
                weighingEngine = get(),
                getAllVehiclesUseCase = get(),
                getAllDriversUseCase = get(),
                getAllProductsUseCase = get(),
                getAllTransactionsUseCase = get(),
                getOpenTransactionsUseCase = get()
        )
    }
    factory { DashboardViewModel(get(), get(), get(), get(), get()) }
}
