package com.tisto.githubusers.data.remote

import com.tisto.githubusers.data.remote.dto.SearchUsersResponse
import com.tisto.githubusers.data.remote.dto.UserDetailDto
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApi {

    @Headers(ACCEPT, API_VERSION)
    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = PAGE_SIZE,
        @Query("page") page: Int = 1,
    ): SearchUsersResponse

    @Headers(ACCEPT, API_VERSION)
    @GET("users/{login}")
    suspend fun getUser(@Path("login") login: String): UserDetailDto

    companion object {
        const val BASE_URL = "https://api.github.com/"
        const val PAGE_SIZE = 30

        private const val ACCEPT = "Accept: application/vnd.github+json"
        private const val API_VERSION = "X-GitHub-Api-Version: 2022-11-28"
    }
}
