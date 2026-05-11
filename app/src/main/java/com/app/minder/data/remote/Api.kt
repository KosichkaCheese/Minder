package com.app.minder.data.remote
import com.app.minder.data.remote.dto.Login
import com.app.minder.data.remote.dto.RefreshRequest
import com.app.minder.data.remote.dto.Register
import com.app.minder.data.remote.dto.TokenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface Api {
    @POST("/auth/register")
    suspend fun register(@Body register: Register): TokenResponse

    @POST("/auth/login")
    suspend fun login(@Body login: Login): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): TokenResponse
}