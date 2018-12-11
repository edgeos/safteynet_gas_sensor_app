package com.wearables.ge.safteynet_gas_sensor.asynctasks;

import android.os.AsyncTask;

import com.wearables.ge.safteynet_gas_sensor.activities.main.MainTabbedActivity;
import com.wearables.ge.safteynet_gas_sensor.utils.TempHumidPressure;

import java.util.ArrayList;
import java.util.List;

public class TempHumidPressureTask extends AsyncTask<String, Integer, List<TempHumidPressure>> {
    // Do the long-running work in here
    protected List<TempHumidPressure> doInBackground(String... values) {
        List<TempHumidPressure> dataList = new ArrayList<>();
        for(String value : values){
            TempHumidPressure tempHumidPressure = new TempHumidPressure(value);
            dataList.add(tempHumidPressure);
        }
        return dataList;
    }

    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {

    }


    // This is called when doInBackground() is finished
    protected void onPostExecute(List<TempHumidPressure> result) {
        for(TempHumidPressure obj : result){
            MainTabbedActivity.showTempHumidPressure(obj);
        }
    }
}
