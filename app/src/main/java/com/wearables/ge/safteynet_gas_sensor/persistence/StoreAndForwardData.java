package com.wearables.ge.safteynet_gas_sensor.persistence;

import java.io.Serializable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(primaryKeys = {"timestamp", "device_id"})
public class StoreAndForwardData implements Serializable {
    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "sent")
    public boolean sent;

    @ColumnInfo(name = "device_id")
    public String deviceId;

    @ColumnInfo(name = "header_line")
    public String headerLine;

    @ColumnInfo(name = "data_line")
    public String dataLine;
}
