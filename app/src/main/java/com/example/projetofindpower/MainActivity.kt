package com.example.projetofindpower

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetofindpower.repository.AuthRepository
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

        configurarCliqueBotoes()
    }

    private fun configurarCliqueBotoes() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnNovaDespesa).setOnClickListener {
            startActivity(Intent(this, NovaMovimentacaoActivity::class.java))
        }

        findViewById<Button>(R.id.btnDigitalizar).setOnClickListener {
            startActivity(Intent(this, DigitalizarActivity::class.java))
        }

        findViewById<Button>(R.id.btnCategorias).setOnClickListener {
            startActivity(Intent(this, CategoriasActivity::class.java))
        }

        findViewById<Button>(R.id.btnRelatorios).setOnClickListener {
            startActivity(Intent(this, RelatoriosActivity::class.java))
        }

        findViewById<Button>(R.id.btnPartilhar).setOnClickListener {
            startActivity(Intent(this, PartilhasActivity::class.java))
        }

        findViewById<Button>(R.id.btnMentoria).setOnClickListener {
            startActivity(Intent(this, MentoriaActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            authRepository.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
