package com.wearables.ge.safteynet_gas_sensor.activities.ui;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wearables.ge.safteynet_gas_sensor.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class LoggingTabFragment extends Fragment {
    public static String TAG = "LoggingTabFragment";
    public static String TAB_NAME = "Logging";
    View rootView;

    File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "gas_sensor");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_tab_logging, container, false);

        showFileText();

        Button scanAgainButton = rootView.findViewById(R.id.button1);
        scanAgainButton.setOnClickListener(v -> {
            Log.d(TAG, "Save file to device button pressed");
            saveFile();
        });

        Button clearLogButton = rootView.findViewById(R.id.button3);
        clearLogButton.setOnClickListener(v -> {
            Log.d(TAG, "Clear Log button pressed");
            clearLog();
        });

        Button findFilesButton = rootView.findViewById(R.id.button4);
        findFilesButton.setOnClickListener(v -> {
            Log.d(TAG, "Find local files button pressed");
            findLocalFiles();
        });

        return rootView;
    }

    List<String> lines = Arrays.asList("Test Data Line 1",
            "Test Data Line 2",
            "Test Data Line 3",
            "Test Data Line 4",
            "Test Data Line 5",
            "Test Data Line 6",
            "Test Data Line 7");

    public void showFileText(){
        LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);

        for(String line : lines){
            TextView textView = new TextView(rootView.getContext());
            textView.setText(line);
            textView.setGravity(Gravity.START);
            logEventsList.addView(textView);
        }
    }

    public void saveFile(){
        if(lines.isEmpty()){
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));
            alert.setTitle("Current Log is empty");
            alert.show();
            return;
        }
        Long time = Calendar.getInstance().getTimeInMillis();
        String filename = String.valueOf(time) + "_gas_sensor_log.txt";

        Log.d(TAG, "Files Dir: " + path.getPath());
        Boolean pathCreated = true;
        if(!path.exists()){
            Log.d(TAG, "Path doesn't exist");
            pathCreated = path.mkdirs();
        }
        if(!pathCreated){
            Log.d(TAG, "Unable to create path");
            return;
        }
        File file = new File(path, filename);
        try {
            FileWriter writer = new FileWriter(file);
            for(String line : lines){
                writer.append(line + System.lineSeparator());
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findLocalFiles(){
        File[] files = path.listFiles();
        List<String> optionsList = new ArrayList<>();
        for(File file : files){
            Log.d(TAG, "File: " + file.getName());
            optionsList.add(file.getName());
        }

        String[] optionsArray = new String[optionsList.size()];
        optionsList.toArray(optionsArray);

        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));
        alert.setTitle("Select a file");
        alert.setItems(optionsArray, (dialog, which) -> {
            Log.d(TAG, "Chose option #" + which + " filename: " + optionsList.get(which));
            File selectedFile = new File(path, optionsList.get(which));
            try {
                FileInputStream is = new FileInputStream(selectedFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
                logEventsList.removeAllViews();
                lines = new ArrayList<>();
                while(line != null){
                    Log.d(TAG, "Line read: " + line);
                    lines.add(line);
                    TextView textView = new TextView(rootView.getContext());
                    textView.setText(line);
                    textView.setGravity(Gravity.START);
                    logEventsList.addView(textView);
                    line = reader.readLine();
                }
            } catch (Exception e) {
               Log.d(TAG, "Unable to read file: " + e.getMessage());
            }
        });
        alert.show();
    }

    public void clearLog(){
        LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
        logEventsList.removeAllViews();
        lines = new ArrayList<>();
    }
}
