package dominio;

public interface Sensor {

    String getId();

    String getTipo();

    String getUnidade();

    boolean verificarFuncionamento();

    double lerValor();
}
