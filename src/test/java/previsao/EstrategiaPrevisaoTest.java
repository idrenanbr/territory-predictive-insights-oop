package previsao;

import dominio.Medicao;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstrategiaPrevisaoTest {

    @Test
    void mediaMovelConsideraSomenteAsUltimasCincoLeituras() {
        // Arrange
        List<Medicao> historico = historico("chuva", 10, 20, 30, 40, 50, 60);
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel();

        // Act
        double previsao = estrategia.prever(historico);

        // Assert
        assertEquals(40.0, previsao, 0.0001);
    }

    @Test
    void comparacaoProjetaVariacaoEntreDoisPeriodos() {
        // Arrange
        List<Medicao> historico = historico("chuva", 10, 10, 10, 20, 20, 20);
        PrevisaoComparacao estrategia = new PrevisaoComparacao();

        // Act
        double previsao = estrategia.prever(historico);

        // Assert
        assertEquals(30.0, previsao, 0.0001);
    }

    @Test
    void historicoComMenosDeCincoLeiturasNaoGeraPrevisao() {
        // Arrange
        List<Medicao> historico = historico("chuva", 10, 20, 30, 40);
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel();

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> estrategia.prever(historico));
    }

    @Test
    void fabricaCriaEstrategiaSolicitada() {
        // Arrange
        FabricaPrevisao fabrica = new FabricaPrevisao();

        // Act
        EstrategiaPrevisao estrategia = fabrica.criar("comparacao");

        // Assert
        assertInstanceOf(PrevisaoComparacao.class, estrategia);
    }

    private List<Medicao> historico(String sensorId, double... valores) {
        Instant base = Instant.parse("2026-11-15T12:00:00Z");
        return java.util.stream.IntStream.range(0, valores.length)
                .mapToObj(indice -> new Medicao(sensorId, valores[indice], "mm/h", base.plusSeconds(indice)))
                .toList();
    }
}
