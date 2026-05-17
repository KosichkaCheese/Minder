package com.app.minder.domain.model

data class Profile (
    val id: String,
    val userId: String,
    val name: String,
    val isDefault: Boolean,
    val isCurrent: Boolean,
)