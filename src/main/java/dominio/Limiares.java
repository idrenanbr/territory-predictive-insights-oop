package dominio;

import excecao.DadosInvalidosException;

public record Limiares(double atencao, double alerta, double critico) {

    public Limiares {
        if (!Double.isFinite(atencao) || !Double.isFinite(alerta) || !Double.isFinite(critico)
                || atencao < 0 || atencao >= alerta || alerta >= critico) {
            throw new DadosInvalidosException(
                    "Os limiares devem ser finitos, não negativos e seguir ATENÇÃO < ALERTA < CRÍTICO.");
        }
    }
}
