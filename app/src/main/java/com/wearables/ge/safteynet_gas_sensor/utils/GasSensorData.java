package com.wearables.ge.safteynet_gas_sensor.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class GasSensorData {
    private static String TAG = "GasSensorData";
    private static int messagePacketByteSize = 14;

    public Date date;

    private List<GasSensorDataItem> sensorDataList;

    /**
     * Incoming Gas Sensor data is received in 255 byte packets which is translated to a hexadecimal string in the main broadcast receiver
     * and sent here as a large hexadecimal string.
     * The first byte in the string indicates how many measurements there are in the whole message.
     * The measurements are sent in 14 byte packets, with the following structure:
     *
     * 1st byte - gas sensor number
     * 2nd byte - frequency
     * 3rd - 6th bytes - Z_real (float, LSB first)
     * 7th - 10th bytes - Z_imaginary (float, LSB first)
     * 11th - 14th bytes - Gas PPM (float, LSB first)
     *
     * This hex string is broken up and parsed in the method below to create a GasSensorData object
     * @param hexString
     */
    public GasSensorData(String hexString){
        //split the hex string into a list of each byte
        List<String> hexSplit = Arrays.asList(hexString.split("\\s+"));
        //make sure the list size is divisible by the size in bytes of each individual measurement message
        if((hexSplit.size() - 1) % messagePacketByteSize == 0){
            this.date = new Date();

            //the total number of measurements should be the first item in the list
            Integer totalMeasurements = Integer.parseInt(hexSplit.get(0), 16);

            sensorDataList = new ArrayList<>();

            //now we will loop through the list and extract each measurement
            for(int i = 0; i < totalMeasurements; i++){
                //since we are indexing a list, the messagePacketByteSize is the index step size
                int x = messagePacketByteSize * i + 1;
                //gas sensor number is the first byte
                int gasSensor = Integer.parseInt(hexSplit.get(x), 16);
                //frequency is the second byte
                Integer frequency = Integer.parseInt(hexSplit.get(x + 1), 16);

                //Z_real is the 3rd through 6th bytes, reverse the order here since it is a little-endian float
                String zRealString = hexSplit.get(x + 5) + hexSplit.get(x + 4) + hexSplit.get(x + 3) + hexSplit.get(x + 2);
                //Z_imaginary is the 7th through 10th bytes
                String zImaginaryString = hexSplit.get(x + 9) + hexSplit.get(x + 8) + hexSplit.get(x + 7) + hexSplit.get(x + 6);
                //finally the Gas PPM is the last four bytes
                String gasPpmString = hexSplit.get(x + 13) + hexSplit.get(x + 12) + hexSplit.get(x + 11) + hexSplit.get(x + 10);

                //Getting a proper float out of these values can be a little wonky
                //first, parse the string to a long
                long gasPPMLong = Long.parseLong(gasPpmString, 16);
                long z_realLong = Long.parseLong(zRealString, 16);
                long z_imaginaryLong = Long.parseLong(zImaginaryString, 16);

                //then initialize ints (in case the method in the try/catch fails
                int gasPPMint = 0;
                int z_realInt = 0;
                int z_imaginaryInt = 0;
                try{
                    //then cast the longs to ints
                    gasPPMint = (int) gasPPMLong;
                    z_realInt = (int) z_realLong;
                    z_imaginaryInt = (int) z_imaginaryLong;
                } catch (Exception e){
                    Log.d(TAG, "Unable to parse long to type int: " + e.getMessage());
                }

                //finally use Float.intBitsToFloat to convert the values to a float
                float gas_ppm = Float.intBitsToFloat(gasPPMint);
                float z_real = Float.intBitsToFloat(z_realInt);
                float z_imaginary = Float.intBitsToFloat(z_imaginaryInt);

                GasSensorDataItem sensorData = new GasSensorDataItem(gasSensor, gas_ppm, frequency, z_real, z_imaginary);
                sensorDataList.add(sensorData);
            }

            Collections.sort(sensorDataList, new CustomComparator());

        } else {
            Log.d(TAG, "Unexpected Gas data value size: " + hexSplit.size());
        }
    }

    public class CustomComparator implements Comparator<GasSensorDataItem>{
        @Override
        public int compare(GasSensorDataItem o1, GasSensorDataItem o2){
            return o1.getGasSensor() - o2.getGasSensor();
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

    public String getHeaderLine() {
        String headerLine = "TimeStamp, Temp, Humidity, Pressure";

        for (GasSensorDataItem obj : sensorDataList) {
            int sensorNum = 0;
            if (sensorNum != obj.getGasSensor()) {
                sensorNum = obj.getGasSensor();
                headerLine += ", Gas Sensor " + Integer.toString(sensorNum);
            }
            headerLine += ", Frequency, Z', Z'', Gas PPM";
        }

        return headerLine;
    }
}
