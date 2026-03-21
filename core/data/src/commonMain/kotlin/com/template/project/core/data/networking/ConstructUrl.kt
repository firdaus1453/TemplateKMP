package com.template.project.core.data.networking

import com.template.project.BuildKonfig

fun constructUrl(route: String): String {
    val baseUrl = BuildKonfig.BASE_URL.ifEmpty { "https://dummyjson.com" }
    return "${baseUrl}${route}"
}
