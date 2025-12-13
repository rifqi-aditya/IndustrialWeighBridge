package com.rifqi.industrialweighbridge.domain.usecase.transaction

import com.rifqi.industrialweighbridge.db.SelectOpenTransactions
import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting open (active) transactions. These are vehicles that have done Weigh-In but
 * not yet Weigh-Out.
 */
class GetOpenTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(): Flow<List<SelectOpenTransactions>> {
        return repository.getOpenTransactions()
    }
}
