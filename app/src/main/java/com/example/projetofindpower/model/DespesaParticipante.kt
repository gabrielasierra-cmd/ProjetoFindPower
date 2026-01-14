package com.example.projetofindpower.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "despesa_participante",
    primaryKeys = ["idDespesa", "idParticipante"],
    foreignKeys = [
        ForeignKey(
            entity = Movimentacao::class,
            parentColumns = ["idMovimentacao"],
            childColumns = ["idDespesa"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Participante::class,
            parentColumns = ["idParticipante"],
            childColumns = ["idParticipante"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idDespesa"), Index("idParticipante")]
)
data class DespesaParticipante(
    val idDespesa: String,
    val idParticipante: String,
    val valorDevido: Double = 0.0,
    val estadoConfirmacao: String = "Pendente" // Confirmado, Rejeitado, Pendente
)
