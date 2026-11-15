package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MedicaoTest {

    @Test
    void medicaoValidaPreservaDados() {
        // Arrange
        String sensorId = "sensor-01";
        double valor = 12.5;
        String unidade = "mm";
        Instant instante = Instant.parse("2026-11-15T12:00:00Z");

        // Act
        Medicao medicao = new Medicao(sensorId, valor, unidade, instante);

        // Assert
        assertEquals(sensorId, medicao.getSensorId());
        assertEquals(valor, medicao.getValor());
        assertEquals(unidade, medicao.getUnidade());
        assertEquals(instante, medicao.getInstante());
    }

    @Test
    void medicaoComValorNaNLancaExcecao() {
        // Arrange
        String sensorId = "sensor-01";
        double valor = Double.NaN;
        String unidade = "mm";
        Instant instante = Instant.parse("2026-11-15T12:00:00Z");

        // Act
        DadosInvalidosException excecao = assertThrows(
                DadosInvalidosException.class,
                () -> new Medicao(sensorId, valor, unidade, instante)
        );

        // Assert
        assertEquals("O valor da medição deve ser finito.", excecao.getMessage());
    }
}
