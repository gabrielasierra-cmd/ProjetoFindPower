package com.example.projetofindpower

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.projetofindpower.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pedir permissão para notificações (Android 13+)
        pedirPermissaoNotificacao()

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
                val requestPermissionLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (!isGranted) {
                        Toast.makeText(this, "Notificações desativadas. Ative nas definições para receber alertas.", Toast.LENGTH_LONG).show()
                    }
                }
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
