package servico;

import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.EstacaoMonitoramento;
import dominio.Limiares;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.Sensor;
import dominio.SensorChuva;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;
import previsao.PrevisaoMediaMovel;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicoMonitoramentoTest {

    @Test
    void previsaoAcimaDoLimiarGeraEGravaAlertaUsandoDaoMockado() throws Exception {
        // Arrange
        MedicaoDAO medicaoDAO = mock(MedicaoDAO.class);
        AlertaDAO alertaDAO = mock(AlertaDAO.class);
        List<Medicao> historico = historico(60, 60, 60, 60, 60);
        when(medicaoDAO.buscarHistoricoPorSensor("chuva")).thenReturn(historico);
        ServicoMonitoramento servico = criarServico(medicaoDAO, alertaDAO);

        // Act
        ResultadoMonitoramento resultado = servico.executarPrevisao("chuva", new PrevisaoMediaMovel());

        // Assert
        assertEquals(60.0, resultado.previsao().valorPrevisto(), 0.0001);
        assertEquals(NivelAlerta.ALERTA, resultado.alerta().orElseThrow().nivel());
        verify(alertaDAO).salvar(any(Alerta.class));
    }

    @Test
    void previsaoAbaixoDoLimiteDeAtencaoNaoGeraAlerta() throws Exception {
        // Arrange
        MedicaoDAO medicaoDAO = mock(MedicaoDAO.class);
        AlertaDAO alertaDAO = mock(AlertaDAO.class);
        when(medicaoDAO.buscarHistoricoPorSensor("chuva")).thenReturn(historico(10, 10, 10, 10, 10));
        ServicoMonitoramento servico = criarServico(medicaoDAO, alertaDAO);

        // Act
        ResultadoMonitoramento resultado = servico.executarPrevisao("chuva", new PrevisaoMediaMovel());

        // Assert
        assertFalse(resultado.alerta().isPresent());
        verify(alertaDAO, never()).salvar(any(Alerta.class));
    }

    @Test
    void historicoInsuficienteImpedeAlertaEPrevisao() throws Exception {
        // Arrange
        MedicaoDAO medicaoDAO = mock(MedicaoDAO.class);
        AlertaDAO alertaDAO = mock(AlertaDAO.class);
        when(medicaoDAO.buscarHistoricoPorSensor("chuva")).thenReturn(historico(10, 20, 30, 40));
        ServicoMonitoramento servico = criarServico(medicaoDAO, alertaDAO);

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> servico.executarPrevisao("chuva", new PrevisaoMediaMovel()));
        verify(alertaDAO, never()).salvar(any(Alerta.class));
    }

    private ServicoMonitoramento criarServico(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO) {
        Sensor sensor = new SensorChuva();
        EstacaoMonitoramento estacao = new EstacaoMonitoramento(
                "EST-01", "Estação teste", "Região simulada", List.of(sensor));
        DadosTerritorio dados = new DadosTerritorio(estacao, "2026",
                Map.of("chuva", new Limiares(25, 50, 75)));
        Clock clock = Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC);
        return new ServicoMonitoramento(medicaoDAO, alertaDAO, dados, List.of(sensor), clock);
    }

    private List<Medicao> historico(double... valores) {
        Instant base = Instant.parse("2026-11-15T12:00:00Z");
        return java.util.stream.IntStream.range(0, valores.length)
                .mapToObj(indice -> new Medicao("chuva", valores[indice], "mm/h", base.plusSeconds(indice)))
                .toList();
    }
}
