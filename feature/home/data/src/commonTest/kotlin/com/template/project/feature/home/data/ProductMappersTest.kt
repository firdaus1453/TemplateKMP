package com.template.project.feature.home.data

import com.template.project.feature.home.data.dto.ProductDto
import com.template.project.feature.home.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductMappersTest {

    @Test
    fun toDomainMapsAllFieldsCorrectly() {
        val dto = ProductDto(
            id = 10,
            title = "MacBook Pro",
            description = "Apple M3 laptop",
            category = "laptops",
            price = 1999.0,
            discountPercentage = 10.0,
            rating = 4.9,
            stock = 25,
            brand = "Apple",
            thumbnail = "https://example.com/macbook.png",
            images = listOf("https://example.com/macbook1.png")
        )

        val domain = dto.toDomain()

        assertEquals(10, domain.id)
        assertEquals("MacBook Pro", domain.title)
        assertEquals("Apple M3 laptop", domain.description)
        assertEquals("laptops", domain.category)
        assertEquals(1999.0, domain.price)
        assertEquals(10.0, domain.discountPercentage)
        assertEquals(4.9, domain.rating)
        assertEquals(25, domain.stock)
        assertEquals("Apple", domain.brand)
        assertEquals("https://example.com/macbook.png", domain.thumbnail)
        assertEquals(listOf("https://example.com/macbook1.png"), domain.images)
    }

    @Test
    fun toDomainHandlesNullBrand() {
        val dto = ProductDto(
            id = 11,
            title = "Generic Item",
            description = "No brand item",
            category = "misc",
            price = 9.99,
            discountPercentage = 0.0,
            rating = 3.5,
            stock = 100,
            brand = null,
            thumbnail = "https://example.com/item.png",
            images = emptyList()
        )

        val domain = dto.toDomain()
        assertEquals("", domain.brand)
    }
}
