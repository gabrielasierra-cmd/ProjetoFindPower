package com.example.projetofindpower.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {
    @POST
    suspend fun enviarParaPlanilha(
        @Url url: String,
        @Body dados: List<MovimentacaoExport>
    ): Response<Unit>

    @POST
    suspend fun gerarRespostaGemini(
        @Url url: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
