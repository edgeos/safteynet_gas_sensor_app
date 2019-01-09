/*
 * Copyright (c) 2017 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.wearables.ge.safteynet_gas_sensor.services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.wearables.ge.safteynet_gas_sensor.utils.BLEQueue;
import com.wearables.ge.safteynet_gas_sensor.utils.QueueItem;
import com.wearables.ge.safteynet_gas_sensor.utils.GattAttributes;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class BluetoothService extends Service {
    private static String TAG = "Bluetooth Service";

    public static BluetoothGatt connectedGatt;
    public static String deviceName;

    private final IBinder mBinder = new LocalBinder();

    private BLEQueue bleQueue = new BLEQueue();
    private boolean bleQueueIsFree = true;

    public final static String ACTION_GATT_SERVICES_DISCOVERED =        "com.wearables.ge.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_GAS_SENSOR_DISCOVERED =      "com.wearables.ge.ACTION_GATT_GAS_SENSOR_DISCOVERED";
    public final static String ACTION_GATT_VOLTAGE_BAND_DISCOVERED =    "com.wearables.ge.ACTION_GATT_VOLTAGE_BAND_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =                  "com.wearables.ge.ACTION_DATA_AVAILABLE";

    public final static String EXTRA_TYPE =                             "com.wearables.ge.EXTRA_TYPE";
    public final static String EXTRA_UUID =                             "com.wearables.ge.EXTRA_UUID";
    public final static String EXTRA_DATA =                             "com.wearables.ge.EXTRA_DATA";
    public final static String EXTRA_INT_DATA =                         "com.wearables.ge.EXTRA_INT_DATA";


    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Broadcast an update to the main activity.
     * This method just takes a string, which will generally be the SERVICES_AVAILABLE identifier.
     * @param action
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * The more commonly used method for broadcasting actual data to the main activity.
     * Takes action type which will usually be DATA_AVAILABLE.
     * Also adds the characteristic to be read and the type of item which is usually "read".
     * @param action
     * @param characteristic
     * @param itemType
     */
    private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic, int itemType) {
        Intent intent = new Intent(action);

        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        intent.putExtra(EXTRA_INT_DATA, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        intent.putExtra(EXTRA_TYPE, itemType);

        sendBroadcast(intent);
    }

    /**
     * Connects the service to the selected Bluetooth device.
     * Register the callback function and make the connection.
     * @param device
     */
    public void connectDevice(BluetoothDevice device) {
        BluetoothService.GattClientCallback gattClientCallback = new BluetoothService.GattClientCallback();
        device.fetchUuidsWithSdp();
        connectedGatt = device.connectGatt(this, false, gattClientCallback);
        Log.d(TAG, "Device " + deviceName + " connected");
    }

    /**
     * Attempts to disconnect from the connected bluetooth device.
     * Does nothing if no device is connected.
     */
    public void disconnectGattServer() {
        //disconnect
        Log.d(TAG, "Attempting to disconnect " + deviceName);
        if (connectedGatt != null) {
            Boolean refreshed = refreshDeviceCache(connectedGatt);
            Log.d(TAG, "Device cache refreshed: " + refreshed);
            connectedGatt.disconnect();
            connectedGatt.close();
            connectedGatt = null;
        } else {
            Log.d(TAG, "connectedGatt was null");
        }
    }

    /**
     * This method uses reflection (spooky) to reset the device cache on the mobile device
     * This is useful to refresh any cached data that may have changed since the BLE device restarted
     * For instance, a device rename may work, but the old name will be associated with the MAC address in the cache so the old one will be what is seen
     * As of now, there is no more elegant solution to this issue that reflectively clearing the cache
     * @param gatt
     * @return
     */
    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            Method localMethod = gatt.getClass().getMethod("refresh", new Class[0]);
            return (Boolean) localMethod.invoke(gatt, new Object[0]);
        }
        catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        return false;
    }

    /**
     * Callback method for the bluetooth connection
     * Listens for status changes and acts accordingly
     * See https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback for more information
     */
    private class GattClientCallback extends BluetoothGattCallback {

        /**
         * Triggered when the connection state of that gatt has changed
         * Usually just for connects or disconnects, but this is a good place to add a handler for unexpected disconnects
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                //if the connection is anything but successful, stop here and disconnect
                disconnectGattServer();
                return;
            }
            //here we check that the state change was a connection and not a disconnect
            //we can also add actions for connecting, and disconnecting
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //triggered when a device is connected

                //set global variables for connected device and device name
                if(gatt != null){
                    connectedGatt = gatt;
                    //refresh the device cache here
                    Boolean refreshed = refreshDeviceCache(connectedGatt);
                    Log.d(TAG, "Device cache refreshed: " + refreshed);
                    //set the variable for device name, use the MAC address if no name is available
                    deviceName = gatt.getDevice().getName() == null ? gatt.getDevice().getAddress() : gatt.getDevice().getName();
                    Log.d(TAG, "Device connected: " + deviceName);
                }

                //check for BLE services, if successful this will trigger onServicesDiscovered
                if (gatt != null) {
                    gatt.discoverServices();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer();
            }
        }

        /**
         * Triggered when services have been discovered on the connected gatt
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services discovered: ");
            if (status != BluetoothGatt.GATT_SUCCESS) {
                //if the service discovery was not successful, stop here
                Log.d(TAG, "Problem with BLE connection, status not successful: " + status);
                return;
            }

            //the following is just for debug purposes to see all UUIDs associated with the connected device
            for(BluetoothGattService service : gatt.getServices()){
                Log.d(TAG, "Found Service: " + service.getUuid());
                for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                    Log.d(TAG, "With characteristic: " + characteristic.getUuid());
                }
            }

            //here we broadcast a message to the main activity to send the device connected message to the UI
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            //once the UI has been update, MainTabbedActivity will call the setNotifyOnCharacteristics() method
            //we could just as well do that here, but it feels right to do after we send a response to the UI
        }

        /**
         * Triggered when a characteristic has been read explicitly
         * Not to be confused with onCharacteristicChanged which triggers on notifications
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "Characteristic read: " + characteristic.getUuid());
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, BLEQueue.ITEM_TYPE_READ);
            bleQueueIsFree = true;
            processQueue();
        }

        /**
         * Triggers when a characteristic on the connected BLE device has changed.
         * This is the most commonly used callback in this app, since most incoming data comes from changed characteristics which trigger a notify
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, BLEQueue.ITEM_TYPE_READ);
        }

        /**
         * Triggered when a descriptor is written to
         * @param gatt
         * @param descriptor
         * @param status
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            bleQueueIsFree = true;
            processQueue();
        }

        /**
         * Triggered when a characteristic is written to.
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, BLEQueue.ITEM_TYPE_READ);
            bleQueueIsFree = true;
            processQueue();
        }

    }

    /**
     * This function will set all available characteristics on the connected device to notify
     * Sometimes a characteristic is read/write only and can not be set to notify, these should just fail silently
     */
    public void setNotifyOnCharacteristics(){
        Log.d(TAG, "setNotifyOnCharacteristics() called");
        for (BluetoothGattService service : getSupportedGattServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                UUID uuid = characteristic.getUuid();
                Log.d(TAG, "Setting characteristic " + uuid + " to notify");
                setCharacteristicNotification(characteristic, true);
            }
        }
    }

    /**
     * Request a read on a given BluetoothGattCharacteristic. The read result is reported
     * asynchronously through the BluetoothGattCallback.onCharacteristicRead(BluetoothGatt, BluetoothGattCharacteristic, int
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            bleQueue.addRead(characteristic);
        }
        processQueue();
    }

    /**
     * Request a notifications on a given BluetoothGattCharacteristic. The read result is reported
     * asynchronously through the BluetoothGattCallback.onCharacteristicChanged(BluetoothGatt, BluetoothGattCharacteristic)
     * callback.
     *
     * @param characteristic The characteristic to read from.
     * @param enabled true to enable notifications, false to disable
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            bleQueue.addNotification(characteristic, enabled);
        }
        processQueue();
    }

    /**
     * Request a notifications on a given BluetoothGattCharacteristic. The read result is reported
     * asynchronously through the BluetoothGattCallback.onCharacteristicChanged(BluetoothGatt, BluetoothGattCharacteristic)
     * callback.
     *
     * @param characteristic The characteristic to read from.
     * @param data byte array with the data to write
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data) {
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            bleQueue.addWrite(characteristic, data);
        }
        processQueue();
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after BluetoothGatt.discoverServices() completes successfully.
     *
     * A List of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (connectedGatt == null) return null;

        return connectedGatt.getServices();
    }

    /**
     * Function that is handling the request queue.
     * To think about is that BLE on Android only can handle one request at the time.
     * Android do not handle this by itself..
     */
    private void processQueue() {
        if (bleQueueIsFree) {
            bleQueueIsFree = false;
            QueueItem queueItem = bleQueue.getNextItem();
            if (queueItem == null) {
                bleQueueIsFree = true;
                return;
            } else {
                boolean status = false;
                switch (queueItem.itemType) {
                    case BLEQueue.ITEM_TYPE_READ:
                        status = connectedGatt.readCharacteristic(queueItem.characteristic);
                        break;
                    case BLEQueue.ITEM_TYPE_WRITE:
                        status = connectedGatt.writeCharacteristic(queueItem.characteristic);
                        break;
                    case BLEQueue.ITEM_TYPE_NOTIFICATION:
                        System.out.println("Enable characteristic  notification: " + queueItem.characteristic.getUuid());
                        connectedGatt.setCharacteristicNotification(queueItem.characteristic, true);
                        BluetoothGattDescriptor descriptor = queueItem.characteristic.getDescriptor(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
                        if (descriptor != null) {
                            System.out.println("Enable descriptor notification: " + GattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            status = connectedGatt.writeDescriptor(descriptor);
                        } else {
                            status = false;
                        }
                        break;
                }
                if (!status) {
                    bleQueueIsFree = true;
                }
            }
        }
    }
}
