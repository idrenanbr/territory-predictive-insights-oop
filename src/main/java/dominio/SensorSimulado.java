package dominio;

import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;

import java.util.function.DoubleSupplier;

public abstract class SensorSimulado implements Sensor {

    private final String id;
    private final String tipo;
    private final String unidade;
    private final double maximo;
    private final DoubleSupplier gerarLeitura;
    private boolean disponivel = true;

    protected SensorSimulado(String id, String tipo, String unidade, double maximo,
                             DoubleSupplier gerarLeitura) {
        this.id = id;
        this.tipo = tipo;
        this.unidade = unidade;
        this.maximo = maximo;
        this.gerarLeitura = gerarLeitura;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTipo() {
        return tipo;
    }

    @Override
    public String getUnidade() {
        return unidade;
    }

    @Override
    public boolean verificarFuncionamento() {
        return disponivel;
    }

    public void definirDisponibilidade(boolean disponivel) {
        this.disponivel = disponivel;
    }

    @Override
    public double lerValor() {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException("O sensor " + id + " está indisponível.");
        }

        double valor;
        try {
            valor = gerarLeitura.getAsDouble();
        } catch (RuntimeException falha) {
            throw new FonteIndisponivelException("Falha ao ler o sensor " + id + ".", falha);
        }
        if (!Double.isFinite(valor) || valor < 0 || valor > maximo) {
            throw new DadosInvalidosException(
                    "Leitura de " + id + " deve estar entre 0 e " + maximo + " " + unidade + ".");
        }
        return valor;
    }
}
