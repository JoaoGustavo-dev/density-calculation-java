package com.grupo4.densitycalculation.web;

public class DensityDtos {

    public record DensityPointRequest(
            double pressure,
            double[] criticalTemperature,
            double[] criticalPressure,
            double[] acentricFactor,
            double[] molFraction,
            double[] molarMass,
            double[] kij,
            double temperature) {
    }

    public record DensitySeriesRequest(
            double pressure,
            double[] criticalTemperature,
            double[] criticalPressure,
            double[] acentricFactor,
            double[] molFraction,
            double[] molarMass,
            double[] kij,
            double temperatureMin,
            double temperatureMax,
            Integer points) {
    }
}