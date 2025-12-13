package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.db.Product
import com.rifqi.industrialweighbridge.domain.usecase.product.AddProductUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.DeleteProductUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.GetAllProductsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.UpdateProductUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
        val products: List<Product> = emptyList(),
        val isLoading: Boolean = true,
        val searchQuery: String = "",
        val errorMessage: String? = null
)

class ProductViewModel(
        private val getAllProducts: GetAllProductsUseCase,
        private val addProduct: AddProductUseCase,
        private val updateProduct: UpdateProductUseCase,
        private val deleteProduct: DeleteProductUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        scope.launch {
            getAllProducts().collect { productList ->
                _uiState.value = _uiState.value.copy(products = productList, isLoading = false)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredProducts(): List<Product> {
        val query = _uiState.value.searchQuery.lowercase()
        return if (query.isEmpty()) {
            _uiState.value.products
        } else {
            _uiState.value.products.filter { product ->
                product.name.lowercase().contains(query) ||
                        (product.code?.lowercase()?.contains(query) == true)
            }
        }
    }

    fun add(name: String, code: String?) {
        scope.launch {
            try {
                addProduct(name, code)
                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun update(id: Long, name: String, code: String?) {
        scope.launch {
            try {
                updateProduct(id, name, code)
                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun delete(id: Long) {
        scope.launch {
            try {
                deleteProduct(id)
                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
