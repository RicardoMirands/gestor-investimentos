package gestor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate

private fun num(x: Double, casas: Int = 2) = "%.${casas}f".format(x)

@Composable
private fun RowScope.Celula(
    texto: String,
    peso: Float,
    negrito: Boolean = false,
    direita: Boolean = true
) {
    Text(
        text = texto,
        modifier = Modifier.weight(peso),
        fontWeight = if (negrito) FontWeight.Bold else FontWeight.Normal,
        textAlign = if (direita) TextAlign.End else TextAlign.Start
    )
}

@Composable
fun TelaCarteira(repo: Repositorio) {
    var linhas by remember { mutableStateOf(repo.carteira()) }
    var realizado by remember {
        mutableStateOf(calcularPosicoes(repo.listar()).values.sumOf { it.realizado })
    }

    fun atualizar() {
        linhas = repo.carteira()
        realizado = calcularPosicoes(repo.listar()).values.sumOf { it.realizado }
    }

    val total = linhas.sumOf { it.valor }
    val naoRealizado = linhas.sumOf { it.lucro }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Carteira", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            Celula("Ativo", 1.2f, negrito = true, direita = false)
            Celula("Classe", 1f, negrito = true, direita = false)
            Celula("Qtd", 1f, negrito = true)
            Celula("Médio", 1f, negrito = true)
            Celula("Atual", 1f, negrito = true)
            Celula("Valor", 1.2f, negrito = true)
            Celula("L/P %", 0.8f, negrito = true)
            Celula("Peso %", 0.8f, negrito = true)
        }
        HorizontalDivider()

        LazyColumn(Modifier.weight(1f)) {
            items(linhas) { l ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Celula(l.posicao.ativo, 1.2f, direita = false)
                    Celula(l.posicao.classe, 1f, direita = false)
                    Celula(num(l.posicao.qtd, 4), 1f)
                    Celula(num(l.posicao.medio), 1f)
                    Celula(num(l.precoAtual), 1f)
                    Celula(num(l.valor), 1.2f)
                    Celula(num(l.lucroPct, 1), 0.8f)
                    Celula(if (total > 0) num(100 * l.valor / total, 1) else "-", 0.8f)
                }
            }
        }

        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        Text("Total da carteira: ${num(total)}", fontWeight = FontWeight.Bold)
        Text("L/P não realizado: ${num(naoRealizado)}")
        Text("L/P realizado: ${num(realizado)}")
        Text(
            "Alocação: " + pesoPorClasse(linhas).entries
                .joinToString("   ") { "${it.key} ${num(it.value, 1)}%" }
        )

        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            val h = LocalDate.now()
            repo.adicionar(Transacao(h.minusDays(30), "VWCE", "etf", Tipo.COMPRA, 10.0, 100.0))
            repo.adicionar(Transacao(h.minusDays(20), "VWCE", "etf", Tipo.COMPRA, 10.0, 120.0))
            repo.adicionar(Transacao(h.minusDays(10), "BTC", "cripto", Tipo.COMPRA, 0.01, 60000.0))
            repo.adicionar(Transacao(h.minusDays(5), "DP_BANCO", "deposito", Tipo.COMPRA, 500.0, 1.0))
            repo.definirPreco("VWCE", 125.0)
            repo.definirPreco("BTC", 65000.0)
            atualizar()
        }) {
            Text("Carregar dados de exemplo")
        }
    }
}
