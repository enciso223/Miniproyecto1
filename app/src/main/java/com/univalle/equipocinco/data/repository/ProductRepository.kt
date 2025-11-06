package com.univalle.equipocinco.data.repository

import com.univalle.equipocinco.data.local.dao.ProductDao
import com.univalle.equipocinco.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao
) {

    // ✅ Obtener todos los productos como Flow
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    // ✅ Obtener producto por ID
    suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)
    }

    // ✅ Insertar producto
    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    // ✅ Actualizar producto
    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    // ✅ Eliminar producto
    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    // ✅ Obtener valor total del inventario
    suspend fun getTotalInventoryValue(): Double {
        return productDao.getTotalInventoryValue() ?: 0.0
    }
}
