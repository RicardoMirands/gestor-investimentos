package gestor

import java.io.File
import java.time.LocalDate

fun main() {
    File("teste.db").delete()                 // começa sempre do zero neste teste

    Repositorio("teste.db").use { repo ->
        repo.adicionar(Transacao(LocalDate.of(2026, 10, 1), "vwce", "etf", Tipo.COMPRA, 10.0, 100.0))
        repo.adicionar(Transacao(LocalDate.of(2026, 10, 10), "VWCE", "etf", Tipo.COMPRA, 10.0, 120.0))
        repo.definirPreco("VWCE", 125.0)

        runCatching {
            repo.adicionar(Transacao(LocalDate.of(2026, 10, 15), "VWCE", "etf", Tipo.VENDA, 100.0, 125.0))
        }.onFailure { println("Recusado: ${it.message}") }

        for (l in repo.carteira()) {
            println(
                "${l.posicao.ativo}: qtd=${l.posicao.qtd}, médio=${l.posicao.medio}, " +
                "valor=${l.valor}, L/P=${"%.1f".format(l.lucroPct)}%"
            )
        }
    }
}
