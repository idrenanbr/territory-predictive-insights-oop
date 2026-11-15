package dominio;

import java.util.function.DoubleSupplier;

public final class SensorChuva extends SensorSimulado {

    private static final double MAXIMO = 100.0;

    public SensorChuva() {
        this(() -> Math.random() * MAXIMO);
    }

    SensorChuva(DoubleSupplier gerarLeitura) {
        super("chuva", "Intensidade de chuva", "mm/h", MAXIMO, gerarLeitura);
    }
}
