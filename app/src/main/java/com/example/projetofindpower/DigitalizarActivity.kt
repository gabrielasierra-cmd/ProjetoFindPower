package com.example.projetofindpower

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.controller.MovimentacaoController
import com.example.projetofindpower.model.*
import com.example.projetofindpower.network.GeminiRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class DigitalizarActivity : AppCompatActivity() {

    @Inject
    lateinit var geminiRepository: GeminiRepository

    @Inject
    lateinit var controller: MovimentacaoController

    private lateinit var imgPreview: ImageView
    private lateinit var btnAbrirFicheiro: Button
    private lateinit var txtStatusIA: TextView
    private lateinit var progressIA: ProgressBar
    private lateinit var pickerLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_digitalizar)

        configurarEdgeToEdge()
        inicializarUI()

        // Launcher para escolher Imagem ou PDF
        pickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                processarDocumento(it)
            }
        }

        btnAbrirFicheiro.setOnClickListener {
            // Abre seletor para Imagens e PDFs
            pickerLauncher.launch(arrayOf("image/*", "application/pdf"))
        }
    }

    private fun processarDocumento(uri: Uri) {
        val mimeType = contentResolver.getType(uri)
        
        lifecycleScope.launch {
            try {
                exibirCarregamento(true)
                txtStatusIA.text = "A IA está a analisar o documento... ✨"

                val resultado = if (mimeType == "application/pdf") {
                    val pdfBytes = contentResolver.openInputStream(uri)?.readBytes()
                    if (pdfBytes != null) geminiRepository.analisarPDF(pdfBytes) else null
                } else {
                    val bitmap = uriToBitmap(uri)
                    if (bitmap != null) {
                        imgPreview.setImageBitmap(bitmap)
                        geminiRepository.analisarRecibo(bitmap)
                    } else null
                }
                
                if (resultado != null && resultado.contains(";") && !resultado.contains("ERRO")) {
                    salvarDespesaIA(resultado)
                } else {
                    txtStatusIA.text = "Não consegui ler o documento. Tenta novamente."
                }
            } catch (e: Exception) {
                Log.e("IA_ERRO", "Erro: ${e.message}")
                txtStatusIA.text = "Erro ao processar ficheiro."
            } finally {
                exibirCarregamento(false)
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            Log.e("IA_ERRO", "Erro converter URI: ${e.message}")
            null
        }
    }

    private fun configurarEdgeToEdge() {
        val mainView = findViewById<View>(R.id.main)
        mainView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    private fun inicializarUI() {
        imgPreview = findViewById(R.id.imgPreview)
        btnAbrirFicheiro = findViewById(R.id.btnAbrirGaleria) // Reutilizando o ID
        btnAbrirFicheiro.text = "Escolher Imagem ou PDF 📄"
        txtStatusIA = findViewById(R.id.txtStatusIA)
        progressIA = findViewById(R.id.progressIA)
    }

    private suspend fun salvarDespesaIA(dados: String) {
        try {
            val partes = dados.split(";")
            if (partes.size < 3) return

            val descricao = partes[0].trim()
            val valor = partes[1].trim().replace(",", ".").toDoubleOrNull() ?: 0.0
            val categoriaNome = partes[2].trim().uppercase()
            
            val categoria = try { 
                Categoria.valueOf(categoriaNome) 
            } catch (e: Exception) { 
                Categoria.OUTROS 
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            
            val novaMov = Movimentacao(
                idMovimentacao = UUID.randomUUID().toString(),
                idUtilizador = uid,
                descricao = "IA: $descricao",
                valor = valor,
                tipo = TipoMovimentacao.DESPESA,
                categoria = categoria,
                data = System.currentTimeMillis().toString(),
                modoPagamento = "Digital",
                statusPagamento = StatusPagamento.PAGO
            )

            controller.salvar(novaMov)
            txtStatusIA.text = "Sucesso! €$valor salvos em $categoriaNome ✅"
            Toast.makeText(this, "Documento processado com sucesso!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e("IA_ERRO", "Erro ao salvar: ${e.message}")
            txtStatusIA.text = "Erro ao guardar os dados."
        }
    }

    private fun exibirCarregamento(mostrar: Boolean) {
        progressIA.visibility = if (mostrar) View.VISIBLE else View.GONE
        btnAbrirFicheiro.isEnabled = !mostrar
    }
}
