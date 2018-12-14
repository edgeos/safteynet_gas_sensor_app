package com.wearables.ge.safteynet_gas_sensor.activities.ui;

import android.Manifest;
import android.app.ProgressDialog;
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

        //set save file button
        Button scanAgainButton = rootView.findViewById(R.id.button1);
        scanAgainButton.setOnClickListener(v -> {
            Log.d(TAG, "Save file to device button pressed");
            saveFile();
        });

        //save to cloud button
        Button saveToCloudButton = rootView.findViewById(R.id.button2);
        saveToCloudButton.setOnClickListener(v -> {
            Log.d(TAG, "Save file to cloud button pressed");
            saveFileToCloud();
        });

        //clear log button
        Button clearLogButton = rootView.findViewById(R.id.button3);
        clearLogButton.setOnClickListener(v -> {
            Log.d(TAG, "Clear Log button pressed");
            clearLog();
        });

        //find local files button
        Button findFilesButton = rootView.findViewById(R.id.button4);
        findFilesButton.setOnClickListener(v -> {
            Log.d(TAG, "Find local files button pressed");
            findLocalFiles();
        });

        AWSMobileClient.getInstance().initialize(rootView.getContext()).execute();

        setRetainInstance(true);

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

    String savedFileName;
    public void saveFile(){
        //check for file write permissions
        if(ContextCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            return;
        }

        //if there is nothing in the current log, don't save anything
        if(lines.isEmpty()){
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));
            alert.setTitle("Current Log is empty");
            alert.show();
            return;
        }

        //use time epoch ms for filename
        Long time = Calendar.getInstance().getTimeInMillis();
        savedFileName = String.valueOf(time) + "_gas_sensor_log.txt";

        //log path for debugging
        Log.d(TAG, "Files Dir: " + path.getPath());
        Boolean pathCreated = true;

        //make sure path exists
        //it should, when this main class is created it should grab the downloads directory
        if(!path.exists()){
            Log.d(TAG, "Path doesn't exist");
            pathCreated = path.mkdirs();
        }
        if(!pathCreated){
            Log.d(TAG, "Unable to create path");
            return;
        }

        //create the file to save
        File file = new File(path, savedFileName);

        //write all the lines in the saved array to the file
        try {
            FileWriter writer = new FileWriter(file);
            //head the file with some device info
            String headerLine = "Device: " + MainTabbedActivity.connectedDevice.getAddress() + " Name: " + MainTabbedActivity.connectedDeviceName + System.lineSeparator();
            writer.append(headerLine);
            for(String line : lines){
                writer.append(line);
                writer.append(System.lineSeparator());
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean viewingOldFile = false;
    File selectedFile;
    String selectedFileName;
    public void findLocalFiles(){
        //check for file read/write permissions
        if(ContextCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
            return;
        }

        //get a list of all filed in the path directory
        File[] files = path.listFiles();
        List<String> optionsList = new ArrayList<>();

        //if no files were found send a message and return
        if(files == null){
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));
            alert.setTitle("No log files found in " + path.toString());
            alert.show();
            return;
        }

        //add each item to a list of options
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
            //grab the name of the file based o nthe index of the option selected
            selectedFileName = optionsList.get(which);
            selectedFile = new File(path, selectedFileName);
            //boolean for viewing an old file so the bluetooth service in the background doesn't update the list while you are viewing an old file
            viewingOldFile = true;
            try {
                FileInputStream is = new FileInputStream(selectedFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
                logEventsList.removeAllViews();
                while(line != null){
                    //Log.d(TAG, "Line read: " + line);
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
        lastUploadedFileName = null;
        selectedFile = null;
    }

    String lastUploadedFileName;
    public void saveFileToCloud() {

        //get AWS transfer utility class
        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(rootView.getContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();

        //the below scenario will likely be true when the user selects upload for a live log that hasn't been saved to the device
        if(selectedFile == null || !viewingOldFile){
            saveFile();
            selectedFileName = savedFileName;
            selectedFile = new File(path, savedFileName);
            if(selectedFileName == null){
               Log.d(TAG, "Something went wrong with local file save");
               return;
            }
        }

        if(!viewingOldFile && lastUploadedFileName != null && lastUploadedFileName.equals(selectedFile.getName())){
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));
            alert.setTitle("File already uploaded!");
            alert.show();
            return;
        }

        //upload action
        TransferObserver uploadObserver =
                transferUtility.upload(
                        selectedFileName,
                        selectedFile);

        //show a progress bar
        ProgressDialog progressDialog = new ProgressDialog(rootView.getContext());
        progressDialog.setMessage("Uploading file");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressNumberFormat(null);
        progressDialog.setProgressPercentFormat(null);
        progressDialog.show();

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    progressDialog.dismiss();
                    Toast.makeText(rootView.getContext(), "File Uploaded!", Toast.LENGTH_SHORT).show();

                    //now delete file since it has been successfully uploaded
                    lastUploadedFileName = selectedFile.getName();
                    Boolean fileDeleted = selectedFile.delete();
                    Log.d(TAG, "File successfully uploaded to cloud instance, local file deleted status: " + fileDeleted);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                progressDialog.setProgress(percentDone);
                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            Toast.makeText(rootView.getContext(), "File Uploaded!", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d(TAG, "Bytes Total: " + uploadObserver.getBytesTotal());
    }
}
