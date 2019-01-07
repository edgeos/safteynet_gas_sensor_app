package com.wearables.ge.safteynet_gas_sensor.utils;

public class GasSensorDataItem {
    public int gasSensor;
    public float gas_ppm;
    public int frequency;
    public float z_real;
    public float z_imaginary;

    public GasSensorDataItem(int gasSensor, float ppm, int freq, float real, float imag){
        this.gasSensor = gasSensor;
        this.gas_ppm = ppm;
        this.frequency = freq;
        this.z_real = real;
        this.z_imaginary = imag;
    }

    public int getGasSensor() {
        return gasSensor;
    }

    public void setGasSensor(int gasSensor) {
        this.gasSensor = gasSensor;
    }

    public float getGas_ppm() {
        return gas_ppm;
    }

    public void setGas_ppm(float gas_ppm) {
        this.gas_ppm = gas_ppm;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public float getZ_real() {
        return z_real;
    }

    public void setZ_real(float z_real) {
        this.z_real = z_real;
    }

    public float getZ_imaginary() {
        return z_imaginary;
    }

    public void setZ_imaginary(float z_imaginary) {
        this.z_imaginary = z_imaginary;
    }
}
