package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.domain.usecase.driver.GetAllDriversUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.GetAllProductsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.GetAllTransactionsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.GetOpenTransactionsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.GetAllVehiclesUseCase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI State for Dashboard */
data class DashboardUiState(
        // Today's stats
        val transactionsToday: Int = 0,
        val openTransactions: Int = 0,
        val totalNettoToday: Double = 0.0,

        // Monthly stats
        val transactionsThisMonth: Int = 0,

        // Master data counts
        val totalDrivers: Int = 0,
        val totalVehicles: Int = 0,
        val totalProducts: Int = 0,

        // Recent transactions
        val recentTransactions: List<SelectAllTransactions> = emptyList(),

        // Loading state
        val isLoading: Boolean = true
)

/** ViewModel for Dashboard statistics */
class DashboardViewModel(
        private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
        private val getOpenTransactionsUseCase: GetOpenTransactionsUseCase,
        private val getAllDriversUseCase: GetAllDriversUseCase,
        private val getAllVehiclesUseCase: GetAllVehiclesUseCase,
        private val getAllProductsUseCase: GetAllProductsUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val todayDateString: String
        get() {
            val today = LocalDate.now()
            return today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }

    private val thisMonthPrefix: String
        get() {
            val today = LocalDate.now()
            return "%04d-%02d".format(today.year, today.monthValue)
        }

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Load all transactions
        scope.launch {
            getAllTransactionsUseCase().collect { transactions ->
                val today = todayDateString
                val monthPrefix = thisMonthPrefix

                // Calculate today's stats
                val todayTransactions =
                        transactions.filter { it.weigh_in_timestamp.startsWith(today) }
                val transactionsToday = todayTransactions.size
                val totalNettoToday = todayTransactions.mapNotNull { it.net_weight }.sum()

                // Calculate monthly stats
                val transactionsThisMonth =
                        transactions.count { it.weigh_in_timestamp.startsWith(monthPrefix) }

                // Get recent transactions (last 5)
                val recentTransactions = transactions.take(5)

                _uiState.value =
                        _uiState.value.copy(
                                transactionsToday = transactionsToday,
                                totalNettoToday = totalNettoToday,
                                transactionsThisMonth = transactionsThisMonth,
                                recentTransactions = recentTransactions,
                                isLoading = false
                        )
            }
        }

        // Load open transactions count
        scope.launch {
            getOpenTransactionsUseCase().collect { openTxns ->
                _uiState.value = _uiState.value.copy(openTransactions = openTxns.size)
            }
        }

        // Load driver count
        scope.launch {
            getAllDriversUseCase().collect { drivers ->
                _uiState.value = _uiState.value.copy(totalDrivers = drivers.size)
            }
        }

        // Load vehicle count
        scope.launch {
            getAllVehiclesUseCase().collect { vehicles ->
                _uiState.value = _uiState.value.copy(totalVehicles = vehicles.size)
            }
        }

        // Load product count
        scope.launch {
            getAllProductsUseCase().collect { products ->
                _uiState.value = _uiState.value.copy(totalProducts = products.size)
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadDashboardData()
    }
}
