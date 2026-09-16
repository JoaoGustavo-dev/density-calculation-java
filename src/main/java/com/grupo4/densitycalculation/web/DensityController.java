package com.grupo4.densitycalculation.web;

import com.grupo4.densitycalculation.eos.CubicEoS;
import com.grupo4.densitycalculation.service.DensityCalculationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/density")
public class DensityController {

    private final DensityCalculationService densityCalculationService;

    public DensityController(DensityCalculationService densityCalculationService) {
        this.densityCalculationService = densityCalculationService;
    }

    @PostMapping("/point")
    public Map<String, Double> point(@RequestBody DensityDtos.DensityPointRequest request) {
        validate(request.criticalTemperature(), request.criticalPressure(), request.acentricFactor(),
                request.molFraction(), request.molarMass(), request.kij());

        CubicEoS eosModel = new CubicEoS(request.pressure(), request.criticalTemperature(),
                request.criticalPressure(), request.acentricFactor(), request.molFraction(),
                request.molarMass(), request.kij(), request.temperature(), "RK");

        Map<String, double[]> series = densityCalculationService.densityAllModelsFixedKij(
                eosModel, "vapor", request.kij(), new double[]{request.temperature()});

        Map<String, Double> result = new LinkedHashMap<>();
        series.forEach((model, values) -> result.put(model, values[0]));
        return result;
    }

    @PostMapping("/series")
    public Map<String, Object> series(@RequestBody DensityDtos.DensitySeriesRequest request) {
        validate(request.criticalTemperature(), request.criticalPressure(), request.acentricFactor(),
                request.molFraction(), request.molarMass(), request.kij());

        if (request.temperatureMin() >= request.temperatureMax()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "temperatureMin deve ser menor que temperatureMax");
        }

        int points = request.points() == null ? 25 : request.points();
        double[] temperatures = new double[points];
        double step = (request.temperatureMax() - request.temperatureMin()) / (points - 1);
        for (int i = 0; i < points; i++) {
            temperatures[i] = request.temperatureMin() + step * i;
        }

        CubicEoS eosModel = new CubicEoS(request.pressure(), request.criticalTemperature(),
                request.criticalPressure(), request.acentricFactor(), request.molFraction(),
                request.molarMass(), request.kij(), request.temperatureMin(), "RK");

        Map<String, double[]> densities = densityCalculationService.densityAllModelsFixedKij(
                eosModel, "vapor", request.kij(), temperatures);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("temperatures", temperatures);
        response.put("densities", densities);
        return response;
    }

    private void validate(double[] tc, double[] pc, double[] w, double[] x, double[] mm, double[] kij) {
        int n = tc.length;
        if (pc.length != n || w.length != n || x.length != n || mm.length != n) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Todos os vetores de componentes devem ter o mesmo tamanho");
        }
        double sum = 0;
        for (double xi : x) {
            if (xi < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Fracoes molares devem ser positivas");
            }
            sum += xi;
        }
        if (Math.abs(sum - 1.0) > 1e-3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A soma das fracoes molares deve ser aproximadamente 1");
        }
        int expectedKij = Math.max(n * (n - 1) / 2, 1);
        if (kij.length != expectedKij) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "kij deve ter " + expectedKij + " valor(es) para " + n + " componente(s)");
        }
    }
}