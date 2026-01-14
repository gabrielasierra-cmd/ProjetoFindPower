package com.example.projetofindpower.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTipoMovimentacao(value: TipoMovimentacao) = value.name

    @TypeConverter
    fun toTipoMovimentacao(value: String): TipoMovimentacao {
        return try {
            enumValueOf<TipoMovimentacao>(value)
        } catch (e: Exception) {
            TipoMovimentacao.DESPESA
        }
    }

    @TypeConverter
    fun fromStatusPagamento(value: StatusPagamento) = value.name

    @TypeConverter
    fun toStatusPagamento(value: String): StatusPagamento {
        return try {
            enumValueOf<StatusPagamento>(value)
        } catch (e: Exception) {
            StatusPagamento.PENDENTE
        }
    }

    @TypeConverter
    fun fromCategoria(value: Categoria) = value.name

    @TypeConverter
    fun toCategoria(value: String): Categoria {
        return when (value.uppercase()) {
            "CONTAS", "CONTAS FIXAS", "CONTAS_FIXAS" -> Categoria.CONTAS_FIXAS
            "LAZER" -> Categoria.LAZER
            "EMERGENCIA", "EMERGÊNCIA" -> Categoria.EMERGENCIA
            "POUPANÇA", "POUPANCA" -> Categoria.POUPANCA
            "EXTRAS" -> Categoria.EXTRAS
            "VIAGENS" -> Categoria.VIAGENS
            "SALÁRIO", "SALARIO" -> Categoria.SALARIO
            "INVESTIMENTO" -> Categoria.INVESTIMENTO
            "PRESENTE" -> Categoria.PRESENTE
            "VENDA" -> Categoria.VENDA
            else -> Categoria.OUTROS
        }
    }

    @TypeConverter
    fun fromTipoRecomendacao(value: TipoRecomendacao) = value.name

    @TypeConverter
    fun toTipoRecomendacao(value: String) = try { enumValueOf<TipoRecomendacao>(value) } catch(e: Exception) { TipoRecomendacao.ALERTA }

    @TypeConverter
    fun fromEstadoConfirmacao(value: EstadoConfirmacao) = value.name

    @TypeConverter
    fun toEstadoConfirmacao(value: String) = try { enumValueOf<EstadoConfirmacao>(value) } catch(e: Exception) { EstadoConfirmacao.PENDENTE }
}
