package dominio;

public final class SensorVazao extends SensorSimulado {

    private static final double MAXIMO = 500.0;

    public SensorVazao() {
        super("vazao", "Vazão", "L/s", MAXIMO, () -> Math.random() * MAXIMO);
    }
}
