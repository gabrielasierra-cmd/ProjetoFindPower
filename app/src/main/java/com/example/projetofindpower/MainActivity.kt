package com.example.projetofindpower.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels // Importa o delegado viewModels
import com.example.projetofindpower.controller.DespesaController // Assumindo que este é o seu ViewModel
import com.example.projetofindpower.view.ListaDespesasScreen
import dagger.hilt.android.AndroidEntryPoint
import com.example.projetofindpower.CategoriasActivity
import com.example.projetofindpower.DigitalizarActivity
import com.example.projetofindpower.PartilhasActivity
import com.example.projetofindpower.R
import com.example.projetofindpower.RelatoriosActivity

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnDigitalizar = findViewById<Button>(R.id.btnDigitalizar)
        val btnCategorias = findViewById<Button>(R.id.btnCategorias)
        val btnRelatorios = findViewById<Button>(R.id.btnRelatorios)
        val btnPartilhar = findViewById<Button>(R.id.btnPartilhar)

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
    }
}
