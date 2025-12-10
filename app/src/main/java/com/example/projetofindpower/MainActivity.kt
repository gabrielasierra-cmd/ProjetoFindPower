package com.example.projetofindpower

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels // Importa o delegado viewModels
import com.example.projetofindpower.controller.DespesaController // Assumindo que este é o seu ViewModel
import com.example.projetofindpower.view.ListaDespesasScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // CORREÇÃO: Injeta o ViewModel/Controller usando o delegado 'viewModels'
    // O Hilt saberá como fornecer o DespesaController
    private val viewModel: DespesaController by viewModels() // <-- Novo

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

        // REMOVIDO: A criação manual do repository e do viewModel (Controller)
        // val repository = DespesaRepository(DespesaDao(), DespesaApi())
        // val viewModel = DespesaController(repository)

        // Encontra o container Compose dentro do XML e injeta a tela
        val composeView = findViewById<ComposeView>(R.id.composeContainer)
        composeView.setContent {
            // Usa o ViewModel injetado
            ListaDespesasScreen(viewModel)
        }
    }
}