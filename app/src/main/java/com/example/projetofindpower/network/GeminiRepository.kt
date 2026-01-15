package com.example.projetofindpower.network

import android.util.Log
import com.example.projetofindpower.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor() {

    private val TAG = "GEMINI_REPO"

    private val cleanApiKey = BuildConfig.GEMINI_API_KEY
        .replace("\"", "")
        .replace("'", "")
        .replace(" ", "")
        .trim()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash-exp",
        apiKey = cleanApiKey,
        requestOptions = RequestOptions(apiVersion = "v1beta")
    )

    /**
     * Envia uma pergunta genérica para o Gemini 2.0
     */
    suspend fun perguntarIA(mensagem: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(mensagem)
            response.text
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao perguntar: ${e.message}")
            null
        }
    }

    suspend fun testarChave(): String = withContext(Dispatchers.IO) {
        if (cleanApiKey.isEmpty()) return@withContext "ERRO: Chave vazia."
        try {
            val response = generativeModel.generateContent("Olá Gemini 2.0!")
            "CHAVE_OK"
        } catch (e: Exception) {
            Log.e(TAG, "ERRO COM 2.0: ${e.message}")
            "ERRO: ${e.message}"
        }
    }

    suspend fun gerarConselhos(receitas: Double, despesas: Double, saldo: Double): String? = withContext(Dispatchers.IO) {
        try {
            val prompt = "Com base num saldo de €$saldo, dá-me um conselho financeiro curto em Português de Portugal."
            val response = generativeModel.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            Log.e(TAG, "Erro: ${e.message}")
            null
        }
    }
}
