package gestor

import java.time.LocalDate

enum class Tipo { COMPRA, VENDA }

data class Transacao(
    val data: LocalDate,
    val ativo: String,
    val classe: String,
    val tipo: Tipo,
    val qtd: Double,
    val preco: Double,
    val taxa: Double = 0.0
)

data class Posicao(
    val ativo: String,
    val classe: String,
    val qtd: Double = 0.0,
    val custo: Double = 0.0,        // custo total do que ainda tens
    val realizado: Double = 0.0     // lucro/prejuízo já realizado
) {
    val medio: Double get() = if (qtd > 0) custo / qtd else 0.0
}

fun calcularPosicoes(transacoes: List<Transacao>): Map<String, Posicao> {
    val mapa = LinkedHashMap<String, Posicao>()
    for (t in transacoes.sortedBy { it.data }) {      // sortedBy é estável
        val atual = mapa[t.ativo] ?: Posicao(t.ativo, t.classe)
        mapa[t.ativo] = when (t.tipo) {
            Tipo.COMPRA -> atual.copy(
                classe = t.classe,
                qtd = atual.qtd + t.qtd,
                custo = atual.custo + t.qtd * t.preco + t.taxa
            )
            Tipo.VENDA -> {
                require(t.qtd <= atual.qtd + 1e-12) {
                    "Venda maior que a posição em ${t.ativo}"
                }
                val medio = atual.medio               // o custo médio não muda na venda
                atual.copy(
                    qtd = atual.qtd - t.qtd,
                    custo = atual.custo - medio * t.qtd,
                    realizado = atual.realizado + t.qtd * (t.preco - medio) - t.taxa
                )
            }
        }
    }
    return mapa
}

data class Linha(val posicao: Posicao, val precoAtual: Double) {
    val valor: Double get() = posicao.qtd * precoAtual
    val lucro: Double get() = valor - posicao.custo
    val lucroPct: Double get() = if (posicao.custo > 0) 100 * lucro / posicao.custo else 0.0
}

fun pesoPorClasse(linhas: List<Linha>): Map<String, Double> {
    val total = linhas.sumOf { it.valor }
    if (total <= 0) return emptyMap()
    return linhas.groupBy { it.posicao.classe }
        .mapValues { (_, l) -> 100 * l.sumOf { it.valor } / total }
}
