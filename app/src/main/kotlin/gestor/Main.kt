package gestor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
fun App(repo: Repositorio) {
    var aba by remember { mutableStateOf(0) }
    val titulos = listOf("Carteira", "Nova transação", "Preços", "Histórico")

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            titulos.forEachIndexed { i, t ->
                if (aba == i) Button(onClick = { aba = i }) { Text(t) }
                else OutlinedButton(onClick = { aba = i }) { Text(t) }
            }
        }
        HorizontalDivider()

        // Ao trocar de separador a tela é recriada, por isso volta a ler os dados atuais.
        when (aba) {
            0 -> TelaCarteira(repo)
            1 -> TelaNovaTransacao(repo)
            2 -> TelaPrecos(repo)
            else -> TelaHistorico(repo)
        }
    }
}

fun main() = application {
    val pasta = java.io.File(System.getProperty("user.home"), ".gestor-investimentos").apply { mkdirs() }
    val repo = remember { Repositorio(java.io.File(pasta, "investimentos.db").path) }
    Window(
        onCloseRequest = {
            repo.close()
            exitApplication()
        },
        title = "Gestor de Investimentos"
    ) {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) { App(repo) }
        }
    }
}
