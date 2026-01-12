package com.example.projetofindpower.repository

import com.example.projetofindpower.model.User // IMPORTANTE: Importe sua classe nova
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class InvalidPasswordException(message: String) : Exception(message)
class InvalidCredentialsException(message: String) : Exception(message)
class EmailAlreadyInUseException(message: String) : Exception(message)

/**
 * Repository responsible for Authentication and Data operations.
 */
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    private val database = FirebaseDatabase.getInstance().reference
    private val MIN_PASSWORD_LENGTH = 6

    /**
     * Saves or updates basic user data in the Realtime Database using the Domain Model.
     */
    suspend fun saveUserData(uid: String, email: String) {
        // Criando o objeto de domínio User
        val user = User(
            uid = uid,
            email = email,
            lastLogin = System.currentTimeMillis()
        )

        try {
            // O Firebase converte o objeto 'user' automaticamente para a estrutura que você viu
            database.child("users").child(uid).setValue(user).await()
        } catch (e: Exception) {
            throw e
        }
    }

    // --- MÉTODOS DE AUTENTICAÇÃO (MANTIDOS) ---

    suspend fun createUser(email: String, senha: String): FirebaseUser {
        if (senha.length < MIN_PASSWORD_LENGTH) {
            throw InvalidPasswordException("A senha deve ter pelo menos $MIN_PASSWORD_LENGTH caracteres.")
        }

        return suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener { authResult ->
                    continuation.resume(authResult.user!!)
                }
                .addOnFailureListener { exception ->
                    if (exception is FirebaseAuthUserCollisionException) {
                        continuation.resumeWithException(
                            EmailAlreadyInUseException("O e-mail ${email} já está cadastrado.")
                        )
                    } else {
                        continuation.resumeWithException(exception)
                    }
                }
        }
    }

    suspend fun signIn(email: String, senha: String): FirebaseUser =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener { authResult ->
                    continuation.resume(authResult.user!!)
                }
                .addOnFailureListener { exception ->
                    if (exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                        continuation.resumeWithException(
                            InvalidCredentialsException("E-mail ou senha incorretos.")
                        )
                    } else {
                        continuation.resumeWithException(exception)
                    }
                }
        }

    suspend fun signInWithGoogle(idToken: String): FirebaseUser =
        suspendCancellableCoroutine { continuation ->
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    continuation.resume(authResult.user!!)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
