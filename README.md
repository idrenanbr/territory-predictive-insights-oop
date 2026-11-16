# Territory Predictive Insights — Partes A e B

Global Solution 2026.2 da FIAP — Programação Orientada a Objetos (POO).

## 1. Problema e público beneficiado

O projeto simula uma estação comunitária de monitoramento de um córrego. Ela coleta dados de chuva, nível da água e vazão para apoiar equipes de defesa civil, associações de bairro e gestores municipais na priorização de vistorias e na comunicação preventiva de possíveis alagamentos.

O sistema não determina evacuações, interdições ou outras medidas de alto impacto. Ele apresenta histórico, estimativas, limites atingidos e justificativas para que uma pessoa responsável avalie a situação e decida o que fazer.

As leituras são simuladas por `Math.random()` dentro das faixas definidas para cada sensor. Não são medições reais de uma região específica.

## 2. Tecnologias e execução

- Java 17
- Maven
- JDBC e H2 em arquivo
- JUnit 5
- Mockito

Com JDK 17 e Maven instalados, na pasta que contém `pom.xml`, execute:

```bash
mvn clean test
```

Para iniciar a aplicação no IntelliJ IDEA:

1. Abra a pasta do projeto e aguarde o Maven importar as dependências.
2. Abra `src/main/java/app/SistemaMonitoramento.java`.
3. Clique no triângulo verde ao lado do método `main` ou clique com o botão direito no arquivo e escolha **Run 'SistemaMonitoramento.main()'**.

A aplicação usa o banco `jdbc:h2:./data/territorio`. A pasta `data/` é criada automaticamente e está no `.gitignore`; o arquivo local do banco não deve ser enviado ao GitHub.

## 3. Funcionalidades

1. Verificar os sensores e registrar uma leitura de cada sensor disponível.
2. Executar previsão por média móvel ou comparação de períodos e verificar os limiares.
3. Consultar o histórico persistido por sensor.
4. Consultar os alertas persistidos.
5. Gerenciar os limiares do território mediante código de acesso.
6. Consultar o status da estação, dos sensores e dos limiares.

O histórico de medições e os alertas são gravados no H2 e permanecem disponíveis depois de fechar e abrir o programa. Os limiares são mantidos em memória: alterações feitas no menu valem durante a execução atual; ao reiniciar, voltam aos valores iniciais configurados na aplicação.

## 4. Sensores e limiares iniciais

| Sensor | Identificador | Faixa simulada | Unidade | Atenção | Alerta | Crítico |
| --- | --- | ---: | --- | ---: | ---: | ---: |
| Chuva | `chuva` | 0 a 100 | mm/h | 25 | 50 | 75 |
| Nível da água | `nivel` | 0 a 200 | cm | 70 | 110 | 150 |
| Vazão | `vazao` | 0 a 500 | L/s | 170 | 280 | 400 |

Cada valor previsto é comparado com os três limiares do sensor. O alerta informa o sensor, o nível, o valor previsto e o limite atingido:

- `ATENÇÃO`: previsão igual ou superior ao limiar de atenção e abaixo do limiar de alerta.
- `ALERTA`: previsão igual ou superior ao limiar de alerta e abaixo do limiar crítico.
- `CRÍTICO`: previsão igual ou superior ao limiar crítico.

A alteração dos limites exige o código de gestor `2026` nesta versão acadêmica. Esse código é apenas demonstrativo e não deve ser usado como mecanismo de autenticação em produção.

## 5. Previsões e regra de confiabilidade

As duas estratégias herdam de `EstrategiaPrevisao` e sobrescrevem `prever()`, reutilizando a validação da classe-base por `super.prever()`:

- **`PrevisaoMediaMovel`**: calcula a média aritmética das últimas cinco medições disponíveis (ou de todas, quando o histórico contém exatamente cinco).
- **`PrevisaoComparacao`**: divide o histórico em duas partes cronológicas, calcula a média de cada período e projeta a tendência pela fórmula `média recente + (média recente - média anterior)`. O resultado é limitado a zero para não produzir previsão negativa.

As estratégias exigem no mínimo cinco medições do mesmo sensor e da mesma unidade. Com menos dados, a aplicação interrompe a previsão e explica que o histórico é insuficiente. A quantidade mínima é uma regra conservadora do protótipo, não uma garantia estatística de confiabilidade.

A escolha da estratégia é centralizada em `FabricaPrevisao`, que utiliza um mapa de criadores (`MEDIA_MOVEL` e `COMPARACAO`) em vez de espalhar condicionais de instanciação pelo sistema.

