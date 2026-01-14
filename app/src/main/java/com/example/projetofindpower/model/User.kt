package com.example.projetofindpower.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utilizadores")
data class User(
    @PrimaryKey
    val idUtilizador: String = "", // Usando String para compatibilidade com Firebase UID
    val nome: String = "",
    val email: String = "",
    val preferencias: String = "{}", // JSON em String para simplicidade no Room
    val lastLogin: Long = 0
)
