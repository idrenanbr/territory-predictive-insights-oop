package persistencia;

import dominio.Alerta;
import dominio.NivelAlerta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class AlertaDAO {

    private final BancoDados banco;

    public AlertaDAO(BancoDados banco) {
        this.banco = banco;
    }

    public void salvar(Alerta alerta) throws SQLException {
        String sql = """
                INSERT INTO alerta (sensor_id, nivel, valor_previsto, limiar, unidade, instante)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, alerta.sensorId());
            comando.setString(2, alerta.nivel().name());
            comando.setDouble(3, alerta.valorPrevisto());
            comando.setDouble(4, alerta.limiarUltrapassado());
            comando.setString(5, alerta.unidade());
            comando.setObject(6, alerta.instante().atOffset(ZoneOffset.UTC));
            comando.executeUpdate();
        }
    }

    public List<Alerta> listarAlertas() throws SQLException {
        String sql = """
                SELECT sensor_id, nivel, valor_previsto, limiar, unidade, instante
                FROM alerta
                ORDER BY instante, id
                """;
        List<Alerta> alertas = new ArrayList<>();
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet registros = comando.executeQuery()) {
            while (registros.next()) {
                alertas.add(new Alerta(registros.getString("sensor_id"),
                        NivelAlerta.valueOf(registros.getString("nivel")),
                        registros.getDouble("valor_previsto"), registros.getDouble("limiar"),
                        registros.getString("unidade"),
                        registros.getObject("instante", OffsetDateTime.class).toInstant()));
            }
        }
        return alertas;
    }
}
