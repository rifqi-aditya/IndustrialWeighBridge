package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.db.Product
import com.rifqi.industrialweighbridge.domain.usecase.product.AddProductUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.DeleteProductUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.GetAllProductsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.UpdateProductUseCase
import com.rifqi.industrialweighbridge.infrastructure.AuditAction
import com.rifqi.industrialweighbridge.infrastructure.AuditLogger
import com.rifqi.industrialweighbridge.infrastructure.EntityType
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
    private val deleteProduct: DeleteProductUseCase,
    private val auditLogger: AuditLogger,
    private val getCurrentUsername: () -> String
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

                // Log the action
                auditLogger.log(
                    action = AuditAction.PRODUCT_CREATED,
                    username = getCurrentUsername(),
                    description = "Produk ditambahkan: $name",
                    entityType = EntityType.PRODUCT.name,
                    entityId = name,
                    details = code?.let { "Code: $it" }
                )
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

                // Log the action
                auditLogger.log(
                    action = AuditAction.PRODUCT_UPDATED,
                    username = getCurrentUsername(),
                    description = "Produk diperbarui: $name",
                    entityType = EntityType.PRODUCT.name,
                    entityId = id.toString(),
                    details = "Name: $name, Code: ${code ?: "-"}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun delete(id: Long) {
        scope.launch {
            try {
                // Get product name before deletion for logging
                val productToDelete = _uiState.value.products.find { it.id == id }

                deleteProduct(id)
                _uiState.value = _uiState.value.copy(errorMessage = null)

                // Log the action
                auditLogger.log(
                    action = AuditAction.PRODUCT_DELETED,
                    username = getCurrentUsername(),
                    description = "Produk dihapus: ${productToDelete?.name ?: "ID $id"}",
                    entityType = EntityType.PRODUCT.name,
                    entityId = id.toString(),
                    details = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
