package com.rifqi.industrialweighbridge.presentation.print

import com.rifqi.industrialweighbridge.data.repository.SettingsRepository
import com.rifqi.industrialweighbridge.db.SelectAllTransactions

/** JVM implementation of print function */
actual fun printTransaction(
        transaction: SelectAllTransactions,
        settingsRepository: SettingsRepository
): Result<String> {
    return PrintTicketUtil.printTicket(transaction, settingsRepository).map { file ->
        file.absolutePath
    }
}
