package dominio;

import excecao.DadosInvalidosException;

import java.time.Instant;

public class Medicao {

    private final String sensorId;
    private final double valor;
    private final String unidade;
    private final Instant instante;

    public Medicao(String sensorId, double valor, String unidade, Instant instante) {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("O identificador do sensor é obrigatório.");
        }
        if (!Double.isFinite(valor)) {
            throw new DadosInvalidosException("O valor da medição deve ser finito.");
        }
        if (unidade == null || unidade.isBlank()) {
            throw new DadosInvalidosException("A unidade da medição é obrigatória.");
        }
        if (instante == null) {
            throw new DadosInvalidosException("O instante da medição é obrigatório.");
        }

        this.sensorId = sensorId;
        this.valor = valor;
        this.unidade = unidade;
        this.instante = instante;
    }

    public String getSensorId() {
        return sensorId;
    }

    public double getValor() {
        return valor;
    }

    public String getUnidade() {
        return unidade;
    }

    public Instant getInstante() {
        return instante;
    }
}
