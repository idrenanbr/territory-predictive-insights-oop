package previsao;

import dominio.Medicao;
import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Classe-base das estratégias de previsão.
 * A implementação-base valida o histórico e calcula uma média de referência.
 * As estratégias concretas reutilizam essa validação/referência por super.prever().
 */
public abstract class EstrategiaPrevisao {

    public static final int MINIMO_LEITURAS = 5;

    public double prever(List<Medicao> historico) {
        validarHistorico(historico);
        return calcularMedia(historico);
    }

    protected final void validarHistorico(List<Medicao> historico) {
        if (historico == null || historico.size() < MINIMO_LEITURAS) {
            int quantidade = historico == null ? 0 : historico.size();
            throw new DadosInvalidosException(
                    "Histórico insuficiente: são necessárias pelo menos " + MINIMO_LEITURAS
                            + " leituras válidas; foram encontradas " + quantidade + ".");
        }

        Medicao primeira = historico.get(0);
        if (primeira == null) {
            throw new DadosInvalidosException("O histórico contém uma medição vazia.");
        }

        for (Medicao medicao : historico) {
            if (medicao == null) {
                throw new DadosInvalidosException("O histórico contém uma medição vazia.");
            }
            if (!primeira.getSensorId().equals(medicao.getSensorId())
                    || !primeira.getUnidade().equals(medicao.getUnidade())) {
                throw new DadosInvalidosException(
                        "O histórico deve conter medições do mesmo sensor e da mesma unidade.");
            }
        }
    }

    protected final double calcularMedia(List<Medicao> medicoes) {
        return medicoes.stream().mapToDouble(Medicao::getValor).average()
                .orElseThrow(() -> new DadosInvalidosException("Não foi possível calcular a média do histórico."));
    }

    public abstract String getNome();

    public abstract String getDescricao();
}
