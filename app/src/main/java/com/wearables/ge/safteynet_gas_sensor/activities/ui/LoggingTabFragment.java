package com.wearables.ge.safteynet_gas_sensor.activities.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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

    List<String> lines = new ArrayList<>();

    public void showFileText(){
        LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);

        for(String line : lines){
            TextView textView = new TextView(rootView.getContext());
            textView.setText(line);
            textView.setGravity(Gravity.START);
            logEventsList.addView(textView);
        }
    }

    public void addItem(String item){
        lines.add(item);
        if(rootView != null && !viewingOldFile){
            LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
            TextView textView = new TextView(rootView.getContext());
            textView.setText(item);
            textView.setGravity(Gravity.START);
            logEventsList.addView(textView);
        }
    }

    public void saveFile(){
        if(ContextCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            return;
        }
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

    boolean viewingOldFile = false;
    public void findLocalFiles(){
        if(ContextCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
            return;
        }
        File[] files = path.listFiles();
        List<String> optionsList = new ArrayList<>();
        if(files == null){
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));
            alert.setTitle("No log files found in " + path.toString());
            alert.show();
            return;
        }
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
            viewingOldFile = true;
            try {
                FileInputStream is = new FileInputStream(selectedFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
                logEventsList.removeAllViews();
                while(line != null){
                    Log.d(TAG, "Line read: " + line);
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
        viewingOldFile = false;
        LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
        logEventsList.removeAllViews();
        lines = new ArrayList<>();
    }
}
