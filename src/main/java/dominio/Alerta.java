package dominio;

import excecao.DadosInvalidosException;

import java.time.Instant;

public record Alerta(String sensorId, NivelAlerta nivel, double valorPrevisto,
                     double limiarUltrapassado, String unidade, Instant instante) {

    public Alerta {
        if (sensorId == null || sensorId.isBlank() || unidade == null || unidade.isBlank()
                || nivel == null || instante == null) {
            throw new DadosInvalidosException("Sensor, unidade, nível e instante são obrigatórios no alerta.");
        }
        if (!Double.isFinite(valorPrevisto) || !Double.isFinite(limiarUltrapassado)
                || limiarUltrapassado < 0 || valorPrevisto < limiarUltrapassado) {
            throw new DadosInvalidosException("O valor previsto deve atingir um limiar de alerta válido.");
        }
    }
}
