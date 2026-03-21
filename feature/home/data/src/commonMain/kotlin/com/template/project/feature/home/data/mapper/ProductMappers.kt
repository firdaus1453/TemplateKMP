package com.template.project.feature.home.data.mapper

import com.template.project.feature.home.data.dto.ProductDto
import com.template.project.feature.home.domain.model.Product

fun ProductDto.toDomain(): Product {
    return Product(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand ?: "",
        thumbnail = thumbnail,
        images = images,
    )
}
