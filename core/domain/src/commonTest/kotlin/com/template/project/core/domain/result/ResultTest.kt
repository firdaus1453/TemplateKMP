package com.template.project.core.domain.result

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun mapTransformsSuccessValue() {
        val result: Result<Int, DataError> = Result.Success(42)
        val mapped = result.map { it.toString() }
        assertIs<Result.Success<String>>(mapped)
        assertEquals("42", mapped.data)
    }

    @Test
    fun mapPreservesErrorOnFailure() {
        val result: Result<Int, DataError.Network> =
            Result.Error(DataError.Network.SERVER_ERROR)
        val mapped = result.map { it.toString() }
        assertIs<Result.Error<DataError.Network>>(mapped)
        assertEquals(DataError.Network.SERVER_ERROR, mapped.error)
    }

    @Test
    fun onSuccessExecutesBlockForSuccess() {
        var executed = false
        val result: Result<Int, DataError> = Result.Success(42)
        result.onSuccess { executed = true }
        assertTrue(executed)
    }

    @Test
    fun onSuccessDoesNotExecuteForError() {
        var executed = false
        val result: Result<Int, DataError.Network> =
            Result.Error(DataError.Network.NO_INTERNET)
        result.onSuccess { executed = true }
        assertTrue(!executed)
    }

    @Test
    fun onErrorExecutesBlockForError() {
        var executed = false
        val result: Result<Int, DataError.Network> =
            Result.Error(DataError.Network.UNAUTHORIZED)
        result.onError { executed = true }
        assertTrue(executed)
    }

    @Test
    fun onErrorDoesNotExecuteForSuccess() {
        var executed = false
        val result: Result<Int, DataError> = Result.Success(42)
        result.onError { executed = true }
        assertTrue(!executed)
    }
}
