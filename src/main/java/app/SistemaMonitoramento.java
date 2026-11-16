package app;

import dominio.Alerta;
import dominio.ComponenteTerritorial;
import dominio.DadosTerritorio;
import dominio.EstacaoMonitoramento;
import dominio.Limiares;
import dominio.Medicao;
import dominio.Sensor;
import dominio.SensorChuva;
import dominio.SensorNivelAgua;
import dominio.SensorVazao;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.BancoDados;
import persistencia.MedicaoDAO;
import previsao.EstrategiaPrevisao;
import previsao.FabricaPrevisao;
import servico.ResultadoMonitoramento;
import servico.ResultadoPrevisao;
import servico.ServicoMonitoramento;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Aplicação de console para o monitoramento territorial. */
public final class SistemaMonitoramento {

    private static final String CODIGO_GESTOR = "2026";

    private final Scanner scanner;
    private final ServicoMonitoramento servico;
    private final FabricaPrevisao fabricaPrevisao;

    public SistemaMonitoramento(Scanner scanner, ServicoMonitoramento servico,
                                FabricaPrevisao fabricaPrevisao) {
        this.scanner = scanner;
        this.servico = servico;
        this.fabricaPrevisao = fabricaPrevisao;
    }

    public static void main(String[] args) {
        try {
            Files.createDirectories(Path.of("data"));
            BancoDados banco = new BancoDados("jdbc:h2:./data/territorio");
            banco.inicializar();

            List<Sensor> sensores = List.of(new SensorChuva(), new SensorNivelAgua(), new SensorVazao());
            EstacaoMonitoramento estacao = new EstacaoMonitoramento(
                    "EST-01", "Estação de monitoramento do córrego", "Região comunitária simulada", sensores);
            DadosTerritorio dados = new DadosTerritorio(estacao, CODIGO_GESTOR, Map.of(
                    "chuva", new Limiares(25, 50, 75),
                    "nivel", new Limiares(70, 110, 150),
                    "vazao", new Limiares(170, 280, 400)
            ));

            ServicoMonitoramento servico = new ServicoMonitoramento(
                    new MedicaoDAO(banco), new AlertaDAO(banco), dados, sensores);
            new SistemaMonitoramento(new Scanner(System.in), servico, new FabricaPrevisao()).executar();
        } catch (IOException | SQLException erro) {
            System.err.println("Não foi possível iniciar o sistema ou acessar o banco: " + erro.getMessage());
        }
    }

    public void executar() {
        System.out.println("=== TERRITORY PREDICTIVE INSIGHTS ===");
        System.out.println("Monitoramento simulado de risco de alagamento.\n");
        boolean continuar = true;
        while (continuar) {
            exibirMenu();
            String opcao = lerTexto("Escolha uma opção: ");
            try {
                switch (opcao) {
                    case "1" -> registrarLeituras();
                    case "2" -> executarPrevisao();
                    case "3" -> exibirHistorico();
                    case "4" -> exibirAlertas();
                    case "5" -> gerenciarLimiares();
                    case "6" -> exibirStatusCompleto();
                    case "0" -> continuar = false;
                    default -> System.out.println("Opção inválida. Escolha uma opção do menu.");
                }
            } catch (DadosInvalidosException | FonteIndisponivelException erro) {
                System.out.println("Não foi possível concluir a operação: " + erro.getMessage());
            } catch (NumberFormatException erro) {
                System.out.println("Digite um número válido. A configuração não foi alterada.");
            }
            System.out.println();
        }
        System.out.println("Sistema encerrado. Os registros persistidos permanecem no banco H2.");
    }

