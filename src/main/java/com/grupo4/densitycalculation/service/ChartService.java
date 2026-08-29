package com.grupo4.densitycalculation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.grupo4.densitycalculation.eos.CubicEoS;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class ChartService {

    private final DensityCalculationService densityCalculationService;

    public ChartService(DensityCalculationService densityCalculationService) {
        this.densityCalculationService = densityCalculationService;
    }

    private static final Logger log = LoggerFactory.getLogger(ChartService.class);

    public void plotDeviation(CubicEoS eosModel, String phase, double[] expDensities,
                              double[] expTemperature, boolean predictiveForm, String outputPath) throws IOException {

        Map<String, double[]> calculatedDensities;
        if (!predictiveForm) {
            calculatedDensities = densityCalculationService.densityWithKijEstimation(
                    eosModel, phase, expDensities, expTemperature, true);
        } else {
            calculatedDensities = densityCalculationService.densityWithoutKijEstimation(
                    eosModel, phase, expTemperature, true);
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, double[]> entry : calculatedDensities.entrySet()) {
            double[] densities = entry.getValue();
            double sumRelativeDeviation = 0;
            for (int i = 0; i < densities.length; i++) {
                sumRelativeDeviation += Math.abs(densities[i] - expDensities[i]) / expDensities[i];
            }
            double meanDeviationPercent = (sumRelativeDeviation / densities.length) * 100;
            dataset.addValue(meanDeviationPercent, "MRD", entry.getKey());
            log.info("MRD [{}] {} = {}%", predictiveForm ? "preditivo" : "ajustado",
                    entry.getKey(), String.format("%.2f", meanDeviationPercent));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Deviation per EOS",
                "Equation of State",
                "Mean Relative Deviation [%]",
                dataset
        );

        saveChart(chart, outputPath);
    }

    public void plotDensityVersusTemperature(CubicEoS eosModel, String phase, double[] expDensities,
                                             double[] expTemperature, String expLabel,
                                             boolean predictiveForm, String outputPath) throws IOException {

        boolean useExperimentalTemperatures = false;
        double[] temperatures = densityCalculationService.createTemperatureRange(expTemperature, useExperimentalTemperatures);

        Map<String, double[]> calculatedDensities;
        if (!predictiveForm) {
            calculatedDensities = densityCalculationService.densityWithKijEstimation(
                    eosModel, phase, expDensities, expTemperature, useExperimentalTemperatures);
        } else {
            calculatedDensities = densityCalculationService.densityWithoutKijEstimation(
                    eosModel, phase, expTemperature, useExperimentalTemperatures);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries experimentalSeries = new XYSeries(expLabel);
        for (int i = 0; i < expTemperature.length; i++) {
            experimentalSeries.add(expTemperature[i], expDensities[i]);
        }
        dataset.addSeries(experimentalSeries);

        for (Map.Entry<String, double[]> entry : calculatedDensities.entrySet()) {
            XYSeries series = new XYSeries(entry.getKey() + " EoS");
            double[] densities = entry.getValue();
            for (int i = 0; i < temperatures.length; i++) {
                series.add(temperatures[i], densities[i]);
            }
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                null,
                "Temperature [K]",
                "Density [kg/m3]",
                dataset
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        for (int seriesIndex = 1; seriesIndex < dataset.getSeriesCount(); seriesIndex++) {
            renderer.setSeriesLinesVisible(seriesIndex, true);
            renderer.setSeriesShapesVisible(seriesIndex, false);
        }
        plot.setRenderer(renderer);

        saveChart(chart, outputPath);
    }

    private void saveChart(JFreeChart chart, String outputPath) throws IOException {
        File file = new File(outputPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        ChartUtils.saveChartAsPNG(file, chart, 800, 600);
    }

}