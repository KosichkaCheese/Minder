package com.app.minder.domain.model

data class AuthToken(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAt: Long
)
