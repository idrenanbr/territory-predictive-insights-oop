package previsao;

import dominio.Medicao;

import java.util.List;

/** Projeta a tendência comparando a metade recente com a metade anterior do histórico. */
public final class PrevisaoComparacao extends EstrategiaPrevisao {

    @Override
    public double prever(List<Medicao> historico) {
        double mediaDeReferencia = super.prever(historico);
        int pontoDeCorte = historico.size() / 2;
        List<Medicao> periodoAnterior = historico.subList(0, pontoDeCorte);
        List<Medicao> periodoRecente = historico.subList(pontoDeCorte, historico.size());

        double mediaAnterior = calcularMedia(periodoAnterior);
        double mediaRecente = calcularMedia(periodoRecente);
        double variacao = mediaRecente - mediaAnterior;

        // Mantém a previsão não negativa. O valor de referência da classe-base cobre
        // o caso em que as duas janelas não apresentam variação.
        if (variacao == 0.0) {
            return mediaDeReferencia;
        }
        return Math.max(0.0, mediaRecente + variacao);
    }

    @Override
    public String getNome() {
        return "Comparação de períodos";
    }

    @Override
    public String getDescricao() {
        return "Compara a média da metade recente do histórico com a metade anterior e projeta a variação.";
    }
}
