package com.rifqi.industrialweighbridge.domain.usecase.transaction

import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/** Use case for getting all transactions (history). */
class GetAllTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(): Flow<List<SelectAllTransactions>> {
        return repository.getAllTransactions()
    }
}
