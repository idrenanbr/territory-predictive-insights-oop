package servico;

import excecao.DadosInvalidosException;

public record ResultadoPrevisao(String sensorId, String estrategia, double valorPrevisto,
                                String unidade, int leiturasConsideradas, String explicacao) {

    public ResultadoPrevisao {
        if (sensorId == null || sensorId.isBlank() || estrategia == null || estrategia.isBlank()
                || unidade == null || unidade.isBlank() || explicacao == null || explicacao.isBlank()) {
            throw new DadosInvalidosException("Os dados da previsão são obrigatórios.");
        }
        if (!Double.isFinite(valorPrevisto) || valorPrevisto < 0 || leiturasConsideradas < 1) {
            throw new DadosInvalidosException("O resultado da previsão deve ser finito e não negativo.");
        }
    }
}
