package servico;

import dominio.Alerta;

import java.util.Optional;

public record ResultadoMonitoramento(ResultadoPrevisao previsao, Optional<Alerta> alerta) {

    public ResultadoMonitoramento {
        if (previsao == null || alerta == null) {
            throw new IllegalArgumentException("Previsão e opção de alerta são obrigatórias.");
        }
    }
}
