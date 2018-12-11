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

    Boolean isConnected = false;

    public int activeSensor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tab_gas_device, container, false);

        SeekBar frequencyBar = rootView.findViewById(R.id.frequency_bar);

        TextView frequencyView = rootView.findViewById(R.id.frequency_bar_text);
        frequencyView.setText(getString(R.string.frequency_value, frequencyBar.getProgress()));

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

        SeekBar numSamplesBar = rootView.findViewById(R.id.num_sensors_bar);

        TextView numSamplesView = rootView.findViewById(R.id.num_sensors_bar_text);
        numSamplesView.setText(getString(R.string.num_samples_value, numSamplesBar.getProgress()));

        numSamplesBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                numSamplesView.setText(getString(R.string.num_samples_value, progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                numSamplesView.setText(getString(R.string.num_samples_value, seekBar.getProgress()));
            }
        });

        Spinner gasSensorDropdown = rootView.findViewById(R.id.gas_sensor_dropdown);
        int numSensors = 4;
        sensorList = new ArrayList<>();
        for(int i = 1; i < numSensors + 1; i++){
            String itemName = "Gas Sensor " + i;
            sensorList.add(itemName);
        }

        TextView activeGasSensorView = rootView.findViewById(R.id.active_gas_sensor);
        if(activeGasSensorView != null && activeSensor != 0){
            activeGasSensorView.setText(getString(R.string.active_gas_sensor, String.valueOf(activeSensor)));
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

        setConnectedMessage(isConnected);

        return rootView;
    }

    public void displayDeviceName(String name){
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(name);
    }

    public void setConnectedMessage(boolean status){
        if(rootView != null){
            TextView connectedStatusView = rootView.findViewById(R.id.gas_sensor_status);
            if(connectedStatusView != null){
                String message = status ? "Connected" : "Disconnected";
                connectedStatusView.setText(getString(R.string.gas_sensor_status, message));
            }
        }
        this.isConnected = status;
    }

    public void updateBatteryLevel(int batteryLevel){
        TextView batteryLevelView = rootView.findViewById(R.id.battery_level);
        if(batteryLevelView != null){
            batteryLevelView.setText(getString(R.string.battery_level, batteryLevel));
        }
    }

    public void updateTemperature(double temp){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.temperature);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.temperature, String.valueOf(temp)));
        }
    }

    public void updateHumidity(double humidity){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.humidity);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.humidity, String.valueOf(humidity)));
        }
    }

    public void updatePressure(double pressure){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.pressure);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.pressure, String.valueOf(pressure)));
        }
    }

    public void updateActiveGasSensor(int sensor){
        this.activeSensor = sensor;
        Spinner gasSensorDropdown = rootView.findViewById(R.id.gas_sensor_dropdown);
        gasSensorDropdown.setSelection(sensor - 1);
        TextView activeGasSensorView = rootView.findViewById(R.id.active_gas_sensor);
        if(activeGasSensorView != null){
            activeGasSensorView.setText(getString(R.string.active_gas_sensor, String.valueOf(sensor)));
        }
    }

    public void updateZreal(String data){
        TextView zRealView = rootView.findViewById(R.id.z_real);
        if(zRealView != null){
            zRealView.setText(getString(R.string.z_real, data));
        }
    }

    public void updateZimaginary(String data){
        TextView zImaginaryView = rootView.findViewById(R.id.z_imaginary);
        if(zImaginaryView != null){
            zImaginaryView.setText(getString(R.string.z_imaginary, data));
        }
    }

    public void updateGasPpm(String data){
        TextView gasPpmView = rootView.findViewById(R.id.gas_ppm);
        if(gasPpmView != null){
            gasPpmView.setText(getString(R.string.gas_ppm, data));
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
