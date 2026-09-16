package com.grupo4.densitycalculation.eos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CubicEoSTest {

    private static final double PRESSURE = 34.606e06;
    private static final double[] TC = {190.564, 369.89};
    private static final double[] PC = {4.5992e06, 4.2512e06};
    private static final double[] W = {0.01142, 0.1521};
    private static final double[] X = {0.9472, 0.0528};
    private static final double[] MM = {16.043, 44.097};
    private static final double[] KIJ = {0.0};

    @ParameterizedTest
    @CsvSource({
            "PR,256.10,325.67199370876807",
            "SRK,256.10,296.62564586537286",
            "VDW,256.10,249.76277062386526",
            "RK,256.10,297.99720499362473"
    })
    void deveReproduzirValoresValidadosNoRelatorio(String eos, double temperature, double expectedDensity) {
        CubicEoS model = new CubicEoS(PRESSURE, TC, PC, W, X, MM, KIJ, temperature, eos);
        double density = model.density("vapor");
        assertEquals(expectedDensity, density, 1e-6);
    }

    @Test
    void deveAceitarOsQuatroModelos() {
        for (String eos : CubicEoS.EOS_NAMES) {
            CubicEoS model = new CubicEoS(PRESSURE, TC, PC, W, X, MM, KIJ, 300.0, eos);
            double density = model.density("vapor");
            assertTrue(Double.isFinite(density));
            assertTrue(density > 0);
        }
    }

    @Test
    void deveLancarExcecaoParaModeloInvalido() {
        CubicEoS model = new CubicEoS(PRESSURE, TC, PC, W, X, MM, KIJ, 300.0, "XYZ");
        assertThrows(IllegalArgumentException.class, () -> model.density("vapor"));
    }

    @Test
    void deveLancarExcecaoParaFaseInvalida() {
        double[] tc = {507.82};
        double[] pc = {3.0441e06};
        double[] w = {0.3};
        double[] x = {1.0};
        double[] mm = {86.18};
        double[] kij = {0.0};

        CubicEoS model = new CubicEoS(101325, tc, pc, w, x, mm, kij, 298.15, "PR");
        assertThrows(IllegalArgumentException.class, () -> model.density("plasma"));
    }

    @Test
    void deveCalcularDensidadeParaComponentePuro() {
        double[] tc = {507.82};
        double[] pc = {3.0441e06};
        double[] w = {0.3};
        double[] x = {1.0};
        double[] mm = {86.18};
        double[] kij = {0.0};

        CubicEoS model = new CubicEoS(101325, tc, pc, w, x, mm, kij, 298.15, "PR");
        double density = model.density("liquid");

        assertEquals(665.4339479131179, density, 1e-6);
    }

    @Test
    void deveRetornarNumeroDeComponentesCorreto() {
        CubicEoS mixture = new CubicEoS(PRESSURE, TC, PC, W, X, MM, KIJ, 300.0, "PR");
        assertEquals(2, mixture.getComponentCount());

        CubicEoS pure = new CubicEoS(101325,
                new double[]{507.82}, new double[]{3.0441e06}, new double[]{0.3},
                new double[]{1.0}, new double[]{86.18}, new double[]{0.0}, 298.15, "PR");
        assertEquals(1, pure.getComponentCount());
    }
}
