package com.app.minder.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long
)
