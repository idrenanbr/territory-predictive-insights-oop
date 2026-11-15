package persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class BancoDados {

    private final String url;

    public BancoDados(String url) {
        this.url = url;
    }

    Connection abrirConexao() throws SQLException {
        return DriverManager.getConnection(url, "sa", "");
    }

    public void inicializar() throws IOException, SQLException {
        try (InputStream recurso = BancoDados.class.getResourceAsStream("/schema.sql")) {
            if (recurso == null) {
                throw new IOException("Arquivo schema.sql não encontrado.");
            }

            String script = new String(recurso.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection conexao = abrirConexao(); Statement comando = conexao.createStatement()) {
                for (String instrucao : script.split(";")) {
                    if (!instrucao.isBlank()) {
                        comando.executeUpdate(instrucao);
                    }
                }
            }
        }
    }
}
