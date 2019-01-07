package com.wearables.ge.safteynet_gas_sensor.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GasSensorData {
    private static String TAG = "GasSensorData";

    public Date date;

    private List<GasSensorDataItem> sensorDataList;

    public GasSensorData(String hexString){
        List<String> hexSplit = Arrays.asList(hexString.split("\\s+"));
        if((hexSplit.size() - 1) % 4 == 0){
            this.date = new Date();

            Integer totalMeasurements = Integer.parseInt(hexSplit.get(0), 16);

            sensorDataList = new ArrayList<>();

            for(int i = 0; i < totalMeasurements; i++){
                int messagePacketByteSize = 14;
                int x = messagePacketByteSize * i + 1;
                int gasSensor = Integer.parseInt(hexSplit.get(x), 16);
                Integer frequency = Integer.parseInt(hexSplit.get(x + 1), 16);

                String zrealString = hexSplit.get(x + 5) + hexSplit.get(x + 4) + hexSplit.get(x + 3) + hexSplit.get(x + 2);
                String zimaginaryString = hexSplit.get(x + 9) + hexSplit.get(x + 8) + hexSplit.get(x + 7) + hexSplit.get(x + 6);
                String gasPpmString = hexSplit.get(x + 13) + hexSplit.get(x + 12) + hexSplit.get(x + 11) + hexSplit.get(x + 10);

                long gasPPMLong = Long.parseLong(gasPpmString, 16);
                long z_realLong = Long.parseLong(zrealString, 16);
                long z_imaginaryLong = Long.parseLong(zimaginaryString, 16);

                int gasPPMint = 0;
                int z_realInt = 0;
                int z_imaginaryInt = 0;
                try{
                    gasPPMint = (int) gasPPMLong;
                    z_realInt = (int) z_realLong;
                    z_imaginaryInt = (int) z_imaginaryLong;
                } catch (Exception e){
                    Log.d(TAG, "Unable to parse long to type int: " + e.getMessage());
                }

                float gas_ppm = Float.intBitsToFloat(gasPPMint);
                float z_real = Float.intBitsToFloat(z_realInt);
                float z_imaginary = Float.intBitsToFloat(z_imaginaryInt);

                GasSensorDataItem sensorData = new GasSensorDataItem(gasSensor, gas_ppm, frequency, z_real, z_imaginary);
                sensorDataList.add(sensorData);
            }
        } else {
            Log.d(TAG, "Unexpected Gas data value size: " + hexSplit.size());
        }

    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<GasSensorDataItem> getSensorDataList() {
        return sensorDataList;
    }

    public void setSensorDataList(List<GasSensorDataItem> sensorDataList) {
        this.sensorDataList = sensorDataList;
    }
}
