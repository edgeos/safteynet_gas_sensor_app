package com.wearables.ge.safteynet_gas_sensor.asynctasks;

import android.os.AsyncTask;

import com.wearables.ge.safteynet_gas_sensor.activities.main.MainTabbedActivity;
import com.wearables.ge.safteynet_gas_sensor.utils.GasSensorData;

import java.util.ArrayList;
import java.util.List;

public class GasSensorDataTask extends AsyncTask<String, Integer, List<GasSensorData>> {
    // Do the long-running work in here
    protected List<GasSensorData> doInBackground(String... values) {
        List<GasSensorData> dataList = new ArrayList<>();
        for(String value : values){
            GasSensorData data = new GasSensorData(value);
            dataList.add(data);
        }
        return dataList;
    }

    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {

    }


    // This is called when doInBackground() is finished
    protected void onPostExecute(List<GasSensorData> result) {
        for(GasSensorData obj : result){
            MainTabbedActivity.showGasSensorData(obj);
        }
    }
}