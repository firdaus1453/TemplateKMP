package com.template.project.core.data.networking

import kotlin.test.Test
import kotlin.test.assertTrue

class ConstructUrlTest {

    @Test
    fun constructUrlAppendsRouteToBaseUrl() {
        val route = "/auth/login"
        val url = constructUrl(route)
        assertTrue(url.endsWith("/auth/login"))
    }
}
