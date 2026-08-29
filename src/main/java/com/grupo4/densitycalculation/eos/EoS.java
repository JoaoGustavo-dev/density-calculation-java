package com.grupo4.densitycalculation.eos;

public abstract class EoS {

    protected String eos;
    protected double temperature;
    protected double pressure;
    protected double[] x;
    protected double[] kij;

    protected EoS(String eos, double temperature, double pressure, double[] x, double[] kij) {
        this.eos = eos;
        this.temperature = temperature;
        this.pressure = pressure;
        this.x = x;
        this.kij = kij;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setEos(String eos) {
        this.eos = eos.toUpperCase();
    }

    public void setKij(double[] kij) {
        this.kij = kij;
    }

    public abstract double density(String phase);
}