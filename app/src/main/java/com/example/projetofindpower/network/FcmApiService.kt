package com.example.projetofindpower.network

import com.example.projetofindpower.BuildConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmApiService {
    // Agora o ID do projeto é lido de forma segura do BuildConfig
    @POST("v1/projects/${BuildConfig.FIREBASE_PROJECT_ID}/messages:send")
    suspend fun sendPushNotification(
        @Header("Authorization") authHeader: String,
        @Body request: FcmRequest
    ): Response<Unit>
}