## 6. Organização do código

```text
src/main/java/
├── app/
│   └── SistemaMonitoramento.java
├── dominio/
│   ├── Alerta.java
│   ├── ComponenteTerritorial.java
│   ├── DadosTerritorio.java
│   ├── EstacaoMonitoramento.java
│   ├── Limiares.java
│   ├── Medicao.java
│   ├── NivelAlerta.java
│   └── Sensor*.java
├── excecao/
│   ├── DadosInvalidosException.java
│   └── FonteIndisponivelException.java
├── persistencia/
│   ├── BancoDados.java
│   ├── MedicaoDAO.java
│   └── AlertaDAO.java
├── previsao/
│   ├── EstrategiaPrevisao.java
│   ├── PrevisaoMediaMovel.java
│   ├── PrevisaoComparacao.java
│   └── FabricaPrevisao.java
└── servico/
    ├── ServicoMonitoramento.java
    ├── ResultadoPrevisao.java
    └── ResultadoMonitoramento.java
```

### Conceitos de POO e decisões

- **Abstração e herança**: `ComponenteTerritorial` é abstrata e `EstacaoMonitoramento` é uma implementação concreta. `Sensor` define o contrato implementado pelos sensores simulados.
- **Polimorfismo**: as estratégias de previsão compartilham `EstrategiaPrevisao`, mas calculam resultados diferentes. `FabricaPrevisao` fornece a implementação solicitada.
- **Encapsulamento**: `DadosTerritorio` protege os limiares e exige o código de acesso para alterá-los. `Medicao` valida os dados recebidos.
- **Exceções próprias**: `DadosInvalidosException` sinaliza entradas ou históricos inválidos/insuficientes; `FonteIndisponivelException` sinaliza falhas de sensor ou de acesso ao banco. As mensagens são apresentadas no menu; não há blocos `catch` vazios.
- **Persistência JDBC**: `MedicaoDAO` e `AlertaDAO` usam `PreparedStatement` com parâmetros, sem concatenar valores no SQL. Conexões e resultados são fechados com `try-with-resources`.
- **Padrão criacional Factory**: `FabricaPrevisao` centraliza a criação das estratégias. Foi escolhido porque o serviço e o menu não precisam conhecer detalhes de instanciação nem manter a lógica de criação duplicada.

## 7. Testes

A suíte contém testes de domínio e persistência da Parte A, além de testes das estratégias e do serviço da Parte B. Os novos testes usam o padrão AAA (Arrange, Act, Assert), verificam a exceção de histórico insuficiente e usam Mockito para simular `MedicaoDAO` e `AlertaDAO` sem acessar um banco real nos testes de lógica de alerta.

Execute todos com:

```bash
mvn clean test
```

## 8. Reflexão crítica

O sistema deve se calar quando não há dados suficientes, quando o sensor está indisponível, quando uma leitura é inválida ou quando o histórico não representa o mesmo sensor e unidade. Nesta implementação, menos de cinco medições bloqueiam a previsão. Mesmo com cinco ou mais leituras, o resultado deve ser interpretado com cautela: uma amostra curta pode refletir uma situação pontual e não uma tendência duradoura.

Os dados simulados também não representam toda a complexidade do território. Um sensor não descreve todas as ruas, diferenças de altitude, condições de drenagem, obstruções, intensidade localizada da chuva ou vulnerabilidades de cada comunidade. O protótipo não incorpora previsões meteorológicas oficiais, calibração física dos sensores nem validação com eventos históricos reais. Por isso, os números servem para demonstrar o fluxo de monitoramento, e não para orientar diretamente uma operação de emergência.

O alerta apoia a decisão humana porque transforma medições e limites configurados em uma sinalização compreensível: mostra o sensor, o valor previsto, o limiar ultrapassado e a justificativa. A avaliação de campo, a confirmação da fonte, a consulta a outros indicadores e a decisão de agir continuam sob responsabilidade de pessoas e órgãos competentes. Automatizar uma decisão de alto impacto com uma simulação simples poderia gerar alarmes falsos ou deixar de sinalizar riscos reais.

## 9. Divisão de tarefas

- **Renan Mano Otero** — Parte A: domínio, sensores, validações e persistência JDBC/H2.
- **Auro Vanetti de Moura Andrade** — Parte B: estratégias de previsão, Factory, serviço de monitoramento, classificação dos alertas, menu e testes da integração.

## Uso de IA

O ChatGPT ajudou para revisar erros e caçar bugs no código, além de fonte de tirar duvidas.
