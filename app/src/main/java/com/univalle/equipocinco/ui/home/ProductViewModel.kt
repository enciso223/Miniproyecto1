package com.univalle.equipocinco.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.equipocinco.data.local.entity.Product
import com.univalle.equipocinco.data.repository.ProductRepository
import com.univalle.equipocinco.widget.InventoryWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ProductRepository,
    private val appContext: Context
) : ViewModel() {

    // Flow de todos los productos
    val productsFlow: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Total general calculado automáticamente
    val totalFlow: StateFlow<Double> = repository.getAllProducts()
        .map { productList ->
            productList.sumOf { it.price * it.quantity }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Obtener producto por ID
    fun getProductById(id: Int): Flow<Product?> {
        return repository.getAllProducts().map { products ->
            products.find { it.id == id }
        }
    }

    // Agregar producto
    fun addProduct(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertProduct(product)
            InventoryWidgetProvider.updateAllWidgets(appContext)
        }
    }

    // Actualizar producto
    fun updateProduct(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProduct(product)
            InventoryWidgetProvider.updateAllWidgets(appContext)
        }
    }

    // Eliminar producto
    fun deleteProduct(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProduct(product)
            InventoryWidgetProvider.updateAllWidgets(appContext)
        }
    }
}