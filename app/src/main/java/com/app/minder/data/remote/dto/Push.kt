package com.app.minder.data.remote.dto

data class DeviceTokenRequest(
    val token: String
)

data class MissedIntakeNotification(
    val medName: String,
    val time: String
)