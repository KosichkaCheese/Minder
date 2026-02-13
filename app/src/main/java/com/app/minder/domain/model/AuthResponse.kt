package com.app.minder.domain.model

data class AuthResponse(
    val user: User,
    val token: AuthToken
)
