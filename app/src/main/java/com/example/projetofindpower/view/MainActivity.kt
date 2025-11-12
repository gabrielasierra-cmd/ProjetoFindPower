package com.example.projetofindpower.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetofindpower.R
import com.example.projetofindpower.controller.DespesaController
import com.example.projetofindpower.model.DespesaApi
import com.example.projetofindpower.model.DespesaDao
import com.example.projetofindpower.repository.DespesaRepository

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Ajuste das margens do sistema (padrão Android Studio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Criação do repositório e do ViewModel
        val repository = DespesaRepository(DespesaDao(), DespesaApi())
        val viewModel = DespesaController(repository)

        // Encontra o container Compose dentro do XML e injeta a tela
        val composeView = findViewById<ComposeView>(R.id.composeContainer)
        composeView.setContent {
            ListaDespesasScreen(viewModel)
        }
    }
}