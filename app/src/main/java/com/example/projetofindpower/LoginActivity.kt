package com.example.projetofindpower

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.projetofindpower.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

// Marca a Activity para injeção de dependência via Hilt
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    // Injeção de dependência do Repositório (Hilt)
    @Inject
    lateinit var authRepository: AuthRepository

    // Variáveis da UI
    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var googleSignInButton: SignInButton

    // Cliente para configurar e iniciar o fluxo do Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient

    // Launcher para receber o resultado do fluxo do Google Sign-In
    private val googleSignInLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    // Autentica no Firebase com o token do Google
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Toast.makeText(this, "Falha no Login Google: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Se já estiver logado, navega diretamente para a Home
        if (authRepository.getCurrentUser() != null) {
            navegarParaHome()
            return
        }

        inicializarViews()
        configurarGoogleSignIn()

        // Configurações EdgeToEdge (pode ser necessário ou não dependendo do seu layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Definir os Listeners de Ação
        loginButton.setOnClickListener {
            fazerLogin(emailEditText.text.toString(), senhaEditText.text.toString())
        }

        registerButton.setOnClickListener {
            fazerRegistro(emailEditText.text.toString(), senhaEditText.text.toString())
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    // --- FUNÇÕES DE INICIALIZAÇÃO ---

    private fun inicializarViews() {
        emailEditText = findViewById(R.id.email_edit_text)
        senhaEditText = findViewById(R.id.senha_edit_text)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)
        googleSignInButton = findViewById(R.id.google_sign_in_button)
    }

    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    // --- FUNÇÕES DE AUTENTICAÇÃO E COROUTINES ---

    private fun fazerLogin(email: String, senha: String) {
        if (email.isBlank() || senha.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val user = authRepository.signIn(email, senha)
                Toast.makeText(this@LoginActivity, "Login de ${user.email} OK!", Toast.LENGTH_LONG).show()
                navegarParaHome()
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Falha na autenticação."
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fazerRegistro(email: String, senha: String) {
        if (email.isBlank() || senha.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val user = authRepository.createUser(email, senha)
                Toast.makeText(this@LoginActivity, "Usuário ${user.email} criado com sucesso!", Toast.LENGTH_LONG).show()
                navegarParaHome()
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Falha ao criar conta."
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        lifecycleScope.launch {
            try {
                val user = authRepository.signInWithGoogle(idToken)
                Toast.makeText(this@LoginActivity, "Login Google de ${user.email} OK!", Toast.LENGTH_SHORT).show()
                navegarParaHome()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Erro na Autenticação Firebase com Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- FUNÇÃO DE NAVEGAÇÃO CORRIGIDA ---

    private fun navegarParaHome() {
        // CORREÇÃO FINAL: O Intent inicia a Activity de destino (FinPowerActivity)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}