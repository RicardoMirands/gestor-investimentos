package gestor

import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDate

class Repositorio(caminho: String = "investimentos.db") : AutoCloseable {
    private val conn: Connection = DriverManager.getConnection("jdbc:sqlite:$caminho")

    init {
        conn.createStatement().use { st ->
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS transacoes (
                    id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    data   TEXT NOT NULL,
                    ativo  TEXT NOT NULL,
                    classe TEXT NOT NULL,
                    tipo   TEXT NOT NULL,
                    qtd    REAL NOT NULL CHECK (qtd > 0),
                    preco  REAL NOT NULL,
                    taxa   REAL NOT NULL DEFAULT 0
                )
                """
            )
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS precos (
                    ativo TEXT PRIMARY KEY,
                    preco REAL NOT NULL,
                    data  TEXT NOT NULL
                )
                """
            )
        }
    }

    fun adicionar(t: Transacao) {
        val n = t.copy(ativo = t.ativo.trim().uppercase())
        calcularPosicoes(listar() + n)        // lança erro se a sequência ficar inválida
        conn.prepareStatement(
            "INSERT INTO transacoes(data,ativo,classe,tipo,qtd,preco,taxa) VALUES(?,?,?,?,?,?,?)"
        ).use { ps ->
            ps.setString(1, n.data.toString())
            ps.setString(2, n.ativo)
            ps.setString(3, n.classe)
            ps.setString(4, n.tipo.name)
            ps.setDouble(5, n.qtd)
            ps.setDouble(6, n.preco)
            ps.setDouble(7, n.taxa)
            ps.executeUpdate()
        }
    }

    fun listarComId(): List<Pair<Long, Transacao>> {
        val r = mutableListOf<Pair<Long, Transacao>>()
        conn.createStatement().use { st ->
            st.executeQuery(
                "SELECT id,data,ativo,classe,tipo,qtd,preco,taxa FROM transacoes ORDER BY data, id"
            ).use { rs ->
                while (rs.next()) {
                    r += rs.getLong("id") to Transacao(
                        data = LocalDate.parse(rs.getString("data")),
                        ativo = rs.getString("ativo"),
                        classe = rs.getString("classe"),
                        tipo = Tipo.valueOf(rs.getString("tipo")),
                        qtd = rs.getDouble("qtd"),
                        preco = rs.getDouble("preco"),
                        taxa = rs.getDouble("taxa")
                    )
                }
            }
        }
        return r
    }

    fun listar(): List<Transacao> = listarComId().map { it.second }

    fun apagar(id: Long) {
        val resto = listarComId().filter { it.first != id }.map { it.second }
        calcularPosicoes(resto)               // apagar uma compra pode invalidar vendas posteriores
        conn.prepareStatement("DELETE FROM transacoes WHERE id = ?").use { ps ->
            ps.setLong(1, id)
            ps.executeUpdate()
        }
    }

    fun definirPreco(ativo: String, preco: Double) {
        conn.prepareStatement(
            "INSERT OR REPLACE INTO precos(ativo,preco,data) VALUES(?,?,?)"
        ).use { ps ->
            ps.setString(1, ativo.trim().uppercase())
            ps.setDouble(2, preco)
            ps.setString(3, LocalDate.now().toString())
            ps.executeUpdate()
        }
    }

    fun precos(): Map<String, Double> {
        val m = mutableMapOf<String, Double>()
        conn.createStatement().use { st ->
            st.executeQuery("SELECT ativo, preco FROM precos").use { rs ->
                while (rs.next()) m[rs.getString("ativo")] = rs.getDouble("preco")
            }
        }
        return m
    }

    /** Posições abertas com o preço atual (sem preço definido, assume o custo médio). */
    fun carteira(): List<Linha> {
        val pr = precos()
        return calcularPosicoes(listar()).values
            .filter { it.qtd > 1e-12 }
            .map { p -> Linha(p, pr[p.ativo] ?: p.medio) }
    }

    override fun close() = conn.close()
}
