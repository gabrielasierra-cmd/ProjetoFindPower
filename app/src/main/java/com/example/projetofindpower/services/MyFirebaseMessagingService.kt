package com.example.projetofindpower.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Lógica de exibição mantida
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_DEBUG", "Novo Token Gerado: $token")
        guardarTokenNoFirebase(token)
    }

    private fun guardarTokenNoFirebase(token: String?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null && token != null) {
            // CORREÇÃO: Mudado de "usuarios" para "users" para coincidir com a tua DB
            val ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("fcmToken")
            ref.setValue(token)
                .addOnSuccessListener { Log.d("FCM_DEBUG", "Token guardado em /users/$uid/fcmToken") }
                .addOnFailureListener { Log.e("FCM_DEBUG", "Erro ao guardar token: ${it.message}") }
        }
    }
}
