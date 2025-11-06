package com.univalle.equipocinco.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.univalle.equipocinco.data.local.database.InventoryDatabase
import com.univalle.equipocinco.data.repository.ProductRepository

class ProductViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            val database = InventoryDatabase.getDatabase(context)
            val repository = ProductRepository(database.productDao())
            return ProductViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}