package com.rifqi.industrialweighbridge.presentation.print

import com.rifqi.industrialweighbridge.data.repository.SettingsRepository
import com.rifqi.industrialweighbridge.db.SelectAllTransactions

/** Expected platform-specific print function */
expect fun printTransaction(
        transaction: SelectAllTransactions,
        settingsRepository: SettingsRepository
): Result<String>
