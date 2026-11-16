package previsao;

import dominio.Medicao;
import excecao.DadosInvalidosException;

import java.util.List;

/** Calcula a média das últimas N medições do sensor. */
public final class PrevisaoMediaMovel extends EstrategiaPrevisao {

    private final int janela;

    public PrevisaoMediaMovel() {
        this(5);
    }

    public PrevisaoMediaMovel(int janela) {
        if (janela < 2) {
            throw new DadosInvalidosException("A janela da média móvel deve conter pelo menos duas leituras.");
        }
        this.janela = janela;
    }

    @Override
    public double prever(List<Medicao> historico) {
        double mediaDeReferencia = super.prever(historico);
        int inicio = Math.max(0, historico.size() - janela);
        List<Medicao> ultimasLeituras = historico.subList(inicio, historico.size());

        if (ultimasLeituras.size() == historico.size()) {
            return mediaDeReferencia;
        }
        return calcularMedia(ultimasLeituras);
    }

    @Override
    public String getNome() {
        return "Média móvel";
    }

    @Override
    public String getDescricao() {
        return "Média aritmética das últimas " + janela + " medições disponíveis.";
    }
}
