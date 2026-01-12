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

        // Configuração para layout edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Tenta recuperar dados do usuário para validar a sessão
        fetchUserData()

        // Inicialização dos componentes da UI
        val btnNovaDespesa = findViewById<Button>(R.id.btnNovaDespesa)
        val btnDigitalizar = findViewById<Button>(R.id.btnDigitalizar)
        val btnCategorias = findViewById<Button>(R.id.btnCategorias)
        val btnRelatorios = findViewById<Button>(R.id.btnRelatorios)
        val btnPartilhar = findViewById<Button>(R.id.btnPartilhar)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Configuração das ações dos botões
        btnNovaDespesa.setOnClickListener {
            // TODO: Criar e abrir a tela de cadastro de despesas
            // Por enquanto, apenas confirmamos o clique
            Toast.makeText(this, "Funcionalidade: Nova Despesa", Toast.LENGTH_SHORT).show()
            // Exemplo: startActivity(Intent(this, NovaDespesaActivity::class.java))
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

        btnNovaDespesa.setOnClickListener {
            val intent = Intent(this, NovaDespesaActivity::class.java)
            startActivity(intent)
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
            }.addOnFailureListener {
                Toast.makeText(this, "Erro ao conectar com o servidor", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Se não houver usuário logado, redireciona para o login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
