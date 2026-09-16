package com.grupo4.densitycalculation.service;

import com.grupo4.densitycalculation.eos.CubicEoS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class ResultCsvExporter {

    public void export(CubicEoS eosModel, DensityCalculationService densityCalculationService,
                       String phase, double[] expDensities, double[] expTemperature,
                       boolean predictiveForm, String outputPath) throws IOException {

        Map<String, double[]> calculatedDensities;
        if (!predictiveForm) {
            calculatedDensities = densityCalculationService.densityWithKijEstimation(
                    eosModel, phase, expDensities, expTemperature, true);
        } else {
            calculatedDensities = densityCalculationService.densityWithoutKijEstimation(
                    eosModel, phase, expTemperature, true);
        }

        File file = new File(outputPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("EquationOfState,Temperature_K,ExperimentalDensity_kgm3,CalculatedDensity_kgm3,RelativeDeviationPercent\n");
            for (Map.Entry<String, double[]> entry : calculatedDensities.entrySet()) {
                String eosName = entry.getKey();
                double[] calculated = entry.getValue();
                for (int i = 0; i < calculated.length; i++) {
                    double expDensity = expDensities[i];
                    double expTemp = expTemperature[i];
                    double deviation = Math.abs(calculated[i] - expDensity) / expDensity * 100;
                    writer.write(eosName + "," + expTemp + "," + expDensity + "," + calculated[i] + "," + deviation + "\n");
                }
            }
        }
    }
}
