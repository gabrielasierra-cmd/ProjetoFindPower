package com.example.projetofindpower.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class InvalidPasswordException(message: String) : Exception(message)
class InvalidCredentialsException(message: String) : Exception(message)
class EmailAlreadyInUseException(message: String) : Exception(message)

/**
 * Repositório (Model) responsável por todas as operações de Autenticação.
 * Ele usa o FirebaseAuth para interagir com o Firebase.
 */
class AuthRepository
@Inject constructor(
    private val auth: FirebaseAuth // Injetado pelo Hilt
) {

    // Constante para o requisito mínimo de senha
    private val MIN_PASSWORD_LENGTH = 6

    // --- MÉTODOS DE AUTENTICAÇÃO PADRÃO (EMAIL/SENHA) ---

    /**
     * Tenta criar um novo usuário com email e senha.
     * Inclui validação local para o tamanho da senha e captura erros específicos.
     * @return O objeto FirebaseUser recém-criado.
     * @throws InvalidPasswordException se a senha for muito curta.
     * @throws EmailAlreadyInUseException se o e-mail já estiver cadastrado.
     */
    suspend fun createUser(email: String, senha: String): FirebaseUser {
        // 1. VALIDAÇÃO LOCAL: Verifica o tamanho da senha
        if (senha.length < MIN_PASSWORD_LENGTH) {
            throw InvalidPasswordException("A senha deve ter pelo menos $MIN_PASSWORD_LENGTH caracteres.")
        }

        // 2. Continua com a requisição ao Firebase
        return suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener { authResult ->
                    // Sucesso: retorna o usuário
                    continuation.resume(authResult.user!!)
                }
                .addOnFailureListener { exception ->
                    // 3. TRATAMENTO DE ERROS DO FIREBASE:
                    if (exception is FirebaseAuthUserCollisionException) {
                        // E-mail já em uso
                        continuation.resumeWithException(
                            EmailAlreadyInUseException("O e-mail ${email} já está cadastrado.")
                        )
                    } else {
                        // Outros erros (ex: falha de rede)
                        continuation.resumeWithException(exception)
                    }
                }
        }
    }

    /**
     * Tenta fazer login com email e senha.
     * @return O objeto FirebaseUser logado.
     */


    // --- MÉTODOS DE AUTENTICAÇÃO COM GOOGLE ---



    /**
     * Tenta fazer login com email e senha.
     * @return O objeto FirebaseUser logado.
     * @throws InvalidCredentialsException se o e-mail/senha estiver incorreto.
     */
    suspend fun signIn(email: String, senha: String): FirebaseUser =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener { authResult ->
                    // Sucesso: retorna o usuário
                    continuation.resume(authResult.user!!)
                }
                .addOnFailureListener { exception ->
                    // --- NOVA LÓGICA DE TRATAMENTO DE ERRO AQUI ---

                    if (exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                        continuation.resumeWithException(
                            InvalidCredentialsException("E-mail ou senha incorretos.")
                        )
                    } else {
                        // Outros erros (ex: usuário desativado, rede)
                        continuation.resumeWithException(exception)
                    }
                }
        }
    // --- MÉTODOS DE STATUS E LOGOUT ---
    /**
     * Autentica no Firebase usando o token de ID do Google obtido na Activity.
     * @param idToken Token de ID fornecido pelo GoogleSignInClient.
     * @return O objeto FirebaseUser autenticado.
     */
    // Dentro da classe AuthRepository, método signIn
    suspend fun signInWithGoogle(idToken: String): FirebaseUser =
        suspendCancellableCoroutine { continuation ->
            // 1. Cria a credencial do Firebase a partir do token do Google
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            // 2. Autentica no Firebase com essa credencial
            auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    continuation.resume(authResult.user!!)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    /**
     * Faz logout do usuário atual do Firebase.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Retorna o usuário logado atualmente (ou null se ninguém estiver logado).
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}