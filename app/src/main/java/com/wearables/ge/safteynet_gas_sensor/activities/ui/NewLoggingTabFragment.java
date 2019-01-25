package com.wearables.ge.safteynet_gas_sensor.activities.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.wearables.ge.safteynet_gas_sensor.R;
import com.wearables.ge.safteynet_gas_sensor.activities.main.MainTabbedActivity;
import com.wearables.ge.safteynet_gas_sensor.utils.GasSensorData;
import com.wearables.ge.safteynet_gas_sensor.utils.GasSensorDataItem;
import com.wearables.ge.safteynet_gas_sensor.utils.LogCollection;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class NewLoggingTabFragment extends Fragment {
    private static long MAX_LOGS = 50;
    public static String TAG = "LoggingTabFragment";
    public static String TAB_NAME = "Logging";

    private View rootView;
    private LinearLayout logEventsList;
    ProgressBar progressBar;
    ConstraintLayout mainLayout;

    private LogCollection logs;

    private String firstLine;
    private ArrayList<TextView> logViews;

    public NewLoggingTabFragment() {
        super();

        // Initialize the log collection
        logViews = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Cache the view and logs list for later
        rootView = inflater.inflate(R.layout.fragment_tab_logging, container, false);
        logEventsList = rootView.findViewById(R.id.logEventList);

        // Add the first line
        if (logs != null && logs.size() > 1) {
            addFirstLineToView();

            // Add any saved lines
            for (long i = 2; i < ((MAX_LOGS > logs.size()) ? logs.size() : MAX_LOGS); i++) {
                try {
                    synchronized (this) {
                        addLogToView(logs.read(i));
                    }
                } catch (IOException io) {
                    Log.e(TAG, "Unable to add log to view: " + io.getLocalizedMessage());
                }
            }
        }

        return rootView;
    }

    synchronized private void initializeLogs() {
        try {
            logs = new LogCollection(newLogName());
        } catch (IOException io) {
            // Not sure what to do here yet
            Log.d(TAG, io.getLocalizedMessage());
        }
    }

    private String newLogName() {
        //use time epoch ms for filename
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MMdd_HHmmss");
        final Date d = new Date();
        final String date = (dateFormat.format(d));
        List<String> macAdrsList = Arrays.asList(MainTabbedActivity.connectedDeviceAddress.split("\\s*:\\s*"));
        return "Sensor_" + macAdrsList.get(macAdrsList.size() - 2) + macAdrsList.get(macAdrsList.size() - 1) + "_" + date + ".txt";
    }


    public void addItem(String item, GasSensorData data) {
        // Check for file write permissions
        if (rootView != null) {
            if (ContextCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                if (ContextCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }

        // Add the header line to the file based on the incoming data
        if (firstLine == null) {
            firstLine = "TimeStamp, Temp, Humidity, Pressure";
            for (GasSensorDataItem obj : data.getSensorDataList()){
                int sensorNum = obj.getGasSensor();
                if (sensorNum != obj.getGasSensor()) {
                    firstLine += ", Gas Sensor " + Integer.toString(sensorNum);
                }
                firstLine += ", Frequency, Z', Z'', Gas PPM";
            }

            // If the logs collection has not been initialized yet, initialize it
            if (logs == null) {
                initializeLogs();
            }

            // Persist the data to the file and display it
            try {
                synchronized (this) {
                    logs.write(firstLine + "\n");
                }
            } catch (IOException io) {
                Log.e(TAG, "Unable to add first line to file: " + io.getLocalizedMessage());
            }
            addFirstLineToView();
        }

        // Add a new item to the list
        try {
            synchronized (this) {
                logs.write(item + "\n");
            }
        } catch (IOException io) {
            Log.e(TAG, "Unable to add log to list");
        }

        // If appropriate, add to the UI as well
        addLogToView(item);
    }

    private void addFirstLineToView() {
        // Add the first line to the view if it is not null
        if (rootView != null && logEventsList != null) {
            TextView firstLineView = new TextView(rootView.getContext());
            firstLineView.setText(firstLine);
            firstLineView.setGravity(Gravity.START);
            logEventsList.addView(firstLineView);
        }
    }

    private void addLogToView(String logLine) {
        // Add the log to the view and our array if it is not null
        if (rootView != null && logEventsList != null) {
            TextView textView = new TextView(rootView.getContext());
            textView.setText(logLine);
            textView.setGravity(Gravity.START);
            logViews.add(textView);
            logEventsList.addView(textView);

            // If we now have more views than we are allowed, remove an old one
            while (logViews.size() > MAX_LOGS) {
                final TextView logView = logViews.get(0);
                logView.setVisibility(View.GONE);
                logEventsList.removeView(logView);
                logViews.remove(logView);
            }
        }
    }
}
