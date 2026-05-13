package com.app.minder.data.remote.dto

data class InviteObserverResponse(
    val code: String,
    val expiresAt: String
)

data class InvitationAcceptRequest(
    val code: String
)
