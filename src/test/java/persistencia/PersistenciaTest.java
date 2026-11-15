package persistencia;

import dominio.Alerta;
import dominio.Medicao;
import dominio.NivelAlerta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenciaTest {

    @TempDir
    Path diretorio;

    @Test
    void reabreBaseDeArquivoPreservandoMedicoesOrdenadasEAlertas() throws Exception {
        // Arrange
        String url = "jdbc:h2:" + diretorio.resolve("territorio").toAbsolutePath();
        BancoDados bancoInicial = new BancoDados(url);
        bancoInicial.inicializar();
        MedicaoDAO medicoes = new MedicaoDAO(bancoInicial);
        Instant recente = Instant.parse("2026-11-15T12:00:01.123456789Z");
        Instant anterior = recente.minusSeconds(1);
        Alerta alerta = new Alerta("chuva", NivelAlerta.ALERTA, 54, 50, "mm/h", recente);
        medicoes.salvar(new Medicao("chuva", 40, "mm/h", recente));
        medicoes.salvar(new Medicao("nivel", 110, "cm", anterior));
        medicoes.salvar(new Medicao("chuva", 20, "mm/h", anterior));
        medicoes.salvar(new Medicao("chuva", 45, "mm/h", recente));
        new AlertaDAO(bancoInicial).salvar(alerta);

        // Act
        BancoDados reaberto = new BancoDados(url);
        reaberto.inicializar();
        List<Medicao> recuperadas = new MedicaoDAO(reaberto).buscarHistoricoPorSensor("chuva");
        List<Alerta> alertas = new AlertaDAO(reaberto).listarAlertas();

        // Assert
        assertEquals(List.of(20.0, 40.0, 45.0), recuperadas.stream().map(Medicao::getValor).toList());
        assertEquals(List.of(anterior, recente, recente), recuperadas.stream().map(Medicao::getInstante).toList());
        assertEquals(List.of("mm/h", "mm/h", "mm/h"), recuperadas.stream().map(Medicao::getUnidade).toList());
        assertEquals(List.of(alerta), alertas);
    }
}
