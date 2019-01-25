
package com.wearables.ge.safteynet_gas_sensor.activities.main;

import android.Manifest;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.wearables.ge.safteynet_gas_sensor.R;
import com.wearables.ge.safteynet_gas_sensor.activities.main.fragments.GasDeviceTabFragment;
import com.wearables.ge.safteynet_gas_sensor.activities.main.fragments.GasHistoryTabFragment;
import com.wearables.ge.safteynet_gas_sensor.activities.main.fragments.LoggingTabFragment;
import com.wearables.ge.safteynet_gas_sensor.activities.main.fragments.NewLoggingTabFragment;
import com.wearables.ge.safteynet_gas_sensor.activities.main.fragments.PairingTabFragment;
import com.wearables.ge.safteynet_gas_sensor.services.BluetoothService;
import com.wearables.ge.safteynet_gas_sensor.services.LocationService;
import com.wearables.ge.safteynet_gas_sensor.utils.BLEQueue;
import com.wearables.ge.safteynet_gas_sensor.utils.GasSensorData;
import com.wearables.ge.safteynet_gas_sensor.utils.GasSensorDataItem;
import com.wearables.ge.safteynet_gas_sensor.utils.GattAttributes;
import com.wearables.ge.safteynet_gas_sensor.utils.TempHumidPressure;

import java.util.Date;
import java.util.UUID;

public class MainTabbedActivity extends FragmentActivity implements ActionBar.TabListener {
    private static final String TAG = "Main Tabbed Activity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;

    private PairingTabFragment mPairingTabFragment;
    private GasDeviceTabFragment mGasDeviceTabFragment;
    //static LoggingTabFragment mLoggingTabFragment = new LoggingTabFragment();
    private NewLoggingTabFragment mLoggingTabFragment;
    private GasHistoryTabFragment mGasHistoryTabFragment;

    public static String ARG_SECTION_NUMBER = "section_number";

    boolean mBound;
    public BluetoothService mService;
    public static BluetoothDevice connectedDevice;

    public static String connectedDeviceName;
    public static String connectedDeviceAddress;

    public boolean devMode;
    public Menu menuBar;

