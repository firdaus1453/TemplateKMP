package com.template.project.feature.home.domain

import com.template.project.feature.home.domain.model.Product
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductTest {

    @Test
    fun productModelPropertiesAreCorrect() {
        val product = Product(
            id = 1,
            title = "iPhone 15",
            description = "Apple smartphone",
            category = "smartphones",
            price = 999.99,
            discountPercentage = 5.0,
            rating = 4.8,
            stock = 50,
            brand = "Apple",
            thumbnail = "https://example.com/thumb.jpg",
            images = listOf("https://example.com/img1.jpg")
        )

        assertEquals(1, product.id)
        assertEquals("iPhone 15", product.title)
        assertEquals("Apple smartphone", product.description)
        assertEquals("smartphones", product.category)
        assertEquals(999.99, product.price)
        assertEquals(5.0, product.discountPercentage)
        assertEquals(4.8, product.rating)
        assertEquals(50, product.stock)
        assertEquals("Apple", product.brand)
        assertEquals("https://example.com/thumb.jpg", product.thumbnail)
        assertEquals(1, product.images.size)
    }
}
