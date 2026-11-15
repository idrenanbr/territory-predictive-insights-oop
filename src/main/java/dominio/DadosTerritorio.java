package dominio;

import excecao.DadosInvalidosException;

import java.util.HashMap;
import java.util.Map;

public final class DadosTerritorio {

    private final ComponenteTerritorial componente;
    private final String codigoAcesso;
    private final Map<String, Limiares> limiares;

    public DadosTerritorio(ComponenteTerritorial componente, String codigoAcesso,
                           Map<String, Limiares> limiaresIniciais) {
        this.componente = componente;
        this.codigoAcesso = codigoAcesso;
        this.limiares = new HashMap<>(Map.copyOf(limiaresIniciais));
    }

    public ComponenteTerritorial getComponente() {
        return componente;
    }

    public Limiares consultarLimiares(String sensorId) {
        Limiares resultado = limiares.get(sensorId);
        if (resultado == null) {
            throw new DadosInvalidosException("Sensor sem configuração de limiares: " + sensorId + ".");
        }
        return resultado;
    }

    public void alterarLimiares(String sensorId, String codigo, double atencao,
                                double alerta, double critico) {
        if (!codigoAcesso.equals(codigo)) {
            throw new DadosInvalidosException("Código de acesso incorreto.");
        }
        consultarLimiares(sensorId);
        limiares.put(sensorId, new Limiares(atencao, alerta, critico));
    }
}
