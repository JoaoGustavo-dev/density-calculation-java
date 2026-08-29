package com.grupo4.densitycalculation.eos;

import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CubicEoS extends EoS {

    public static final List<String> EOS_NAMES = List.of("PR", "SRK", "VDW", "RK");

    private static final double R = 8.314;

    private final double[] tc;
    private final double[] pc;
    private final double[] w;
    private final double[] mm;

    public CubicEoS(double pressure, double[] tc, double[] pc, double[] w,
                    double[] x, double[] mm, double[] kij) {
        this(pressure, tc, pc, w, x, mm, kij, 298.15, "RK");
    }

    public CubicEoS(double pressure, double[] tc, double[] pc, double[] w,
                    double[] x, double[] mm, double[] kij,
                    double temperature, String eos) {
        super(eos, temperature, pressure, x, kij);
        this.tc = tc;
        this.pc = pc;
        this.w = w;
        this.mm = mm;
    }

    public int getComponentCount() {
        return w.length;
    }


    private EosParameters setEosParameters(double temperature, String eosType) {
        int n = tc.length;
        double[] tr = new double[n];
        for (int i = 0; i < n; i++) {
            tr[i] = temperature / tc[i];
        }

        double sigma;
        double epsilon;
        double[] alfai = new double[n];
        double omega;
        double psi;

        switch (eosType.toUpperCase()) {
            case "VDW" -> {
                sigma = 0;
                epsilon = 0;
                Arrays.fill(alfai, 1.0);
                omega = 1.0 / 8.0;
                psi = 27.0 / 64.0;
            }
            case "RK" -> {
                sigma = 1;
                epsilon = 0;
                for (int i = 0; i < n; i++) {
                    alfai[i] = 1.0 / Math.sqrt(tr[i]);
                }
                omega = 0.08664;
                psi = 0.42748;
            }
            case "SRK" -> {
                sigma = 1;
                epsilon = 0;
                for (int i = 0; i < n; i++) {
                    double kSrki = 0.480 + 1.574 * w[i] - 0.176 * Math.pow(w[i], 2);
                    alfai[i] = Math.pow(1 + kSrki * (1 - Math.sqrt(tr[i])), 2);
                }
                omega = 0.08664;
                psi = 0.42748;
            }
            case "PR" -> {
                sigma = 1 + Math.sqrt(2);
                epsilon = 1 - Math.sqrt(2);
                for (int i = 0; i < n; i++) {
                    double kPri = 0.37464 + 1.54226 * w[i] - 0.26992 * Math.pow(w[i], 2);
                    alfai[i] = Math.pow(1 + kPri * (1 - Math.sqrt(tr[i])), 2);
                }
                omega = 0.07780;
                psi = 0.45724;
            }
            default -> throw new IllegalArgumentException("Invalid EOS type. Choose from " + EOS_NAMES);
        }

        return new EosParameters(sigma, epsilon, alfai, omega, psi);
    }


    private double[] buildKijMatrix(double[] kijValues) {
        int n = w.length;
        double[][] matrix = new double[n][n];
        int cont = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (j > i && cont < kijValues.length) {
                    matrix[i][j] = kijValues[cont];
                    cont++;
                }
            }
        }

        double[] flattened = new double[n * n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                flattened[idx++] = matrix[i][j] + matrix[j][i];
            }
        }
        return flattened;
    }

    private MixtureResult mixtureRule() {
        EosParameters params = setEosParameters(this.temperature, this.eos);
        this.kij = buildKijMatrix(this.kij);

        int n = w.length;
        double[] ai = new double[n];
        double[] bi = new double[n];
        for (int i = 0; i < n; i++) {
            ai[i] = params.alfai()[i] * params.psi() * Math.pow(R, 2) * Math.pow(tc[i], 2) / pc[i];
            bi[i] = params.omega() * R * (tc[i] / pc[i]);
        }

        double b = 0;
        for (int i = 0; i < n; i++) {
            b += x[i] * bi[i];
        }

        double[] aij = new double[n * n];
        double a = 0;
        int cont = 0;
        for (int j = 0; j < n; j++) {
            for (int l = 0; l < n; l++) {
                aij[cont] = Math.sqrt(ai[j] * ai[l]) * (1 - this.kij[cont]);
                a += x[j] * x[l] * aij[cont];
                cont++;
            }
        }

        return new MixtureResult(a, b, aij, bi);
    }

    @Override
    public double density(String phase) {
        EosParameters params = setEosParameters(this.temperature, this.eos);
        MixtureResult mixture = mixtureRule();

        double a = mixture.a();
        double b = mixture.b();
        double sigma = params.sigma();
        double epsilon = params.epsilon();

        double beta = b * pressure / (R * temperature);
        double q = a / (b * R * temperature);

        double c3 = 1;
        double c2 = (sigma + epsilon - 1) * beta - 1;
        double c1 = ((epsilon * sigma * beta) - sigma - epsilon - (sigma * beta) - (epsilon * beta) + q) * beta;
        double c0 = -(((epsilon * sigma) + (epsilon * sigma * beta) + q) * Math.pow(beta, 2));

        double[] coefficientsLowToHigh = {c0, c1, c2, c3};
        LaguerreSolver solver = new LaguerreSolver();
        Complex[] roots = solver.solveAllComplex(coefficientsLowToHigh, 1.0);

        List<Double> realRoots = new ArrayList<>();
        for (Complex root : roots) {
            if (Math.abs(root.getImaginary()) < 1e-9) {
                realRoots.add(root.getReal());
            }
        }

        double[] molarVolumes = new double[realRoots.size()];
        for (int i = 0; i < realRoots.size(); i++) {
            molarVolumes[i] = realRoots.get(i) * R * temperature / pressure;
        }

        double mmMix = 0;
        for (int i = 0; i < mm.length; i++) {
            mmMix += mm[i] * x[i];
        }

        double density;
        if (molarVolumes.length == 1) {
            density = (mmMix / molarVolumes[0]) * 1e-03;
        } else {
            double selectedVolume;
            if (phase.equalsIgnoreCase("vapor")) {
                selectedVolume = Arrays.stream(molarVolumes).max().orElseThrow();
            } else if (phase.equalsIgnoreCase("liquid")) {
                selectedVolume = Arrays.stream(molarVolumes).min().orElseThrow();
            } else {
                throw new IllegalArgumentException("Such phase doesn't exist!");
            }
            density = (mmMix / selectedVolume) * 1e-03;
        }

        return density;
    }

}