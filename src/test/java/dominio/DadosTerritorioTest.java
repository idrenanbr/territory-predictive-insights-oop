package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DadosTerritorioTest {

    @Test
    void codigoIncorretoPreservaConfiguracao() {
        // Arrange
        DadosTerritorio dados = criarDados();

        // Act
        assertThrows(DadosInvalidosException.class,
                () -> dados.alterarLimiares("chuva", "incorreto", 3, 5, 7));

        // Assert
        assertEquals(new Limiares(25, 50, 75), dados.consultarLimiares("chuva"));
    }

    @Test
    void limiaresForaDeOrdemPreservamConfiguracao() {
        // Arrange
        DadosTerritorio dados = criarDados();

        // Act
        assertThrows(DadosInvalidosException.class,
                () -> dados.alterarLimiares("chuva", "2026", 10, 5, 7));

        // Assert
        assertEquals(new Limiares(25, 50, 75), dados.consultarLimiares("chuva"));
    }

    private DadosTerritorio criarDados() {
        SensorChuva sensor = new SensorChuva();
        var estacao = new EstacaoMonitoramento("EST-01", "Estação", "Região simulada", List.of(sensor));
        return new DadosTerritorio(estacao, "2026", Map.of("chuva", new Limiares(25, 50, 75)));
    }
}
