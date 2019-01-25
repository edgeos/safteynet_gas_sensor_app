package com.wearables.ge.safteynet_gas_sensor.persistence;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface StoreAndForwardDataDao {
    @Query("SELECT * FROM storeandforwarddata WHERE sent = 0 ORDER BY timestamp ASC LIMIT :limit")
    List<StoreAndForwardData> getNotSent(long limit);

    @Insert
    void insert(StoreAndForwardData data);

    @Insert
    void insertAll(StoreAndForwardData... data);

    @Query("DELETE FROM storeandforwarddata WHERE sent = 1")
    void deleteAllSent();
}
