package com.example.projetofindpower.network

import android.graphics.Bitmap
import android.util.Log
import com.example.projetofindpower.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
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

    private val promptPadrao = """
        Analisa este recibo, fatura ou documento.
        Extrai apenas estas 3 informações separadas por ponto e vírgula (;):
        1. Descrição curta (ex: Continente, Almoço, Gasolina)
        2. Valor numérico (usa ponto para decimais, ex: 15.50)
        3. Categoria (Escolha APENAS uma destas: ALIMENTACAO, TRANSPORTE, SAUDE, LAZER, EDUCACAO, OUTROS)
        
        Exemplo de resposta esperada: Pingo Doce;22.45;ALIMENTACAO
        Se não encontrares os dados, responde apenas: ERRO
    """.trimIndent()

    suspend fun analisarRecibo(imagem: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val inputContent = content {
                image(imagem)
                text(promptPadrao)
            }
            val response = generativeModel.generateContent(inputContent)
            response.text
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao analisar imagem: ${e.message}")
            null
        }
    }

    suspend fun analisarPDF(pdfBytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val inputContent = content {
                blob("application/pdf", pdfBytes)
                text(promptPadrao)
            }
            val response = generativeModel.generateContent(inputContent)
            response.text
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao analisar PDF: ${e.message}")
            null
        }
    }

    suspend fun perguntarIA(mensagem: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(mensagem)
            response.text
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao perguntar: ${e.message}")
            null
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
