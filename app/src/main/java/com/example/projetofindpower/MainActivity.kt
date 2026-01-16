package com.example.projetofindpower

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.projetofindpower.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Definir o launcher como propriedade da classe para poder ser usado no onCreate
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM_DEBUG", "Permissão concedida. Capturando token...")
            obterEGuardarTokenFCM()
        } else {
            Toast.makeText(this, "Notificações desativadas. Ative nas definições.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Pedir permissão (se necessário)
        pedirPermissaoNotificacao()
        
        // 2. Tentar capturar o token logo na abertura (caso já tenha permissão)
        obterEGuardarTokenFCM()

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnNovaDespesa.setOnClickListener {
            startActivity(Intent(this, NovaMovimentacaoActivity::class.java))
        }

        binding.btnCategorias.setOnClickListener {
            startActivity(Intent(this, CategoriasActivity::class.java))
        }

        binding.btnRelatorios.setOnClickListener {
            startActivity(Intent(this, RelatoriosActivity::class.java))
        }

        binding.btnPartilhar.setOnClickListener {
            startActivity(Intent(this, PartilhasActivity::class.java))
        }

        binding.btnDigitalizar.setOnClickListener {
            startActivity(Intent(this, DigitalizarActivity::class.java))
        }

        binding.btnMentoria.setOnClickListener {
            startActivity(Intent(this, MentoriaActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun pedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun obterEGuardarTokenFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM_DEBUG", "Erro ao obter token", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (uid != null && token != null) {
                Log.d("FCM_DEBUG", "Token obtido: $token")
                // Guardar no Firebase Realtime Database
                FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .child("fcmToken")
                    .setValue(token)
                    .addOnSuccessListener {
                        Log.d("FCM_DEBUG", "✅ Token guardado com sucesso na base de dados!")
                    }
                    .addOnFailureListener {
                        Log.e("FCM_DEBUG", "❌ Erro ao guardar token: ${it.message}")
                    }
            }
        }
    }
}
