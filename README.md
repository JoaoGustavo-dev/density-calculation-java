# Density Calculation using Equations of State (EoS) — Java Edition

Este repositório é uma conversão para **Java (Spring Boot)** do framework original em Python [Density-calculation](https://github.com/ArleyAlles/Density-calculation), desenvolvido por Arley A. Cruz, Cochiran P. dos Santos e Walisson de J. Souza, publicado em:

> Cruz, A. A.; dos Santos, C. P.; Souza, W. J. *Density Calculation Using Cubic Equations of State: A Python-Based Framework for Thermodynamics Education*. J. Chem. Educ. 2026, 103, 4635–4642.
> [https://doi.org/10.1021/acs.jchemed.6c00172](https://doi.org/10.1021/acs.jchemed.6c00172)

O objetivo deste projeto é reproduzir, em Java, o mesmo cálculo de densidade de misturas fluidas por meio de quatro equações de estado cúbicas clássicas — van der Waals (VDW), Redlich–Kwong (RK), Soave–Redlich–Kwong (SRK) e Peng–Robinson (PR) — incluindo o modo preditivo (`kij = 0`) e o modo ajustado (`kij` otimizado), com geração de gráficos comparativos entre densidade calculada e experimental.

Como extensão do projeto, foi desenvolvida uma API REST e uma interface HTML interativa para permitir novos cálculos em fase vapor diretamente pelo navegador.

Trabalho desenvolvido para a disciplina de Projeto de Programação — Grupo 4.

## Estrutura do projeto

```
src/main/java/com/grupo4/densitycalculation/
├── DensityCalculationApplication.java   # Ponto de entrada e estudos de caso
├── eos/
│   ├── EoS.java                         # Classe abstrata base
│   ├── CubicEoS.java                    # Implementação das 4 equações de estado cúbicas
│   ├── EosParameters.java               # Parâmetros específicos de cada EoS
│   └── MixtureResult.java               # Resultado da regra de mistura
├── service/
│   ├── DensityCalculationService.java   # Cálculos em lote e otimização de kij
│   ├── ChartService.java                # Geração dos gráficos com JFreeChart
│   └── ResultCsvExporter.java            # Exportação dos resultados para CSV
└── web/
    ├── DensityController.java            # Endpoints da API REST
    └── DensityDtos.java                  # Objetos de entrada da API

src/main/resources/static/
└── index.html                            # Interface web interativa

src/test/java/com/grupo4/densitycalculation/eos/
└── CubicEoSTest.java                     # Testes automatizados com JUnit
```

## Tecnologias utilizadas

- **Java 21**
- **Spring Boot 3.3** (`CommandLineRunner` e API REST)
- **Apache Commons Math 3** — resolução do polinômio cúbico (`LaguerreSolver`) e otimização do parâmetro de interação binária `kij` (`SimplexOptimizer` com `MultivariateFunctionMappingAdapter`)
- **JFreeChart** — geração dos gráficos de densidade × temperatura e de desvio relativo médio (MRD)
- **Chart.js** — geração do gráfico interativo na página HTML
- **JUnit 5** — testes automatizados do núcleo de cálculo
- **Maven** — gerenciamento de dependências e build

## Pré-requisitos

- [JDK 21](https://www.oracle.com/java/technologies/downloads/) ou superior
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- IDE recomendada: [IntelliJ IDEA](https://www.jetbrains.com/idea/)

No macOS, o Maven pode ser instalado com o Homebrew:

```bash
brew install maven
```

Verifique as versões instaladas:

```bash
java -version
mvn -version
```

## Como executar

1. Clone o repositório:

```bash
git clone https://github.com/JoaoGustavo-dev/density-calculation-java.git
cd density-calculation-java
```

2. Compile o projeto:

```bash
mvn clean package
```

3. Execute os testes automatizados:

```bash
mvn test
```

4. Execute a aplicação:

```bash
mvn spring-boot:run
```

Ou, pela IDE, execute diretamente a classe `DensityCalculationApplication`.

A aplicação executa os estudos de caso, gera os gráficos PNG e exporta os resultados em CSV.

## Interface web

Depois de iniciar a aplicação, acesse:

```text
http://localhost:8080
```

A interface permite informar:

- componente puro ou mistura binária;
- fração molar;
- pressão;
- faixa de temperatura;
- parâmetro de interação binária `kij`.

A página envia os dados para a API Java e exibe um gráfico interativo de densidade em função da temperatura para os quatro modelos, com foco na fase vapor.

## API REST

A interface web utiliza os seguintes endpoints:

```text
POST /api/density/point
```

Calcula a densidade dos quatro modelos em uma temperatura específica.

```text
POST /api/density/series
```

Calcula uma série de densidades em uma faixa de temperaturas.

A API REST é uma extensão do núcleo original. A aplicação `CommandLineRunner` continua responsável pela reprodução dos estudos de caso do artigo e pela geração dos gráficos e arquivos de validação.

## Saídas geradas

Os gráficos gerados pela aplicação ficam disponíveis na pasta `output/`:

- `gas_phase_predictive.png` / `gas_phase_predictive_deviation.png`
- `gas_phase_fitted.png` / `gas_phase_fitted_deviation.png`
- `liquid_phase_predictive.png` / `liquid_phase_predictive_deviation.png`

Os resultados numéricos são exportados para a pasta `resultados/`:

- `gas_phase_predictive.csv`
- `gas_phase_fitted.csv`
- `liquid_phase_predictive.csv`

Cada arquivo CSV registra a equação de estado, a temperatura, a densidade experimental, a densidade calculada e o desvio relativo percentual.

## Estudos de caso

Os mesmos dois estudos de caso do artigo original foram reproduzidos:

- **Fase vapor** — mistura metano + propano, com dados experimentais de Karimi et al. (2016), nos modos preditivo e ajustado (`kij` otimizado).
- **Fase líquida** — n-hexano puro, com dados experimentais de Ramos-Estrada et al. (2006), no modo preditivo.

## Testes automatizados

A classe `CubicEoSTest` verifica:

- reprodução dos valores numéricos validados;
- funcionamento dos quatro modelos;
- rejeição de modelos inválidos;
- rejeição de fases inválidas;
- cálculo de componente puro;
- quantidade de componentes da mistura.

Os testes podem ser executados com:

```bash
mvn test
```

## Referência do projeto original

Repositório Python de origem: [https://github.com/ArleyAlles/Density-calculation](https://github.com/ArleyAlles/Density-calculation)