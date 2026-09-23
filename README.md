# Density Calculation using Equations of State (EoS) — Java Edition

Este repositório é uma conversão para **Java (Spring Boot)** do framework original em Python [Density-calculation](https://github.com/ArleyAlles/Density-calculation), desenvolvido por Arley A. Cruz, Cochiran P. dos Santos e Walisson de J. Souza, publicado em:

> Cruz, A. A.; dos Santos, C. P.; Souza, W. J. *Density Calculation Using Cubic Equations of State: A Python-Based Framework for Thermodynamics Education*. J. Chem. Educ. 2026, 103, 4635–4642.
> [https://doi.org/10.1021/acs.jchemed.6c00172](https://doi.org/10.1021/acs.jchemed.6c00172)

O objetivo deste projeto é reproduzir, em Java, o mesmo cálculo de densidade de misturas fluidas por meio de quatro equações de estado cúbicas clássicas — van der Waals (VDW), Redlich–Kwong (RK), Soave–Redlich–Kwong (SRK) e Peng–Robinson (PR) — incluindo o modo preditivo (`kij = 0`) e o modo ajustado (`kij` otimizado), com geração de gráficos comparativos entre densidade calculada e experimental.

Como extensão do projeto, foi desenvolvida uma API REST e uma interface HTML interativa para permitir novos cálculos em fase vapor diretamente pelo navegador, sem necessidade de instalar nada.

Trabalho desenvolvido para a disciplina de Projeto de Programação — Grupo 4.

## Acesso online (sem instalar nada)

A interface web está publicada e pode ser usada por qualquer pessoa diretamente no navegador:

**https://density-calculation-java.onrender.com**

> **Atenção ao primeiro acesso:** a aplicação está hospedada em um plano gratuito, que "adormece" após um período sem uso. Se ninguém acessou o link recentemente, o primeiro carregamento pode levar cerca de 1 minuto. Aguarde a página carregar; depois disso as respostas são rápidas.

## Estrutura do projeto

```
Dockerfile                                # Build e execução em container (deploy)
.dockerignore                             # Arquivos ignorados no build da imagem

src/main/java/com/grupo4/densitycalculation/
├── DensityCalculationApplication.java   # Ponto de entrada da aplicação Spring Boot
├── CaseStudyRunner.java                 # Estudos de caso (PNGs e CSVs); desativado no profile "deploy"
├── eos/
│   ├── EoS.java                         # Classe abstrata base
│   ├── CubicEoS.java                    # Implementação das 4 equações de estado cúbicas
│   ├── EosParameters.java               # Parâmetros específicos de cada EoS
│   └── MixtureResult.java               # Resultado da regra de mistura
├── service/
│   ├── DensityCalculationService.java   # Cálculos em lote e otimização de kij
│   ├── ChartService.java                # Geração dos gráficos com JFreeChart
│   └── ResultCsvExporter.java           # Exportação dos resultados para CSV
└── web/
    ├── DensityController.java           # Endpoints da API REST
    └── DensityDtos.java                 # Objetos de entrada da API

src/main/resources/
├── application.properties               # Configuração (porta via variável de ambiente PORT)
└── static/
    └── index.html                       # Interface web interativa

src/test/java/com/grupo4/densitycalculation/eos/
└── CubicEoSTest.java                    # Testes automatizados com JUnit
```

## Tecnologias utilizadas

- **Java 21**
- **Spring Boot 3.3** (`CommandLineRunner`, profiles e API REST)
- **Apache Commons Math 3** — resolução do polinômio cúbico (`LaguerreSolver`) e otimização do parâmetro de interação binária `kij` (`SimplexOptimizer` com `MultivariateFunctionMappingAdapter`)
- **JFreeChart** — geração dos gráficos PNG dos estudos de caso (densidade × temperatura e MRD)
- **Chart.js** — gráficos interativos na página HTML
- **JUnit 5** — testes automatizados do núcleo de cálculo
- **Maven** — gerenciamento de dependências e build
- **Docker** — empacotamento da aplicação para deploy
- **Render** — hospedagem da interface web

## Pré-requisitos

Para executar localmente:

- [JDK 21](https://www.oracle.com/java/technologies/downloads/) ou superior
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- IDE recomendada: [IntelliJ IDEA](https://www.jetbrains.com/idea/)

Opcional, para executar via container:

- [Docker](https://www.docker.com/products/docker-desktop/)

No macOS, o Maven pode ser instalado com o Homebrew:

```bash
brew install maven
```

Verifique as versões instaladas:

```bash
java -version
mvn -version
```

## Modos de execução

A aplicação tem dois modos, controlados pelo profile do Spring:

| Modo | Profile | O que faz |
|------|---------|-----------|
| **Relatório** (local) | nenhum (padrão) | Sobe o servidor web **e** executa os estudos de caso, gerando os PNGs em `output/` e os CSVs em `resultados/` |
| **Deploy** (servidor) | `deploy` | Sobe **apenas** o servidor web e a interface; os estudos de caso não são executados e nenhum arquivo é gravado em disco |

## Como executar localmente (modo relatório)

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

A aplicação executa os estudos de caso, gera os gráficos PNG, exporta os resultados em CSV e disponibiliza a interface web em `http://localhost:8080`.

## Como executar sem gerar arquivos (modo deploy)

Para subir somente o servidor web, ative o profile `deploy`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=deploy
```

Ou, com o JAR já compilado:

```bash
java -jar target/density-calculation-1.0.0.jar --spring.profiles.active=deploy
```

Também é possível definir a variável de ambiente `SPRING_PROFILES_ACTIVE=deploy`.

## Executando com Docker

O `Dockerfile` usa build em duas etapas (compila com Maven + JDK 21 e executa apenas com o JRE) e já define o profile `deploy` dentro da imagem.

```bash
docker build -t density-calculation .
docker run -p 8080:8080 density-calculation
```

Depois, acesse `http://localhost:8080`.

## Deploy

A versão pública roda no [Render](https://render.com) como *Web Service* em modo Docker, a partir da branch `main` deste repositório. Pontos de configuração:

- **Porta:** o `application.properties` define `server.port=${PORT:8080}`. Em plataformas de deploy, a porta é injetada pela variável `PORT`; localmente, o padrão é `8080`.
- **Profile:** a imagem Docker já define `SPRING_PROFILES_ACTIVE=deploy`, então os estudos de caso não são executados no servidor.
- **Plano gratuito:** a instância adormece após inatividade (veja o aviso na seção de acesso online).

## Interface web

Depois de iniciar a aplicação (ou acessando o link público), a interface permite informar:

- componente 1 e componente 2 (ou "Nenhum" para fluido puro);
- fração molar do componente 1;
- parâmetro de interação binária `kij`;
- pressão (bar);
- temperatura de referência e faixa de temperatura da curva (mínima e máxima);
- modelo de referência para o cálculo de desvio.

A página envia os dados para a API Java e exibe:

1. **Gráfico de densidade × temperatura**, com as curvas dos quatro modelos (VDW, RK, SRK e PR), com foco na fase vapor.
2. **Tabela de resultados no ponto de referência**, com a densidade calculada por cada modelo na temperatura de referência escolhida.
3. **Gráfico de desvio relativo entre modelos** (barras), que compara cada modelo com o **modelo de referência** selecionado (padrão: Peng–Robinson), ao longo da faixa de temperatura calculada. Quanto menor a barra, mais próximo o modelo está do de referência.

> **Importante:** o gráfico de desvio da interface web **não é o MRD experimental** dos estudos de caso do relatório. Como a interface aceita entradas arbitrárias, não há dado experimental disponível para comparação; por isso o desvio é calculado entre os próprios modelos. O MRD contra dados experimentais continua sendo produzido apenas pelos estudos de caso (`CaseStudyRunner`), nos gráficos `*_deviation.png`.

### Observação sobre a fase calculada

A interface calcula a densidade da fase **vapor** (raiz de maior volume molar do polinômio cúbico). Em condições de pressão muito alta ou com componentes pesados (por exemplo, n-butano a 346 bar e 300 K), o fluido real está na região líquida ou densa, e os valores resultantes devem ser interpretados com cautela.

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

A API REST é uma extensão do núcleo original. O `CaseStudyRunner` continua responsável pela reprodução dos estudos de caso do artigo e pela geração dos gráficos e arquivos de validação (no modo relatório).

## Saídas geradas (modo relatório)

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

A classe `CubicEoSTest` (9 testes) verifica:

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