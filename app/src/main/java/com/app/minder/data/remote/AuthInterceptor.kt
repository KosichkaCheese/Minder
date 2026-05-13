package com.app.minder.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.app.minder.data.remote.dto.RefreshRequest
import com.app.minder.util.PreferencesKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor (
    private val dataStore: DataStore<Preferences>
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            dataStore.data.first()[PreferencesKeys.ACCESS_TOKEN]
        }

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        val response = chain.proceed(request)

        if (response.code == 401 && token != null) {
            response.close()

            val newToken = refreshToken()

            if (newToken != null) {
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(newRequest)
            }
        }

        return response
    }

    private fun refreshToken(): String? {
        return try {
            val refreshToken = runBlocking {
                dataStore.data.first()[PreferencesKeys.REFRESH_TOKEN]
            } ?: return null

            val refreshApi = Client.createRefreshApi(dataStore)
            val response = runBlocking {
                refreshApi.refresh(RefreshRequest(refreshToken))
            }

            runBlocking {
                dataStore.edit { preferences ->
                    preferences[PreferencesKeys.ACCESS_TOKEN] = response.accessToken
                    preferences[PreferencesKeys.REFRESH_TOKEN] = response.refreshToken
                }
            }

            response.accessToken
        } catch (e: Exception){
            runBlocking {
                dataStore.edit { prefs ->
                    prefs.remove(PreferencesKeys.ACCESS_TOKEN)
                    prefs.remove(PreferencesKeys.REFRESH_TOKEN)
                }
            }
            null
        }
    }
}