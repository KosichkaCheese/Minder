package com.app.minder.data.remote.dto

data class Register(
    val email: String,
    val password: String,
    val name: String
)

data class Login(
    val email: String,
    val password: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val user: UserResponse
)

data class UserResponse(
    val id: String,
    val name: String,
    val email: String
)

data class RefreshRequest(
    val refreshToken: String
)