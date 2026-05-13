package com.app.minder.data.remote
import com.app.minder.data.remote.dto.DeviceTokenRequest
import com.app.minder.data.remote.dto.InvitationAcceptRequest
import com.app.minder.data.remote.dto.InviteObserverResponse
import com.app.minder.data.remote.dto.Login
import com.app.minder.data.remote.dto.MissedIntakeNotification
import com.app.minder.data.remote.dto.RefreshRequest
import com.app.minder.data.remote.dto.Register
import com.app.minder.data.remote.dto.TokenResponse
import com.app.minder.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface Api {
    @POST("/auth/register")
    suspend fun register(@Body register: Register): TokenResponse

    @POST("/auth/login")
    suspend fun login(@Body login: Login): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): TokenResponse

    @POST("observe/device-token")
    suspend fun saveDeviceToken(@Body request: DeviceTokenRequest): Map<String, String>

    @POST("push/missed-intake")
    suspend fun notifyMissedIntake(@Body request: MissedIntakeNotification): Map<String, String>

    @POST("observe/invite")
    suspend fun inviteObserver(): InviteObserverResponse

    @POST("observe/accept")
    suspend fun acceptInvite(@Body request: InvitationAcceptRequest): Map<String, String>

    @GET("observe/observers")
    suspend fun getObservers(): List<UserResponse>

    @GET("observe/patients")
    suspend fun getPatients(): List<UserResponse>

    @DELETE("observe/link/{patient_id}/{observer_id}")
    suspend fun removeObserver(@Path("patient_id") patientId: String, @Path("observer_id") observerId: String): Map<String, String>
}