package com.example.data.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class LoginRequest(val email: String, val password: String)

@JsonClass(generateAdapter = true)
data class RegisterRequest(val email: String, val password: String, val name: String)

@JsonClass(generateAdapter = true)
data class AuthResponse(val access_token: String, val token_type: String, val email: String, val name: String)

@JsonClass(generateAdapter = true)
data class BackendTaskSync(
    val id: Long,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val isArchived: Boolean,
    val dueDate: Long,
    val priority: Int,
    val category: String,
    val recurrence: String,
    val subtasksRaw: String
)

@JsonClass(generateAdapter = true)
data class SyncResponse(val status: String, val message: String)

interface BackendApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/tasks/sync")
    suspend fun syncTasks(
        @Header("Authorization") token: String,
        @Body tasks: List<BackendTaskSync>
    ): SyncResponse
}

object BackendRetrofitClient {
    // 10.0.2.2 is the standard Android loopback IP addressing the host machine running FastAPI
    private var baseUrl = "http://10.0.2.2:8000/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: BackendApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BackendApiService::class.java)
    }

    fun updateBaseUrl(newUrl: String) {
        baseUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        // We reinitialize or just let users know
    }
}
