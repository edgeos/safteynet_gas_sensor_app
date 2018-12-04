package com.wearables.ge.safteynet_gas_sensor.activities.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wearables.ge.safteynet_gas_sensor.R;
import com.wearables.ge.safteynet_gas_sensor.activities.main.MainTabbedActivity;

import java.util.ArrayList;
import java.util.List;

public class GasDeviceTabFragment extends Fragment {
    private static final String TAG = "Gas Device Tab Fragment";

    public static final String TAB_NAME = "Device";

    private TextView sampleRateView = null;
    private TextView logThresholdView = null;
    private TextView deviceName = null;

    View rootView;

    SeekBar logThresholdBar;

    public int alarmLevel;

    List<String> sensorList = new ArrayList<>();
    ArrayAdapter<String> spinnerArrayAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tab_gas_device, container, false);

        SeekBar frequencyBar = rootView.findViewById(R.id.frequency_bar);

        TextView frequencyView = rootView.findViewById(R.id.frequency_bar_text);

        frequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                frequencyView.setText(getString(R.string.frequency_value, progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                frequencyView.setText(getString(R.string.frequency_value, seekBar.getProgress()));

            }
        });

        SeekBar numSensorsBar = rootView.findViewById(R.id.num_sensors_bar);

        TextView numSensorsView = rootView.findViewById(R.id.num_sensors_bar_text);
        numSensorsView.setText(getString(R.string.num_sensors_value, numSensorsBar.getProgress()));

        numSensorsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                numSensorsView.setText(getString(R.string.num_sensors_value, progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                numSensorsView.setText(getString(R.string.num_sensors_value, seekBar.getProgress()));
                int numSensors = seekBar.getProgress();
                if(seekBar.getProgress() == 0){
                    numSensors = 1;
                    numSensorsBar.setProgress(1);
                    numSensorsView.setText(getString(R.string.num_sensors_value, 1));
                }
                if(numSensors > sensorList.size()){
                    for(int i = sensorList.size() + 1; i < numSensors + 1; i++){
                        String item = "Gas Sensor " + i;
                        sensorList.add(item);
                    }
                    spinnerArrayAdapter.notifyDataSetChanged();
                } else if(numSensors < sensorList.size()){
                    for(int i = sensorList.size() - 1; i > numSensors - 1; i--){
                        sensorList.remove(i);
                    }
                    spinnerArrayAdapter.notifyDataSetChanged();
                }
            }
        });

        Spinner gasSensorDropdown = rootView.findViewById(R.id.gas_sensor_dropdown);
        int numSensors = numSensorsBar.getProgress();
        for(int i = 1; i < numSensors + 1; i++){
            String itemName = "Gas Sensor " + i;
            sensorList.add(itemName);
        }

        spinnerArrayAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.spinner_item, sensorList);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        gasSensorDropdown.setAdapter(spinnerArrayAdapter);
        gasSensorDropdown.setOnItemSelectedListener(new GasDeviceTabFragment.CustomOnItemSelectedListener());

        int sampleRateStepSize = 25; // from 0 - 100 with increments of 25 points each
        SeekBar sampleRateBar = rootView.findViewById(R.id.sampleRateBar);
        sampleRateView = rootView.findViewById(R.id.sampleRateView);
        sampleRateView.setText(getString(R.string.update_rate_value, sampleRateBar.getProgress()));
        sampleRateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                int stepProgress = ((int)Math.round(progress/sampleRateStepSize))*sampleRateStepSize;
                seekBar.setProgress(stepProgress);
                if (seekBar.getProgress() > 0) {
                    sampleRateView.setText(getString(R.string.update_rate_value, seekBar.getProgress()));
                } else {
                    sampleRateView.setText(getString(R.string.auto_sample_off_message));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                //sampleRateView.setText("Sample Rate: " + seekBar.getProgress());
            }
        });

        logThresholdBar = rootView.findViewById(R.id.logThresholdBar);
        logThresholdView = rootView.findViewById(R.id.logThresholdView);
        logThresholdView.setText(getString(R.string.alarm_threshold, logThresholdBar.getProgress()));
        alarmLevel = logThresholdBar.getProgress();
        logThresholdBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                logThresholdView.setText(getString(R.string.alarm_threshold, progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));

                if(MainTabbedActivity.connectedDevice != null){
                    alert.setMessage("Are you sure you would like to set the voltage alarm threshold to " + seekBar.getProgress() + "?");

                    alert.setPositiveButton(R.string.dialog_accept_button_message, (dialog, whichButton) -> {
                        //((MainTabbedActivity)Objects.requireNonNull(getActivity())).mService.writeToVoltageAlarmConfigChar(GattAttributes.MESSAGE_TYPE_ALARM_THRESHOLD, String.valueOf(seekBar.getProgress()));
                        alarmLevel = seekBar.getProgress();
                    });

                    alert.setNegativeButton(R.string.dialog_cancel_button_message, (dialog, whichButton) -> {
                        logThresholdBar.setProgress(alarmLevel);
                        Log.d(TAG, "Alarm Threshold dialog closed");
                    });

                } else {
                    alert.setMessage("No device connected");
                }

                alert.show();
                logThresholdView.setText(getString(R.string.alarm_threshold, seekBar.getProgress()));

            }
        });

        // Device name shown at the top of the page
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(MainTabbedActivity.connectedDeviceName);

        return rootView;
    }

    public void displayDeviceName(String name){
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(name);
    }

    public void updateBatteryLevel(int batteryLevel){
        TextView batteryLevelView = rootView.findViewById(R.id.battery_level);
        if(batteryLevelView != null){
            batteryLevelView.setText(getString(R.string.battery_level, batteryLevel));
        }
    }

    public void updateTemperature(int temp){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.temperature);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.temperature, String.valueOf(temp)));
        }
    }

    public void updateHumidity(int humidity){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.humidity);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.humidity, String.valueOf(humidity)));
        }
    }

    public void updatePressure(int pressure){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.pressure);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.pressure, String.valueOf(pressure)));
        }
    }

    public void updateActiveGasSensor(String gasSensor){
        TextView activeGasSensorView = rootView.findViewById(R.id.active_gas_sensor);
        if(activeGasSensorView != null){
            activeGasSensorView.setText(getString(R.string.active_gas_sensor, gasSensor));
        }
    }

    public void updateGasSensorData(String data){
        TextView gasSensorDataView = rootView.findViewById(R.id.gas_sensor_data);
        if(gasSensorDataView != null){
            gasSensorDataView.setText(getString(R.string.gas_sensor_data, data));
        }
    }

    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            //Toast.makeText(parent.getContext(), "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }
}
