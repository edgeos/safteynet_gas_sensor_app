package com.wearables.ge.safteynet_gas_sensor.utils;

import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class GasSensorData {
    public static String TAG = "GasSensorData";

    public int gasSensor;
    public float gas_ppm;
    public float frequency;
    public float z_real;
    public float z_imaginary;

    public long time;

    public GasSensorData(String hexString){
        List<String> hexSplit = Arrays.asList(hexString.split("\\s+"));
        if(hexSplit.size() == 17){
            String gasSensorString = hexSplit.get(0);
            String gasPpmString = hexSplit.get(4) + hexSplit.get(3) + hexSplit.get(2) + hexSplit.get(1);
            String frequencyString = hexSplit.get(8) + hexSplit.get(7) + hexSplit.get(6) + hexSplit.get(5);
            String zrealString = hexSplit.get(12) + hexSplit.get(11) + hexSplit.get(10) + hexSplit.get(9);
            String zimaginaryString = hexSplit.get(16) + hexSplit.get(15) + hexSplit.get(14) + hexSplit.get(13);

            this.gasSensor = Integer.parseInt(gasSensorString, 16);

            /*Log.d(TAG, "gasSensorString: " + gasSensorString);
            Log.d(TAG, "gasPpmString: " + gasPpmString);
            Log.d(TAG, "frequencyString: " + frequencyString);
            Log.d(TAG, "zrealString: " + zrealString);
            Log.d(TAG, "zimaginaryString: " + zimaginaryString);*/

            long gasPPMLong = Long.parseLong(gasPpmString, 16);
            long frequencyLong = Long.parseLong(frequencyString, 16);
            long z_realLong = Long.parseLong(zrealString, 16);
            long z_imaginaryLong = Long.parseLong(zimaginaryString, 16);

            int gasPPMint = 0;
            int frequencyInt = 0;
            int z_realInt = 0;
            int z_imaginaryInt = 0;
            try{
                gasPPMint = (int) gasPPMLong;
                frequencyInt = (int) frequencyLong;
                z_realInt = (int) z_realLong;
                z_imaginaryInt = (int) z_imaginaryLong;
            } catch (Exception e){
                Log.d(TAG, "Unable to parse long to type int: " + e.getMessage());
            }

            this.gas_ppm = Float.intBitsToFloat(gasPPMint);
            this.frequency = Float.intBitsToFloat(frequencyInt);
            this.z_real = Float.intBitsToFloat(z_realInt);
            this.z_imaginary = Float.intBitsToFloat(z_imaginaryInt);

            this.time = Calendar.getInstance().getTimeInMillis();

            /*Log.d(TAG, "gasSensor: " + gasSensor);
            Log.d(TAG, "gas_ppm: " + gas_ppm);
            Log.d(TAG, "frequency: " + frequency);
            Log.d(TAG, "z_real: " + z_real);
            Log.d(TAG, "z_imaginary: " + z_imaginary);*/
        } else {
            Log.d(TAG, "Unexpected Gas data value size: " + hexSplit.size());
        }

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

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
