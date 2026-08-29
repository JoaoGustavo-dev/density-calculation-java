# Density Calculation using Equations of State (EoS) — Java Edition

Este repositório é uma conversão para **Java (Spring Boot)** do framework original em Python
["Density-calculation"](https://github.com/ArleyAlles/Density-calculation), desenvolvido por Arley A. Cruz,
Cochiran P. dos Santos e Walisson de J. Souza, publicado em:

> Cruz, A. A.; dos Santos, C. P.; Souza, W. J. *Density Calculation Using Cubic Equations of State: A
> Python-Based Framework for Thermodynamics Education*. J. Chem. Educ. 2026, 103, 4635–4642.
> https://doi.org/10.1021/acs.jchemed.6c00172

O objetivo deste projeto é reproduzir, em Java, o mesmo cálculo de densidade de misturas fluidas por meio
de quatro equações de estado cúbicas clássicas — van der Waals (VDW), Redlich–Kwong (RK),
Soave–Redlich–Kwong (SRK) e Peng–Robinson (PR) —, incluindo o modo preditivo (kij = 0) e o modo ajustado
(kij otimizado), com geração de gráficos comparativos entre densidade calculada e experimental.

Trabalho desenvolvido para a disciplina de Projeto de Programação — Grupo 4.

## Estrutura do projeto

```
src/main/java/com/grupo4/densitycalculation/
├── DensityCalculationApplication.java   # Ponto de entrada (CommandLineRunner) e estudos de caso
├── eos/
│   ├── EoS.java                         # Classe abstrata base
│   ├── CubicEoS.java                    # Implementação das 4 equações de estado cúbicas
│   ├── EosParameters.java               # Parametros especificos de cada EoS (sigma, epsilon, alfa, omega, psi)
│   └── MixtureResult.java               # Resultado da regra de mistura (a, b, aij, bi)
└── service/
    ├── DensityCalculationService.java   # Calculo em lote, criacao de range de temperatura, otimizacao de kij
    └── ChartService.java                # Geracao dos graficos (JFreeChart)
```

## Tecnologias utilizadas

- **Java 21**
- **Spring Boot 3.3** (`CommandLineRunner`, sem API REST — aplicação roda como script)
- **Apache Commons Math 3** — resolução do polinômio cúbico (`LaguerreSolver`) e otimização do
  parâmetro de interação binária `kij` (`SimplexOptimizer` com `MultivariateFunctionMappingAdapter`
  para respeitar os limites [-0.7, 0.7])
- **JFreeChart** — geração dos gráficos de densidade × temperatura e de desvio relativo médio (MRD)
- **Maven** — gerenciamento de dependências e build

## Pré-requisitos

- [JDK 21](https://www.oracle.com/java/technologies/downloads/) ou superior
- [Maven](https://maven.apache.org/download.cgi) (ou usar o Maven Wrapper incluído, se presente)
- [Git](https://git-scm.com/downloads)
- IDE recomendada: [IntelliJ IDEA](https://www.jetbrains.com/idea/)

## Como executar

1. Clone o repositório:

```bash
git clone https://github.com/SEU-USUARIO/density-calculation-java.git
cd density-calculation-java
```

2. Compile o projeto com Maven:

```bash
mvn clean install
```

3. Execute a aplicação:

```bash
mvn spring-boot:run
```

Ou, pela IDE, execute diretamente a classe `DensityCalculationApplication`.

4. Os gráficos gerados (formato `.png`) ficam disponíveis na pasta `output/`, criada automaticamente na
   raiz do projeto:

- `gas_phase_predictive.png` / `gas_phase_predictive_deviation.png`
- `gas_phase_fitted.png` / `gas_phase_fitted_deviation.png`
- `liquid_phase_predictive.png` / `liquid_phase_predictive_deviation.png`

## Estudos de caso

Os mesmos dois estudos de caso do artigo original foram reproduzidos:

- **Fase vapor** — mistura metano + propano, dados experimentais de Karimi et al. (2016), nos modos
  preditivo e ajustado (kij otimizado).
- **Fase líquida** — n-hexano puro, dados experimentais de Ramos-Estrada et al. (2006), modo preditivo.

## Referência do projeto original

Repositório Python de origem: https://github.com/ArleyAlles/Density-calculation