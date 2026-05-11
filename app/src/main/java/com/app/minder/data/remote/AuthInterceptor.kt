package com.app.minder.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.minder.data.remote.dto.RefreshRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor (
    private val dataStore: DataStore<Preferences>
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            dataStore.data.first()[stringPreferencesKey("access_token")]
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
                dataStore.data.first()[stringPreferencesKey("refresh_token")]
            } ?: return null

            val refreshApi = Client.createRefreshApi(dataStore)
            val response = runBlocking {
                refreshApi.refresh(RefreshRequest(refreshToken))
            }

            runBlocking {
                dataStore.edit { preferences ->
                    preferences[stringPreferencesKey("access_token")] = response.accessToken
                    preferences[stringPreferencesKey("refresh_token")] = response.refreshToken
                }
            }

            response.accessToken
        } catch (e: Exception){
            runBlocking {
                dataStore.edit { prefs ->
                    prefs.remove(stringPreferencesKey("access_token"))
                    prefs.remove(stringPreferencesKey("refresh_token"))
                }
            }
            null
        }
    }
}