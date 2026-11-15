package persistencia;

import dominio.Medicao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class MedicaoDAO {

    private final BancoDados banco;

    public MedicaoDAO(BancoDados banco) {
        this.banco = banco;
    }

    public void salvar(Medicao medicao) throws SQLException {
        String sql = "INSERT INTO medicao (sensor_id, valor, unidade, instante) VALUES (?, ?, ?, ?)";
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, medicao.getSensorId());
            comando.setDouble(2, medicao.getValor());
            comando.setString(3, medicao.getUnidade());
            comando.setObject(4, medicao.getInstante().atOffset(ZoneOffset.UTC));
            comando.executeUpdate();
        }
    }

    public List<Medicao> buscarHistoricoPorSensor(String sensorId) throws SQLException {
        String sql = """
                SELECT sensor_id, valor, unidade, instante
                FROM medicao
                WHERE sensor_id = ?
                ORDER BY instante, id
                """;
        List<Medicao> historico = new ArrayList<>();
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, sensorId);
            try (ResultSet registros = comando.executeQuery()) {
                while (registros.next()) {
                    historico.add(new Medicao(registros.getString("sensor_id"),
                            registros.getDouble("valor"), registros.getString("unidade"),
                            registros.getObject("instante", OffsetDateTime.class).toInstant()));
                }
            }
        }
        return historico;
    }
}
