package com.wearables.ge.safteynet_gas_sensor.services;

import android.app.IntentService;
import android.content.Intent;

import com.wearables.ge.safteynet_gas_sensor.persistence.StoreAndForwardData;
import com.wearables.ge.safteynet_gas_sensor.persistence.StoreAndForwardDatabase;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import androidx.annotation.Nullable;
import androidx.room.Room;

public class StoreAndForwardService extends IntentService {

    public static final String DATA_EXTRA_NAME = "STORE_AND_FORWARD_DATA";

    private static final String DATABASE_NAME = "store-and-forward";
    private static final long DEFAULT_ENTRY_LIMIT = 1024;
    private static final long DEFAULT_SAVE_WAIT_TIME_MS = 1000;
    private static final long DEFAULT_SYNC_WAIT_TIME_MS = 6000;

    private AtomicLong entryLimit;

    private List<StoreAndForwardData> list;
    private StoreAndForwardDatabase database;

    private AtomicLong saveWaitTimeMs;
    private AtomicLong syncWaitTimeMs;

    private Thread sendThread;
    private Thread syncThread;

    private boolean running;

    public StoreAndForwardService(String name) {
        super(name);

        // Set all the defaults
        entryLimit = new AtomicLong(DEFAULT_ENTRY_LIMIT);
        saveWaitTimeMs = new AtomicLong(DEFAULT_SAVE_WAIT_TIME_MS);
        syncWaitTimeMs = new AtomicLong(DEFAULT_SYNC_WAIT_TIME_MS);

        // Set up the database
        database = Room.databaseBuilder(getApplicationContext(), StoreAndForwardDatabase.class, DATABASE_NAME).build();

        // Delete all of the sent entries on disk
        database.storeAndForwardDataDao().deleteAllSent();

        // Get a chunk of the files on disk and load them into memory
        list = database.storeAndForwardDataDao().getNotSent(entryLimit.get());

        // Let the threads know that it is okay to start
        running = true;

        // This is a simple thread that will wait for a configurable time before attempting to send all available data
        sendThread = new Thread(() -> {
            while (running) {
                // Send the data to the cloud
                sendData();

                // Wait for a configurable amount of time
                try {
                    sendThread.wait(saveWaitTimeMs.get());
                } catch (InterruptedException i) {
                    // Do nothing
                }
            }
        });
        sendThread.start();

        // This is a simple thread that will wait for a configurable amount of time before attempting to flush all data to the disk database as well as clean the database
        syncThread = new Thread(() -> {
            while (running) {
                // Flush the data from the in memory database to the disk
                syncData();

                // Wait for a configurable amount of time
                try {
                    syncThread.wait(syncWaitTimeMs.get());
                } catch (InterruptedException i) {
                    // Do nothing
                }
            }
        });
        syncThread.start();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // There is no work to be done if there is not intent, so just return for now
        if (intent == null) {
            return;
        }

        // Pull out the data, and save it to the list
        final StoreAndForwardData data = (StoreAndForwardData) intent.getSerializableExtra(DATA_EXTRA_NAME);
        saveData(data);
    }

    synchronized private void sendData() {
        // Loop through all not sent data and send it
        for (final StoreAndForwardData data : list) {
            // TODO: Actually send the entries here
        }
    }

    synchronized private void saveData(StoreAndForwardData data) {
        // If the list is already too full, save the data to the database
        if (list.size() < entryLimit.get()) {
            list.add(data);
        } else {
            database.storeAndForwardDataDao().insert(data);
        }

        // Notify the sending thread that it should check again
        sendThread.notify();
    }

    synchronized private void syncData() {
        // Save all entries in the list to the database
        database.storeAndForwardDataDao().insertAll(list.toArray(new StoreAndForwardData[0]));

        // Delete all sent entries from the database
        database.storeAndForwardDataDao().deleteAllSent();

        // Refresh the list so it is synced with the database
        list = database.storeAndForwardDataDao().getNotSent(entryLimit.get());
    }
}
