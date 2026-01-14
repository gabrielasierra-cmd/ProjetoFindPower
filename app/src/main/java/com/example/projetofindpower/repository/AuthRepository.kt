package com.example.projetofindpower.repository

import com.example.projetofindpower.model.User
import com.example.projetofindpower.model.UserDao
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

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val userDao: UserDao // Adicionado UserDao para persistência local
) {

    private val database = FirebaseDatabase.getInstance().reference

    suspend fun saveUserData(uid: String, email: String, nome: String = "") {
        val user = User(
            idUtilizador = uid,
            nome = nome,
            email = email,
            lastLogin = System.currentTimeMillis()
        )

        try {
            // Sincroniza Local (Room)
            userDao.salvar(user)
            // Sincroniza Nuvem (Firebase)
            database.child("users").child(uid).setValue(user).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun createUser(email: String, senha: String): FirebaseUser {
        return suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener { authResult -> continuation.resume(authResult.user!!) }
                .addOnFailureListener { e -> 
                    if (e is FirebaseAuthUserCollisionException) {
                        continuation.resumeWithException(EmailAlreadyInUseException("E-mail já cadastrado."))
                    } else {
                        continuation.resumeWithException(e)
                    }
                }
        }
    }

    suspend fun signIn(email: String, senha: String): FirebaseUser =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener { authResult -> continuation.resume(authResult.user!!) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }

    suspend fun signInWithGoogle(idToken: String): FirebaseUser =
        suspendCancellableCoroutine { continuation ->
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult -> continuation.resume(authResult.user!!) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }

    fun signOut() { auth.signOut() }
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}
