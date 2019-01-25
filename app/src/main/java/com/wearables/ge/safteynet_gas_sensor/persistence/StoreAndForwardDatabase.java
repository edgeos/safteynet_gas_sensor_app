package com.wearables.ge.safteynet_gas_sensor.persistence;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {StoreAndForwardData.class}, version = 1)
public abstract class StoreAndForwardDatabase extends RoomDatabase {
    public abstract StoreAndForwardDataDao storeAndForwardDataDao();
}
