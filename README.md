# Territory Predictive Insights — Parte A

Global Solution 2026.2 da FIAP — Object-Oriented Programming (OOP).

Uma estação simulada monitora chuva, nível da água e vazão perto de um córrego. O histórico ajuda a equipe comunitária ou municipal a priorizar vistorias e decidir quando avisar a população sobre risco de alagamento.

## Compilar e testar

Com JDK 17 e Maven, rode na pasta do `pom.xml`:

```sh
mvn clean test
```

A suíte JUnit inclui um teste que reabre o H2 e confere os registros gravados. O console da aplicação entra na integração com a Parte B.

## Banco de dados

O projeto usa JDBC com H2 em arquivo para preservar medições e alertas entre execuções. Use `new BancoDados("jdbc:h2:./data/territorio")` e chame `banco.inicializar()` antes de usar os DAOs. A inicialização aplica `src/main/resources/schema.sql` e mantém os dados existentes. Na aplicação integrada, execute a partir da raiz do projeto para usar a mesma base `data/territorio.mv.db`.

## Integração com a Parte B

- Use `Sensor.lerValor()` para obter a leitura e `verificarFuncionamento()` para consultar a disponibilidade. `SensorSimulado.definirDisponibilidade(boolean)` permite simular a interrupção e a volta da fonte. `EstacaoMonitoramento.verificarStatus()` calcula o estado da estação a partir dos sensores.
- Crie uma `Medicao` com ID do sensor, valor, unidade e instante, e grave com `MedicaoDAO.salvar`. `buscarHistoricoPorSensor` retorna os registros daquele sensor em ordem de instante e ID.
- Configure os limites iniciais no construtor de `DadosTerritorio`. Use `consultarLimiares` para ler e `alterarLimiares` para alterar com o código configurado. A alteração vale para a instância em uso.
- Ao emitir um alerta, crie um `Alerta` com sensor, nível, previsão, limite atingido, unidade e instante. Grave com `AlertaDAO.salvar` e consulte com `listarAlertas`. O histórico conserva o limite que motivou cada alerta.

| Sensor | Faixa validada | Limiares sugeridos (atenção, alerta, crítico) |
| --- | --- | --- |
| `chuva` | 0 a 100 mm/h | 25, 50, 75 |
| `nivel` | 0 a 200 cm | 70, 110, 150 |
| `vazao` | 0 a 500 L/s | 170, 280, 400 |

## Contribuições

- Renan Mano Otero — Parte A (domínio, sensores, validações e persistência).
- Auro Vanetti de Moura Andrade — responsável pela Parte B na divisão do trabalho.

## Uso de IA

O ChatGPT apoiou a implementação e a revisão da Parte A, incluindo o código Java, os testes e a documentação. A revisão técnica incluiu a execução de oito testes com Java 17 e a verificação de que as medições e os alertas continuam disponíveis após reabrir o banco H2.