    public static TempHumidPressure tempHumidPressure;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed_main);

        // Inititialize the fragments
        mPairingTabFragment = new PairingTabFragment();
        mGasDeviceTabFragment = new GasDeviceTabFragment();
        mLoggingTabFragment = new NewLoggingTabFragment();
        mGasHistoryTabFragment = new GasHistoryTabFragment();

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(), mGasDeviceTabFragment,
                mPairingTabFragment, mLoggingTabFragment, mGasHistoryTabFragment);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        //bind this activity to bluetooth service
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;

        // Make sure we have permissions to write to disk
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        //start location service
        //Location service is not an extension of the service class and doesn't need to be bound to.
        //This is because we don't need the location service to send updates to the UI.
        //We only need to grab the latest coordinates from the location service.
        LocationService.startLocationService(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        //when this activity is resumed, rebind the bluetooth service
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
        //and re-register the broadcast receiver
        registerReceiver(mGattUpdateReceiver, createIntentFilter());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //when the activity is destroyed, unbind the bluetooth service
        //and unregister the broadcast receiver
        if(mConnection != null){
            unbindService(mConnection);
            unregisterReceiver(mGattUpdateReceiver);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        this.menuBar = menu;
        return true;
    }

    //switch case logic for menu button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.device_id:
                Log.d(TAG, "device_id button pushed");
                showDeviceID();
                return true;
            case R.id.rename:
                Log.d(TAG, "rename button pushed");
                renameDevice();
                return true;
            case R.id.disconnect:
                //action for disconnect
                Log.d(TAG, "Disconnect button pushed");
                if(connectedDevice != null){
                    disconnectDevice();
                }
                return true;
            case R.id.logout:
                logout();
                return true;
            case R.id.dev_mode:
                Log.d(TAG, "dev_mode button pushed");
                //dev mode action
                switchModes();
                return true;
            default:
                Log.d(TAG, "No menu item found for " + item.getItemId());
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Opens a dialog for the user to enter a new name for the connected device.
     * Will call the writeToVoltageAlarmConfigChar method in the bluetooth service with a "rename" message type.
     */
    public void renameDevice(){
        //create alert dialog with custom alert style
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));

        //If there is no device connected, don't allow the user to enter a name
        if(connectedDevice != null){
            alert.setMessage(R.string.rename_device_modal_message);

            //create edit text dialog
            final EditText input = new EditText(this);
            //set the max length of the field to 16 characters, since the board can only take a rename up to a length of 16
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setTextColor(Color.WHITE);

            alert.setView(input);

            //the gas sensor does not have rename functionality right now so do nothing upon submit
            alert.setPositiveButton(R.string.dialog_accept_button_message, (dialog, whichButton) -> {
                //mService.writeToVoltageAlarmConfigChar(GattAttributes.MESSAGE_TYPE_RENAME, input.getText().toString());
            });

            alert.setNegativeButton(R.string.dialog_cancel_button_message, (dialog, whichButton) -> Log.d(TAG, "Rename Device dialog closed"));

        } else {
            alert.setMessage("No device Connected");
        }
        alert.show();
    }

    /**
     * Connect the selected bluetooth device and set the global variables for device name and connected device
     * @param device
     * @param deviceName
     */
    public void connectDevice(BluetoothDevice device, String deviceName){
        Log.d(TAG, "Attempting to connect to: " + deviceName);
        connectedDeviceName = deviceName;
        connectedDevice = device;
        connectedDeviceAddress = device.getAddress();
        mService.connectDevice(device);
        //once the device is connected, display the name in the device tab fragment
        mGasDeviceTabFragment.displayDeviceName(deviceName);
    }

    /**
     * Disconnect the connected bluetooth device
     */
    public void disconnectDevice(){
        mService.disconnectGattServer();
        connectedDevice = null;
        connectedDeviceName = null;
        //negate the switch for that device on the pairing page if it is visible
        Switch button = findViewById(R.id.connected_button);
        if(button != null){
            button.setChecked(false);
        }
    }

    /**
     * Show the MAC address of the connected device. If no device is connected, alert that to the user.
     */
    public void showDeviceID(){
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        if(connectedDevice != null){
            alert.setMessage(getString(R.string.show_device_id, connectedDeviceAddress));
        } else {
            alert.setMessage(getString(R.string.show_device_id, "No device connected"));
        }
        alert.show();
    }

    public void handleUnexpectedDisconnect(){
        Log.d(TAG, "handleUnexpectedDisconnect");
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        alert.setMessage("Unexpected Disconnect");
        alert.show();
    }

    /**
     * This method will switch modes from Dev(engineering) mode to normal mode and chang ethe menu item accordingly.
     * First, check if a device is connected and alert the user if no device is connected.
     * Write to the VoltageAlarmConfig characteristic with a "mode" write type to change the mode type.
     * Then change the menu item to the other option.
     */
    public void switchModes() {
        if(connectedDevice != null){
            MenuItem devModeItem = menuBar.findItem(R.id.dev_mode);
            if(!devMode){
                //mService.writeToVoltageAlarmConfigChar(GattAttributes.MESSAGE_TYPE_MODE, Character.toString((char) 2));
                devModeItem.setTitle(R.string.normal_mode_menu_item);
                devMode = true;
            } else {
                //mService.writeToVoltageAlarmConfigChar(GattAttributes.MESSAGE_TYPE_MODE, Character.toString((char) 1));
                devModeItem.setTitle(R.string.dev_mode_menu_item);
                devMode = false;
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
            alert.setMessage("No device connected");
            alert.show();
        }
    }

    /**
     * Logout of AWS instance and disconnect the device.
     */
    public void logout(){
        // Disconnect the device
        disconnectDevice();

        // Tell AWS to dump the credentials
        IdentityManager.getDefaultIdentityManager().signOut();
    }

    /**
     * Connection callback method for the bluetooth service
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "attempting bind to bluetooth service");
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mService = binder.getService();

            //register the broadcast receiver
            registerReceiver(mGattUpdateReceiver, createIntentFilter());

            Log.d(TAG, "Bluetooth service bound successfully");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "Bluetooth service disconnected");
            mBound = false;
        }
    };

    /**
     * Create custom intent filter for broadcasting messages from the bluetooth service to this activity
     * @return IntentFilter object
     */
    private static IntentFilter createIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_GAS_SENSOR_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_VOLTAGE_BAND_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * Broadcast receiver for getting messages back from the bluetooth service.
     * When a message is received, the message type is determined and appropriate action is taken.
     */
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothService.ACTION_GATT_SERVICES_DISCOVERED:
                        Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED broadcast received");
                        //good indication that the device is successfully connected
                        Toast.makeText(mPairingTabFragment.getContext(), "Device Connected", Toast.LENGTH_LONG).show();
                        mGasDeviceTabFragment.setConnectedMessage(true);
                        mService.setNotifyOnCharacteristics();
                        break;
                    case BluetoothService.ACTION_DATA_AVAILABLE:
                        int extraType = intent.getIntExtra(BluetoothService.EXTRA_TYPE, -1);
                        if (extraType == BLEQueue.ITEM_TYPE_READ) {
                            readAvailableData(intent);
                        }
                        break;
                    case BluetoothService.UNEXPECTED_DISCONNECT:
                        handleUnexpectedDisconnect();
                        break;
                }
            }
        }
    };

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private GasDeviceTabFragment mGasDeviceTabFragment;
        private PairingTabFragment mPairingTabFragment;
        private NewLoggingTabFragment mLoggingTabFragment;
        private GasHistoryTabFragment mGasHistoryTabFragment;

        AppSectionsPagerAdapter(FragmentManager fm, GasDeviceTabFragment gasDeviceTabFragment,
                                PairingTabFragment pairingTabFragment, NewLoggingTabFragment loggingTabFragment,
                                GasHistoryTabFragment historyTabFragment) {
            super(fm);

            // Save the fragments from the main activity
            this.mGasDeviceTabFragment = gasDeviceTabFragment;
            this.mPairingTabFragment = pairingTabFragment;
            this.mLoggingTabFragment = loggingTabFragment;
            this.mGasHistoryTabFragment = historyTabFragment;
        }

        @Override
        public Fragment getItem(int i) {

            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, i + 1);

            switch (i) {
                case 0:
                    mPairingTabFragment.setArguments(args);
                    return mPairingTabFragment;
                case 1:
                    mGasDeviceTabFragment.setArguments(args);
                    return mGasDeviceTabFragment;
                case 2:
                    mLoggingTabFragment.setArguments(args);
                    return mLoggingTabFragment;
                case 3:
                    mGasHistoryTabFragment.setArguments(args);
                    return mGasHistoryTabFragment;

                default:

                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return PairingTabFragment.TAB_NAME;
                case 1:
                    return GasDeviceTabFragment.TAB_NAME;
                case 2:
                    return LoggingTabFragment.TAB_NAME;
                case 3:
                    return GasHistoryTabFragment.TAB_NAME;

                default:
                    return "Section " + (position + 1);
            }
        }
    }

    /**
     * Worker method for reading bluetooth data sent from a characteristic.
     * This will read any data from any characteristic and parse it to the correct format.
     * @param intent
     */
    public void readAvailableData(Intent intent){
        //get the UUID of the incoming data
        UUID extraUuid = UUID.fromString(intent.getStringExtra(BluetoothService.EXTRA_UUID));
        //grab the raw data as a byte array
        byte[] extraData = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA);
        //sometimes the data is a single integer and we don't need to parse the byte array
        int extraIntData = intent.getIntExtra(BluetoothService.EXTRA_INT_DATA, 0);

        //stop here if there is no message to read
        if(extraData == null){
            Log.d(TAG, "No message parsed on characteristic.");
            return;
        }

        //here attempt to convert the byte array to a string
        //the incoming data from the voltage bang should be hexadecimal strings
        String value = null;
        try {
            final StringBuilder stringBuilder = new StringBuilder(extraData.length);
            for(byte byteChar : extraData){
                stringBuilder.append(String.format("%02x ", byteChar));
            }

            value = stringBuilder.toString();
        } catch (Exception e) {
            Log.e(TAG, "Unable to convert message bytes to string" + e.getMessage());
        }

        //if we were able to get a string value from the byte array message, parse it based on the UUID with it
        if(value != null){
            //for battery level, just show the battery level on the UI
            if(extraUuid.equals(GattAttributes.BATT_LEVEL_CHAR_UUID)){
                if(mGasDeviceTabFragment.isVisible()){
                    mGasDeviceTabFragment.updateBatteryLevel(extraIntData);
                }
                Log.d(TAG, "Battery level: " + extraIntData + "%");
            } else if(extraUuid.equals(GattAttributes.GAS_SENSOR_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "GAS_SENSOR_DATA value: " + value);
                //Gas sensor data, create a GasSensorData object out of the message
                GasSensorData data = new GasSensorData(value);
                showGasSensorData(data, data.getDate());

                //print all items for debugging
                /*for(GasSensorDataItem item : data.getSensorDataList()){
                    Log.d(TAG, "Sensor Number:" + item.getGasSensor());
                    Log.d(TAG, "Frequency:" + item.getFrequency());
                    Log.d(TAG, "z_real:" + item.getZ_real());
                    Log.d(TAG, "z_imaginary:" + item.getZ_imaginary());
                    Log.d(TAG, "gas ppm:" + item.getGas_ppm());
                }*/
            } else if(extraUuid.equals(GattAttributes.GAS_SENSOR_CONFIG_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "GAS_SENSOR_CONFIG_DATA value: " + value);
            } else if(extraUuid.equals(GattAttributes.TEMP_HUMIDITY_PRESSURE_DATA_CHARACTERISTIC_UUID)){
                tempHumidPressure = new TempHumidPressure(value);
                showTempHumidPressure(tempHumidPressure);
                Log.d(TAG, "TEMP_HUMIDITY_PRESSURE_DATA value: " + value);
            } else if(extraUuid.equals(GattAttributes.GAS_SENSOR_1_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "GAS_SENSOR_1_DATA value: " + value);
            } else if(extraUuid.equals(GattAttributes.GAS_SENSOR_2_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "GAS_SENSOR_2_DATA value: " + value);
            } else if(extraUuid.equals(GattAttributes.GAS_SENSOR_3_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "GAS_SENSOR_3_DATA value: " + value);
            } else if(extraUuid.equals(GattAttributes.GAS_SENSOR_4_DATA_CHARACTERISTIC_UUID)) {
                Log.d(TAG, "GAS_SENSOR_4_DATA value: " + value);
            } else {
                Log.d(TAG, "Received message: " + value + " with UUID: " + extraUuid);
            }
        }
    }

    public void showGasSensorData(GasSensorData datum, Date date){
        //format a text string to show on the UI and save to a log file
        String dateString = String.valueOf(date.getTime());
        //make sure temp/humid/pressure values are not null and get their values
        String temp = (tempHumidPressure != null) ? String.valueOf(tempHumidPressure.getTemp()) : "null";
        String humid = (tempHumidPressure != null) ? String.valueOf(tempHumidPressure.getHumid()) : "null";
        String pres = (tempHumidPressure != null) ? String.valueOf(tempHumidPressure.getPres()) : "null";
        //build the string that will be saved, add commas for CSV format
        StringBuilder message = new StringBuilder(dateString
                + ", " + temp
                + ", " + humid
                + ", " + pres);
        int gasSensorNum = 0;
        for(GasSensorDataItem item : datum.getSensorDataList()){
            if(gasSensorNum != item.getGasSensor()){
                gasSensorNum = item.getGasSensor();
                message
                        .append(", ").append(item.getGasSensor());
            }
            message
                    .append(", ").append(item.getFrequency())
                    .append(", ").append(item.getZ_real())
                    .append(", ").append(item.getZ_imaginary())
                    .append(", ").append(item.getGas_ppm());
        }
        mLoggingTabFragment.addItem(message.toString(), datum);

        //just use the first item in the list for now to graph
        //eventually will want to be able to dynamically chose which sensor is graphed
        GasSensorDataItem data = datum.getSensorDataList().get(0);
        mGasHistoryTabFragment.updateGasGraphs(datum);
        if(mGasDeviceTabFragment.activeSensor != data.getGasSensor()){
            mGasDeviceTabFragment.updateActiveGasSensor(data.getGasSensor());
        }
        if(mGasDeviceTabFragment.isVisible()){
            mGasDeviceTabFragment.updateZreal(String.valueOf(data.getZ_real()));
            mGasDeviceTabFragment.updateZimaginary(String.valueOf(data.getZ_imaginary()));
            mGasDeviceTabFragment.updateGasPpm(String.valueOf(data.getGas_ppm()));
        }
    }

    public void showTempHumidPressure(TempHumidPressure tempHumidPressure){
        if(tempHumidPressure.getDate() == null){
            return;
        }
        if(mGasDeviceTabFragment.isVisible()){
            mGasDeviceTabFragment.updateHumidity(tempHumidPressure.getHumid());
            mGasDeviceTabFragment.updateTemperature(tempHumidPressure.getTemp());
            mGasDeviceTabFragment.updatePressure(tempHumidPressure.getPres());
        }
        mGasHistoryTabFragment.updateTempHumidityPressureGraph(tempHumidPressure);
    }
}


