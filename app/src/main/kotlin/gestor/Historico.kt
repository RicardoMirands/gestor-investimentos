package gestor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TelaHistorico(repo: Repositorio) {
    var itens by remember { mutableStateOf(repo.listarComId().reversed()) }  // mais recentes primeiro
    var mensagem by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Histórico", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        if (mensagem.isNotEmpty()) {
            Text(mensagem)
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(Modifier.weight(1f)) {
            items(itens, key = { it.first }) { (id, t) ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${t.data}  ${t.tipo.name.lowercase()}  ${t.ativo} (${t.classe})  " +
                            "qtd ${t.qtd}  preço ${t.preco}  taxa ${t.taxa}",
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        mensagem = try {
                            repo.apagar(id)
                            itens = repo.listarComId().reversed()
                            "Transação apagada."
                        } catch (e: Exception) {
                            e.message ?: "Erro ao apagar."
                        }
                    }) {
                        Text("Apagar")
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
