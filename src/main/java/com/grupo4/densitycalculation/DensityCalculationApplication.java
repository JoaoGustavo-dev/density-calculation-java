package com.grupo4.densitycalculation;

import com.grupo4.densitycalculation.eos.CubicEoS;
import com.grupo4.densitycalculation.service.ChartService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DensityCalculationApplication implements CommandLineRunner {

    private final ChartService chartService;

    public DensityCalculationApplication(ChartService chartService) {
        this.chartService = chartService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DensityCalculationApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        runGasPhaseCaseStudy();
        runLiquidPhaseCaseStudy();
        System.out.println("Graficos gerados na pasta 'output'.");
    }

    private void runGasPhaseCaseStudy() throws Exception {
        double pressure = 34.606e06;
        double[] criticalTemperature = {190.564, 369.89};
        double[] criticalPressure = {4.5992e06, 4.2512e06};
        double[] acentricFactor = {0.01142, 0.1521};
        double[] kij = {0.0};
        double[] molFraction = {0.9472, 0.0528};
        double[] molarMass = {16.043, 44.097};

        double[] expTemperature = {256.10, 311.0, 366.40, 421.52};
        double[] expDensity = {305.13, 242.27, 195.80, 163.74};

        CubicEoS predictiveModel = new CubicEoS(pressure, criticalTemperature, criticalPressure,
                acentricFactor, molFraction, molarMass, kij);

        chartService.plotDensityVersusTemperature(predictiveModel, "vapor", expDensity, expTemperature,
                "Karimi et al. 2016", true, "output/gas_phase_predictive.png");
        chartService.plotDeviation(predictiveModel, "vapor", expDensity, expTemperature, true,
                "output/gas_phase_predictive_deviation.png");

        CubicEoS fittedModel = new CubicEoS(pressure, criticalTemperature, criticalPressure,
                acentricFactor, molFraction, molarMass, kij);

        chartService.plotDensityVersusTemperature(fittedModel, "vapor", expDensity, expTemperature,
                "Karimi et al. 2016", false, "output/gas_phase_fitted.png");
        chartService.plotDeviation(fittedModel, "vapor", expDensity, expTemperature, false,
                "output/gas_phase_fitted_deviation.png");
    }

    private void runLiquidPhaseCaseStudy() throws Exception {
        double pressure = 101325;
        double[] criticalTemperature = {507.82};
        double[] criticalPressure = {3.0441e06};
        double[] acentricFactor = {0.3};
        double[] kij = {0.0};
        double[] molFraction = {1.0};
        double[] molarMass = {86.18};

        double[] expTemperature = {273.15, 278.15, 283.15, 288.15, 293.15, 298.15, 303.15, 308.15};
        double[] expDensity = {677.18, 672.79, 668.37, 663.91, 659.42, 654.89, 650.33, 645.73};

        CubicEoS model = new CubicEoS(pressure, criticalTemperature, criticalPressure,
                acentricFactor, molFraction, molarMass, kij);

        chartService.plotDensityVersusTemperature(model, "liquid", expDensity, expTemperature,
                "Ramos-Estrada et al. 2006", true, "output/liquid_phase_predictive.png");
        chartService.plotDeviation(model, "liquid", expDensity, expTemperature, true,
                "output/liquid_phase_predictive_deviation.png");
    }

}