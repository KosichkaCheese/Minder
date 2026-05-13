package com.app.minder.util.notifications

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

fun hasGooglePlayServices(context: Context): Boolean {
    val availability = GoogleApiAvailability.getInstance()
    val result = availability.isGooglePlayServicesAvailable(context)
    return result == ConnectionResult.SUCCESS
}