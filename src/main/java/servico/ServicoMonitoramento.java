package servico;

import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.Limiares;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.Sensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;
import previsao.EstrategiaPrevisao;

import java.sql.SQLException;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Coordena leituras, previsões, classificação e persistência dos alertas. */
public final class ServicoMonitoramento {

    private final MedicaoDAO medicaoDAO;
    private final AlertaDAO alertaDAO;
    private final DadosTerritorio dadosTerritorio;
    private final Map<String, Sensor> sensores;
    private final List<Sensor> listaSensores;
    private final Clock clock;

    public ServicoMonitoramento(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO,
                                DadosTerritorio dadosTerritorio, List<Sensor> sensores) {
        this(medicaoDAO, alertaDAO, dadosTerritorio, sensores, Clock.systemUTC());
    }

    public ServicoMonitoramento(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO,
                                DadosTerritorio dadosTerritorio, List<Sensor> sensores, Clock clock) {
        if (medicaoDAO == null || alertaDAO == null || dadosTerritorio == null
                || sensores == null || clock == null || sensores.isEmpty()) {
            throw new DadosInvalidosException("A configuração do serviço de monitoramento está incompleta.");
        }
        this.medicaoDAO = medicaoDAO;
        this.alertaDAO = alertaDAO;
        this.dadosTerritorio = dadosTerritorio;
        this.clock = clock;
        this.listaSensores = List.copyOf(sensores);
        this.sensores = listaSensores.stream().collect(Collectors.toUnmodifiableMap(
                Sensor::getId, Function.identity(), (primeiro, duplicado) -> {
                    throw new DadosInvalidosException("Existe mais de um sensor com o mesmo identificador.");
                }));
    }

    public Medicao registrarLeitura(String sensorId) {
        Sensor sensor = buscarSensor(sensorId);
        double valor = sensor.lerValor();
        Medicao medicao = new Medicao(sensor.getId(), valor, sensor.getUnidade(), clock.instant());
        try {
            medicaoDAO.salvar(medicao);
            return medicao;
        } catch (SQLException erro) {
            throw new FonteIndisponivelException(
                    "Não foi possível salvar a leitura do sensor " + sensorId + " no banco de dados.", erro);
        }
    }

    public ResultadoMonitoramento executarPrevisao(String sensorId, EstrategiaPrevisao estrategia) {
        Sensor sensor = buscarSensor(sensorId);
        if (estrategia == null) {
            throw new DadosInvalidosException("Selecione uma estratégia de previsão.");
        }

        List<Medicao> historico;
        try {
            historico = medicaoDAO.buscarHistoricoPorSensor(sensorId);
        } catch (SQLException erro) {
            throw new FonteIndisponivelException(
                    "Não foi possível consultar o histórico do sensor " + sensorId + ".", erro);
        }

        // A aplicação não emite previsões com amostra pequena ou insuficiente.
        double valorPrevisto = estrategia.prever(historico);
        ResultadoPrevisao previsao = new ResultadoPrevisao(sensorId, estrategia.getNome(), valorPrevisto,
                sensor.getUnidade(), historico.size(), estrategia.getDescricao());

        Optional<Alerta> alerta = classificarAlerta(previsao, dadosTerritorio.consultarLimiares(sensorId));
        alerta.ifPresent(this::persistirAlerta);
        return new ResultadoMonitoramento(previsao, alerta);
    }

    public List<Medicao> buscarHistoricoPorSensor(String sensorId) {
        buscarSensor(sensorId);
        try {
            return medicaoDAO.buscarHistoricoPorSensor(sensorId);
        } catch (SQLException erro) {
            throw new FonteIndisponivelException("Não foi possível consultar o histórico do sensor " + sensorId + ".", erro);
        }
    }

    public List<Alerta> listarAlertas() {
        try {
            return alertaDAO.listarAlertas();
        } catch (SQLException erro) {
            throw new FonteIndisponivelException("Não foi possível consultar os alertas persistidos.", erro);
        }
    }

    public dominio.ComponenteTerritorial.Status verificarStatusTerritorio() {
        return dadosTerritorio.getComponente().verificarStatus();
    }

    public List<Sensor> listarSensores() {
        return listaSensores;
    }

    public DadosTerritorio getDadosTerritorio() {
        return dadosTerritorio;
    }

    private Sensor buscarSensor(String sensorId) {
        Sensor sensor = sensores.get(sensorId);
        if (sensor == null) {
            throw new DadosInvalidosException("Sensor não cadastrado: " + sensorId + ".");
        }
        return sensor;
    }

    private Optional<Alerta> classificarAlerta(ResultadoPrevisao previsao, Limiares limiares) {
        double valor = previsao.valorPrevisto();
        NivelAlerta nivel;
        double limite;

        if (valor >= limiares.critico()) {
            nivel = NivelAlerta.CRITICO;
            limite = limiares.critico();
        } else if (valor >= limiares.alerta()) {
            nivel = NivelAlerta.ALERTA;
            limite = limiares.alerta();
        } else if (valor >= limiares.atencao()) {
            nivel = NivelAlerta.ATENCAO;
            limite = limiares.atencao();
        } else {
            return Optional.empty();
        }

        return Optional.of(new Alerta(previsao.sensorId(), nivel, valor, limite,
                previsao.unidade(), clock.instant()));
    }

    private void persistirAlerta(Alerta alerta) {
        try {
            alertaDAO.salvar(alerta);
        } catch (SQLException erro) {
            throw new FonteIndisponivelException(
                    "O alerta foi identificado, mas não foi possível salvá-lo no banco de dados.", erro);
        }
    }
}