    private void exibirMenu() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1 - Verificar sensores e registrar leituras");
        System.out.println("2 - Rodar previsão e avaliar alertas");
        System.out.println("3 - Consultar histórico de medições");
        System.out.println("4 - Consultar alertas emitidos");
        System.out.println("5 - Gerenciar limiares do território");
        System.out.println("6 - Exibir status completo");
        System.out.println("0 - Sair");
    }

    private void registrarLeituras() {
        for (Sensor sensor : servico.listarSensores()) {
            System.out.printf("%s (%s): ", sensor.getTipo(), sensor.getId());
            if (!sensor.verificarFuncionamento()) {
                System.out.println("indisponível; nenhuma leitura foi registrada.");
                continue;
            }
            try {
                Medicao medicao = servico.registrarLeitura(sensor.getId());
                System.out.printf("leitura %.2f %s registrada em %s.%n",
                        medicao.getValor(), medicao.getUnidade(), medicao.getInstante());
            } catch (FonteIndisponivelException | DadosInvalidosException erro) {
                System.out.println("falha: " + erro.getMessage());
            }
        }
    }

    private void executarPrevisao() {
        String sensorId = selecionarSensor();
        System.out.println("1 - Média móvel");
        System.out.println("2 - Comparação de períodos");
        String tipo = lerTexto("Estratégia: ");
        String chave = switch (tipo) {
            case "1" -> "MEDIA_MOVEL";
            case "2" -> "COMPARACAO";
            default -> throw new DadosInvalidosException("Estratégia inválida.");
        };

        EstrategiaPrevisao estrategia = fabricaPrevisao.criar(chave);
        ResultadoMonitoramento resultado = servico.executarPrevisao(sensorId, estrategia);
        ResultadoPrevisao previsao = resultado.previsao();
        System.out.printf("Previsão (%s): %.2f %s, calculada com %d leituras.%n",
                previsao.estrategia(), previsao.valorPrevisto(), previsao.unidade(),
                previsao.leiturasConsideradas());
        System.out.println("Justificativa: " + previsao.explicacao());
        resultado.alerta().ifPresentOrElse(this::exibirAlerta,
                () -> System.out.println("Nenhum limiar de alerta foi atingido pela previsão."));
    }

    private void exibirHistorico() {
        String sensorId = selecionarSensor();
        List<Medicao> historico = servico.buscarHistoricoPorSensor(sensorId);
        if (historico.isEmpty()) {
            System.out.println("Ainda não existem medições para este sensor.");
            return;
        }
        System.out.println("Histórico de " + sensorId + ":");
        historico.forEach(medicao -> System.out.printf("%s | %.2f %s%n",
                medicao.getInstante(), medicao.getValor(), medicao.getUnidade()));
    }

    private void exibirAlertas() {
        List<Alerta> alertas = servico.listarAlertas();
        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta foi emitido até o momento.");
            return;
        }
        alertas.forEach(this::exibirAlerta);
    }

    private void gerenciarLimiares() {
        String sensorId = selecionarSensor();
        Limiares atuais = servico.getDadosTerritorio().consultarLimiares(sensorId);
        System.out.printf("Limiares atuais de %s: atenção %.2f | alerta %.2f | crítico %.2f%n",
                sensorId, atuais.atencao(), atuais.alerta(), atuais.critico());
        String codigo = lerTexto("Código de acesso do gestor: ");
        double atencao = lerDouble("Novo limite de ATENÇÃO: ");
        double alerta = lerDouble("Novo limite de ALERTA: ");
        double critico = lerDouble("Novo limite de CRÍTICO: ");
        servico.getDadosTerritorio().alterarLimiares(sensorId, codigo, atencao, alerta, critico);
        System.out.println("Limiares atualizados para esta execução do sistema.");
    }

    private void exibirStatusCompleto() {
        ComponenteTerritorial componente = servico.getDadosTerritorio().getComponente();
        System.out.printf("Estação: %s (%s) | Região: %s%n",
                componente.getNome(), componente.getId(), componente.getRegiao());
        System.out.println("Status geral: " + servico.verificarStatusTerritorio().getDescricao());
        for (Sensor sensor : servico.listarSensores()) {
            System.out.printf("- %s: %s%n", sensor.getTipo(),
                    sensor.verificarFuncionamento() ? "operacional" : "indisponível");
        }
        System.out.println("Limiares configurados:");
        for (Sensor sensor : servico.listarSensores()) {
            Limiares limiares = servico.getDadosTerritorio().consultarLimiares(sensor.getId());
            System.out.printf("- %s: atenção %.2f | alerta %.2f | crítico %.2f %s%n",
                    sensor.getId(), limiares.atencao(), limiares.alerta(), limiares.critico(), sensor.getUnidade());
        }
    }

    private String selecionarSensor() {
        System.out.println("Sensores disponíveis: chuva, nivel, vazao");
        String sensorId = lerTexto("Identificador do sensor: ").trim().toLowerCase();
        servico.getDadosTerritorio().consultarLimiares(sensorId);
        return sensorId;
    }

    private void exibirAlerta(Alerta alerta) {
        System.out.printf("ALERTA %s | sensor %s | previsão %.2f %s | limite %.2f %s | %s%n",
                alerta.nivel().getDescricao(), alerta.sensorId(), alerta.valorPrevisto(), alerta.unidade(),
                alerta.limiarUltrapassado(), alerta.unidade(), alerta.instante());
        System.out.println("Motivo: a previsão atingiu ou ultrapassou o limiar configurado para este sensor.");
        System.out.println("Ação recomendada: encaminhar para avaliação humana; o sistema não decide medidas automaticamente.");
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private double lerDouble(String mensagem) {
        return Double.parseDouble(lerTexto(mensagem).replace(',', '.'));
    }
}
