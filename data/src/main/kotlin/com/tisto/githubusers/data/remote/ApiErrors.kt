package com.tisto.githubusers.data.remote

import com.tisto.githubusers.domain.model.DataError
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.toDataError())
    }

fun Throwable.toDataError(): DataError = when (this) {
    is DataError -> this
    is HttpException -> toDataError()
    is IOException -> DataError.Network(this)
    else -> DataError.Unknown(this)
}

private fun HttpException.toDataError(): DataError {
    val headers = response()?.headers()
    return when (code()) {
        404 -> DataError.NotFound()
        // A plain 403 is a permission problem, only one with an exhausted quota is a rate limit.
        403 -> if (headers?.get("X-RateLimit-Remaining") == "0") {
            DataError.RateLimited(headers.get("X-RateLimit-Reset")?.toLongOrNull())
        } else {
            DataError.Unknown(this)
        }
        429 -> DataError.RateLimited(headers?.get("X-RateLimit-Reset")?.toLongOrNull())
        else -> DataError.Unknown(this)
    }
}
