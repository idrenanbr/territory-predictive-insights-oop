package dominio;

import java.util.List;

public final class EstacaoMonitoramento extends ComponenteTerritorial {

    private final List<Sensor> sensores;

    public EstacaoMonitoramento(String id, String nome, String regiao, List<Sensor> sensores) {
        super(id, nome, regiao, Status.INDISPONIVEL);
        this.sensores = List.copyOf(sensores);
        verificarStatus();
    }

    @Override
    public Status verificarStatus() {
        long ativos = sensores.stream().filter(Sensor::verificarFuncionamento).count();
        if (ativos == 0) {
            atualizarStatus(Status.INDISPONIVEL);
        } else if (ativos == sensores.size()) {
            atualizarStatus(Status.OPERACIONAL);
        } else {
            atualizarStatus(Status.DEGRADADO);
        }
        return getStatus();
    }
}
