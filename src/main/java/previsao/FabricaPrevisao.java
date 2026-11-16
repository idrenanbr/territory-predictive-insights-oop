package previsao;

import excecao.DadosInvalidosException;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/** Factory: centraliza a criação das estratégias de previsão. */
public final class FabricaPrevisao {

    private static final Map<String, Supplier<EstrategiaPrevisao>> CRIADORES = Map.of(
            "MEDIA_MOVEL", PrevisaoMediaMovel::new,
            "COMPARACAO", PrevisaoComparacao::new
    );

    public EstrategiaPrevisao criar(String tipo) {
        String chave = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
        Supplier<EstrategiaPrevisao> criador = CRIADORES.get(chave);
        if (criador == null) {
            throw new DadosInvalidosException(
                    "Estratégia desconhecida. Utilize MEDIA_MOVEL ou COMPARACAO.");
        }
        return criador.get();
    }
}
