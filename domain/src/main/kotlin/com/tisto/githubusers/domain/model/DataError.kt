package com.tisto.githubusers.domain.model

sealed class DataError(cause: Throwable? = null) : Exception(cause) {
    class Network(cause: Throwable? = null) : DataError(cause)
    class RateLimited(val resetAtEpochSeconds: Long?) : DataError()
    class NotFound : DataError()
    class Unknown(cause: Throwable? = null) : DataError(cause)
}
