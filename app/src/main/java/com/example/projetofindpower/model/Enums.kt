package com.example.projetofindpower.model

enum class TipoMovimentacao {
    DESPESA, RECEITA, POUPANCA
}

enum class StatusPagamento {
    PAGO, PENDENTE, PARCIAL
}

enum class TipoRecomendacao {
    ORCAMENTO, POUPANCA, INVESTIMENTO, ALERTA
}

enum class ModoPagamento {
    DINHEIRO, CARTAO, TRANSFERENCIA, MB_WAY, OUTRO
}

enum class EstadoConfirmacao {
    CONFIRMADO, REJEITADO, PENDENTE
}

enum class Categoria {
    LAZER, EMERGENCIA, CONTAS_FIXAS, POUPANCA, EXTRAS, VIAGENS,
    SALARIO, INVESTIMENTO, PRESENTE, VENDA, OUTROS
}
