package com.rifqi.industrialweighbridge.di

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
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.AddVehicleUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.DeleteVehicleUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.GetAllVehiclesUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.UpdateVehicleUseCase
import org.koin.dsl.module

val appModule = module {

    // --- Master Data Repositories (SRP) ---

    single<VehicleRepository> { SqlDelightVehicleRepository(db = get()) }

    single<DriverRepository> { SqlDelightDriverRepository(db = get()) }

    single<ProductRepository> { SqlDelightProductRepository(db = get()) }

    // --- Transaction Repository ---
    single<TransactionRepository> { SqlDelightTransactionRepository(db = get()) }

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
}
