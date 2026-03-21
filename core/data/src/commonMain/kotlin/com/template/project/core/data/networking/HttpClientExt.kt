package com.template.project.core.data.networking

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException

suspend inline fun <reified Response : Any> HttpClient.safeGet(
    route: String,
    queryParams: Map<String, Any?> = emptyMap(),
): Result<Response, DataError.Network> {
    return safeCall {
        get {
            url(constructUrl(route))
            queryParams.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.safePost(
    route: String,
    body: Request,
): Result<Response, DataError.Network> {
    return safeCall {
        post {
            url(constructUrl(route))
            setBody(body)
        }
    }
}

suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.safePut(
    route: String,
    body: Request,
): Result<Response, DataError.Network> {
    return safeCall {
        put {
            url(constructUrl(route))
            setBody(body)
        }
    }
}

suspend inline fun <reified Response : Any> HttpClient.safeDelete(
    route: String,
): Result<Response, DataError.Network> {
    return safeCall {
        delete {
            url(constructUrl(route))
        }
    }
}

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse,
): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SerializationException) {
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        return Result.Error(DataError.Network.UNKNOWN)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse,
): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> Result.Success(response.body<T>())
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Network.CONFLICT)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}
