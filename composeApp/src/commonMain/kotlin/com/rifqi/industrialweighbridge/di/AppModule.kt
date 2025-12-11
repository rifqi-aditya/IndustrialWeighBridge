package com.rifqi.industrialweighbridge.di

import com.rifqi.industrialweighbridge.data.repository.SqlDelightDriverRepository
import com.rifqi.industrialweighbridge.data.repository.SqlDelightProductRepository
import com.rifqi.industrialweighbridge.data.repository.SqlDelightVehicleRepository
import com.rifqi.industrialweighbridge.domain.repository.DriverRepository
import com.rifqi.industrialweighbridge.domain.repository.ProductRepository
import com.rifqi.industrialweighbridge.domain.repository.VehicleRepository
import org.koin.dsl.module

val appModule = module {

    // --- Master Data Repositories (SRP) ---

    single<VehicleRepository> {
        SqlDelightVehicleRepository(db = get())
    }

    single<DriverRepository> {
        SqlDelightDriverRepository(db = get())
    }

    single<ProductRepository> {
        SqlDelightProductRepository(db = get())
    }

    // --- Transaction Repository (Tetap) ---
    single<TransactionRepository> {
        SqlDelightTransactionRepository(db = get())
    }
}