package com.app.minder.domain.model

import java.util.UUID

data class Profile (
    val id: String,
    val userId: String,
    val name: String,
    val isDefault: Boolean,
    val isCurrent: Boolean,
)