package com.univalle.equipocinco.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.equipocinco.data.local.database.InventoryDatabase
import com.univalle.equipocinco.data.local.entity.Product
import com.univalle.equipocinco.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val productDao = InventoryDatabase.getDatabase(application).productDao()
        repository = ProductRepository(productDao)
    }

    fun loadProduct(productId: Int) {
        if (productId < 0) return
        viewModelScope.launch {
            _isLoading.value = true
            _product.value = repository.getProductById(productId)
            _isLoading.value = false
        }
    }

    fun deleteCurrentProduct(onDeleted: () -> Unit, onError: () -> Unit) {
        val current = _product.value ?: return onError()
        viewModelScope.launch {
            try {
                repository.deleteProduct(current)
                onDeleted()
            } catch (_: Exception) {
                onError()
            }
        }
    }
}

