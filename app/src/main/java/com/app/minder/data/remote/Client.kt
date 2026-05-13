package com.app.minder.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Client {
    private const val BASE_URL = "http://192.168.1.131:8000/"

    fun createApi(dataStore: DataStore<Preferences>): Api {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(dataStore))
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                client
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
    }

    fun createRefreshApi(dataStore: DataStore<Preferences>): Api{
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
    }
}