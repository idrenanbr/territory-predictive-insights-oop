package dominio;

public final class SensorNivelAgua extends SensorSimulado {

    private static final double MAXIMO = 200.0;

    public SensorNivelAgua() {
        super("nivel", "Nível da água", "cm", MAXIMO, () -> Math.random() * MAXIMO);
    }
}
