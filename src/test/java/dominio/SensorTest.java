package dominio;

import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorTest {

    @Test
    void fonteIndisponivelLancaExcecao() {
        // Arrange
        SensorChuva sensor = new SensorChuva();
        sensor.definirDisponibilidade(false);

        // Act
        FonteIndisponivelException erro = assertThrows(FonteIndisponivelException.class, sensor::lerValor);

        // Assert
        assertTrue(erro.getMessage().contains(sensor.getId()));
    }

    @Test
    void falhaDoGeradorLancaExcecaoDeFonte() {
        // Arrange
        IllegalStateException falha = new IllegalStateException("Falha na geração da leitura");
        SensorChuva sensor = new SensorChuva(() -> {
            throw falha;
        });

        // Act
        FonteIndisponivelException erro = assertThrows(FonteIndisponivelException.class, sensor::lerValor);

        // Assert
        assertSame(falha, erro.getCause());
        assertTrue(erro.getMessage().contains(sensor.getId()));
    }

    @Test
    void leituraForaDaFaixaLancaExcecao() {
        // Arrange
        SensorChuva sensor = new SensorChuva(() -> -1);

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class, sensor::lerValor);

        // Assert
        assertTrue(erro.getMessage().contains("entre 0 e 100.0 mm/h"));
    }
}
