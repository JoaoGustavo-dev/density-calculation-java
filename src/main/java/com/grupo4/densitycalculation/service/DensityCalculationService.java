package com.grupo4.densitycalculation.service;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionMappingAdapter;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

import com.grupo4.densitycalculation.eos.CubicEoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DensityCalculationService {

    private static final Logger log = LoggerFactory.getLogger(DensityCalculationService.class);

    public double[] createTemperatureRange(double[] experimentalTemperatures, boolean useExperimentalTemperatures) {
        if (!useExperimentalTemperatures) {
            double min = Arrays.stream(experimentalTemperatures).min().orElseThrow();
            double max = Arrays.stream(experimentalTemperatures).max().orElseThrow();
            return linspace(min, max, 100);
        }
        return experimentalTemperatures;
    }


    private double[] linspace(double start, double end, int numPoints) {
        double[] result = new double[numPoints];
        double step = (end - start) / (numPoints - 1);
        for (int i = 0; i < numPoints; i++) {
            result[i] = start + step * i;
        }
        return result;
    }


    public Map<String, double[]> densityWithoutKijEstimation(CubicEoS eosModel, String phase,
                                                             double[] expTemperature,
                                                             boolean useExperimentalTemperatures) {
        Map<String, double[]> densitiesPerEos = new LinkedHashMap<>();
        double[] temperatures = createTemperatureRange(expTemperature, useExperimentalTemperatures);

        for (String eosName : CubicEoS.EOS_NAMES) {
            double[] densities = new double[temperatures.length];
            for (int i = 0; i < temperatures.length; i++) {
                eosModel.setEos(eosName);
                eosModel.setTemperature(temperatures[i]);
                densities[i] = eosModel.density(phase);
            }
            densitiesPerEos.put(eosName, densities);
        }

        log.info("Calculation accomplished successfully!");
        return densitiesPerEos;
    }


    private double[] calculateDensityForKij(double[] kij, CubicEoS eosModel, String phase, double[] temperatures) {
        double[] densities = new double[temperatures.length];
        for (int i = 0; i < temperatures.length; i++) {
            eosModel.setKij(kij);
            eosModel.setTemperature(temperatures[i]);
            densities[i] = eosModel.density(phase);
        }
        return densities;
    }


    private int combinations(int n, int k) {
        if (k > n) {
            return 0;
        }
        long result = 1;
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }
        return (int) result;
    }


    private double[] optimizeKij(CubicEoS eosModel, String phase, double[] expDensities,
                                 double[] expTemperature, boolean useExperimentalTemperatures) {
        int n = eosModel.getComponentCount();
        int size = combinations(n, 2);

        if (size == 0) {
            double[] evaluationTemperatures = createTemperatureRange(expTemperature, useExperimentalTemperatures);
            return calculateDensityForKij(new double[]{0.0}, eosModel, phase, evaluationTemperatures);
        }

        double[] lowerBounds = new double[size];
        double[] upperBounds = new double[size];
        Arrays.fill(lowerBounds, -0.7);
        Arrays.fill(upperBounds, 0.7);

        MultivariateFunction objective = kijCandidate -> {
            double[] densities = calculateDensityForKij(kijCandidate, eosModel, phase, expTemperature);
            double sumSquares = 0;
            for (int i = 0; i < densities.length; i++) {
                double relativeDeviation = (expDensities[i] - densities[i]) / expDensities[i];
                sumSquares += relativeDeviation * relativeDeviation;
            }
            return sumSquares;
        };

        MultivariateFunctionMappingAdapter adapter =
                new MultivariateFunctionMappingAdapter(objective, lowerBounds, upperBounds);

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        double[] startPoint = new double[size];

        PointValuePair result = optimizer.optimize(
                new MaxEval(5000),
                new ObjectiveFunction(adapter),
                GoalType.MINIMIZE,
                new InitialGuess(adapter.boundedToUnbounded(startPoint)),
                new NelderMeadSimplex(size)
        );

        double[] optimalKij = adapter.unboundedToBounded(result.getPoint());
        double[] evaluationTemperatures = createTemperatureRange(expTemperature, useExperimentalTemperatures);
        return calculateDensityForKij(optimalKij, eosModel, phase, evaluationTemperatures);
    }


    public Map<String, double[]> densityWithKijEstimation(CubicEoS eosModel, String phase,
                                                          double[] expDensities, double[] expTemperature,
                                                          boolean useExperimentalTemperatures) {
        Map<String, double[]> densitiesPerEos = new LinkedHashMap<>();
        for (String eosName : CubicEoS.EOS_NAMES) {
            eosModel.setEos(eosName);
            densitiesPerEos.put(eosName,
                    optimizeKij(eosModel, phase, expDensities, expTemperature, useExperimentalTemperatures));
        }
        return densitiesPerEos;
    }

}