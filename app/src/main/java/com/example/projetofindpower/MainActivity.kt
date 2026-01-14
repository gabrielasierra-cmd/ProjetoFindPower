package com.example.projetofindpower

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetofindpower.repository.AuthRepository
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchUserData()

        // O ID do botão no XML ainda é btnNovaDespesa, mas vamos mudar o rótulo visual
        val btnNovaMovimentacao = findViewById<Button>(R.id.btnNovaDespesa)
        val btnDigitalizar = findViewById<Button>(R.id.btnDigitalizar)
        val btnCategorias = findViewById<Button>(R.id.btnCategorias)
        val btnRelatorios = findViewById<Button>(R.id.btnRelatorios)
        val btnPartilhar = findViewById<Button>(R.id.btnPartilhar)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnNovaMovimentacao.text = "Nova Movimentação" // Muda o texto via código para garantir

        btnNovaMovimentacao.setOnClickListener {
            startActivity(Intent(this, NovaMovimentacaoActivity::class.java))
        }

        btnDigitalizar.setOnClickListener {
            startActivity(Intent(this, DigitalizarActivity::class.java))
        }

        btnCategorias.setOnClickListener {
            startActivity(Intent(this, CategoriasActivity::class.java))
        }

        btnRelatorios.setOnClickListener {
            startActivity(Intent(this, RelatoriosActivity::class.java))
        }

        btnPartilhar.setOnClickListener {
            startActivity(Intent(this, PartilhasActivity::class.java))
        }

        btnLogout.setOnClickListener {
            authRepository.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchUserData() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            val database = FirebaseDatabase.getInstance().reference
            database.child("users").child(currentUser.uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val email = snapshot.child("email").value.toString()
                    Toast.makeText(this, "Sessão ativa: $email", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
