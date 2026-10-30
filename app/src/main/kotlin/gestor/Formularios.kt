package gestor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/** Aceita vírgula ou ponto como separador decimal. */
private fun lerNumero(s: String): Double? = s.trim().replace(',', '.').toDoubleOrNull()

@Composable
private fun Opcao(texto: String, selecionada: Boolean, aoEscolher: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selecionada, onClick = aoEscolher)
        Text(texto)
        Spacer(Modifier.width(12.dp))
    }
}

@Composable
fun TelaNovaTransacao(repo: Repositorio) {
    var tipo by remember { mutableStateOf(Tipo.COMPRA) }
    var classe by remember { mutableStateOf("etf") }
    var data by remember { mutableStateOf(LocalDate.now().toString()) }
    var ativo by remember { mutableStateOf("") }
    var qtd by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var taxa by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Nova transação", style = MaterialTheme.typography.headlineMedium)

        Row {
            Opcao("Compra", tipo == Tipo.COMPRA) { tipo = Tipo.COMPRA }
            Opcao("Venda", tipo == Tipo.VENDA) { tipo = Tipo.VENDA }
        }
        Row {
            for (c in listOf("acao", "etf", "fundo", "cripto", "deposito"))
                Opcao(c, classe == c) { classe = c }
        }

        OutlinedTextField(
            value = data, onValueChange = { data = it },
            label = { Text("Data (AAAA-MM-DD)") }, singleLine = true
        )
        OutlinedTextField(
            value = ativo, onValueChange = { ativo = it },
            label = { Text("Ativo (ex.: VWCE)") }, singleLine = true
        )
        OutlinedTextField(
            value = qtd, onValueChange = { qtd = it },
            label = { Text("Quantidade") }, singleLine = true
        )
        OutlinedTextField(
            value = preco, onValueChange = { preco = it },
            label = { Text("Preço por unidade") }, singleLine = true
        )
        OutlinedTextField(
            value = taxa, onValueChange = { taxa = it },
            label = { Text("Taxa (opcional)") }, singleLine = true
        )

        Button(onClick = {
            val d = runCatching { LocalDate.parse(data.trim()) }.getOrNull()
            val q = lerNumero(qtd)
            val p = lerNumero(preco)
            val tx = if (taxa.isBlank()) 0.0 else lerNumero(taxa)
            mensagem = when {
                d == null -> "Data inválida (use AAAA-MM-DD)."
                ativo.isBlank() -> "Indique o ativo."
                q == null || q <= 0 -> "Quantidade inválida."
                p == null || p < 0 -> "Preço inválido."
                tx == null || tx < 0 -> "Taxa inválida."
                else -> try {
                    repo.adicionar(Transacao(d, ativo, classe, tipo, q, p, tx))
                    ativo = ""; qtd = ""; preco = ""; taxa = ""
                    "Transação registada."
                } catch (e: Exception) {
                    e.message ?: "Erro ao registar."
                }
            }
        }) {
            Text("Registar")
        }

        if (mensagem.isNotEmpty()) Text(mensagem)
    }
}

@Composable
fun TelaPrecos(repo: Repositorio) {
    var precos by remember { mutableStateOf(repo.precos()) }
    var ativo by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }
    val abertos = remember {
        calcularPosicoes(repo.listar()).values.filter { it.qtd > 1e-12 }.map { it.ativo }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Preços atuais", style = MaterialTheme.typography.headlineMedium)
        Text("Clica num ativo para o preencher:")

        for (a in abertos) {
            val pr = precos[a]
            Text(
                text = "$a: " + (if (pr != null) "preço atual $pr" else "sem preço (usa o custo médio)"),
                modifier = Modifier.clickable { ativo = a }.padding(vertical = 4.dp)
            )
        }

        OutlinedTextField(
            value = ativo, onValueChange = { ativo = it },
            label = { Text("Ativo") }, singleLine = true
        )
        OutlinedTextField(
            value = preco, onValueChange = { preco = it },
            label = { Text("Preço atual") }, singleLine = true
        )

        Button(onClick = {
            val p = lerNumero(preco)
            mensagem = when {
                ativo.isBlank() -> "Indique o ativo."
                p == null || p < 0 -> "Preço inválido."
                else -> {
                    repo.definirPreco(ativo, p)
                    precos = repo.precos()
                    preco = ""
                    "Preço guardado."
                }
            }
        }) {
            Text("Guardar preço")
        }

        if (mensagem.isNotEmpty()) Text(mensagem)
    }
}
