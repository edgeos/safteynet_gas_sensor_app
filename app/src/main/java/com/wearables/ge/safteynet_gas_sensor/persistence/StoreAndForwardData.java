package com.wearables.ge.safteynet_gas_sensor.persistence;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"timestamp", "device_id"})
public class StoreAndForwardData implements Serializable {
    @NonNull
    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "sent")
    public boolean sent;

    @NonNull
    @ColumnInfo(name = "device_id")
    public String deviceId;

    @ColumnInfo(name = "data_line")
    public String dataLine;

    @Override
    public String toString() {
        // Add the device id to the data
        String newData = dataLine + "," + deviceId;

        // Trim any spaces and newlines from the data
        return newData.replaceAll("\\s", "");
    }
}
