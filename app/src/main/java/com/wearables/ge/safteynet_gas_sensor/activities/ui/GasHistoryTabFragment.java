package com.wearables.ge.safteynet_gas_sensor.activities.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.wearables.ge.safteynet_gas_sensor.R;
import com.wearables.ge.safteynet_gas_sensor.utils.GasSensorData;
import com.wearables.ge.safteynet_gas_sensor.utils.GasSensorDataItem;
import com.wearables.ge.safteynet_gas_sensor.utils.TempHumidPressure;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GasHistoryTabFragment extends Fragment {
    private static final String TAG = "GasHistoryTabFragment";

    public static final String TAB_NAME = "History";

    private ScaleAnimation expandAnimation = new ScaleAnimation(1, 1, 0, 1);
    private ScaleAnimation collapseAnimation = new ScaleAnimation(1, 1, 1, 0);

    LineChart tempGraph;
    LineChart humidityGraph;
    LineChart pressureGraph;

    LineChart gasGraph1;
    LineChart gasGraph2;

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_tab_gas_history, container, false);
        initializeGasSensorGraphs();
        initializeTempHumidPressureGraphs();
        setRetainInstance(true);

        Spinner sensor1DataDropdown = rootView.findViewById(R.id.sensor_1_data_select_dropdown);
        Spinner sensor2DataDropdown = rootView.findViewById(R.id.sensor_2_data_select_dropdown);
        Spinner sensor3DataDropdown = rootView.findViewById(R.id.sensor_3_data_select_dropdown);
        Spinner sensor4DataDropdown = rootView.findViewById(R.id.sensor_4_data_select_dropdown);

        sensor1DataDropdown.setOnItemSelectedListener(new Sensor1DataSelectorListener());
        sensor2DataDropdown.setOnItemSelectedListener(new Sensor2DataSelectorListener());
        sensor3DataDropdown.setOnItemSelectedListener(new Sensor3DataSelectorListener());
        sensor4DataDropdown.setOnItemSelectedListener(new Sensor4DataSelectorListener());

        return rootView;

    }

    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Log.d(TAG, "Item Selected: " + pos + " ID: " + id + " view ID: " + view.getId());
            /*selectedGasSensor = pos + 1;
            LineData data1 = gasGraph1.getData();
            if (data1 != null) {
                data1.clearValues();
                data1.notifyDataChanged();
                gasGraph1.notifyDataSetChanged();
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    private void updateSensorFrequencyDropdowns(List<Integer> sensor1Freqs, List<Integer> sensor2Freqs, List<Integer> sensor3Freqs, List<Integer> sensor4Freqs){
        Spinner sensor1FreqDropdown = rootView.findViewById(R.id.sensor_1_frequency_dropdown);
        ArrayAdapter sensor1ArrayAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.spinner_item, sensor1Freqs);
        sensor1ArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        sensor1FreqDropdown.setAdapter(sensor1ArrayAdapter);
        sensor1FreqDropdown.setOnItemSelectedListener(new Sensor1FrequencySelectorListener());

        Spinner sensor2FreqDropdown = rootView.findViewById(R.id.sensor_2_frequency_dropdown);
        ArrayAdapter sensor2ArrayAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.spinner_item, sensor2Freqs);
        sensor2ArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        sensor2FreqDropdown.setAdapter(sensor2ArrayAdapter);
        sensor2FreqDropdown.setOnItemSelectedListener(new Sensor2FrequencySelectorListener());

        Spinner sensor3FreqDropdown = rootView.findViewById(R.id.sensor_3_frequency_dropdown);
        ArrayAdapter sensor3ArrayAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.spinner_item, sensor3Freqs);
        sensor3ArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        sensor3FreqDropdown.setAdapter(sensor3ArrayAdapter);
        sensor3FreqDropdown.setOnItemSelectedListener(new Sensor3FrequencySelectorListener());

        Spinner sensor4FreqDropdown = rootView.findViewById(R.id.sensor_4_frequency_dropdown);
        ArrayAdapter sensor4ArrayAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.spinner_item, sensor4Freqs);
        sensor4ArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        sensor4FreqDropdown.setAdapter(sensor4ArrayAdapter);
        sensor4FreqDropdown.setOnItemSelectedListener(new Sensor4FrequencySelectorListener());
    }

    private void expandView(View view, long duration) {
        View parentContainer = view.getRootView();
        expandAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(params);
                parentContainer.requestLayout();
            }
            @Override
            public void onAnimationEnd(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        expandAnimation.setDuration(duration);

        view.startAnimation(expandAnimation);
    }


    private void collapseView(View view, long duration) {
        View parentContainer = view.getRootView();
        collapseAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = 0;
                view.setLayoutParams(params);
                parentContainer.requestLayout();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        collapseAnimation.setDuration(duration);
        view.startAnimation(collapseAnimation);
    }

    public void initializeGasSensorGraphs(){
        LinearLayout expandableLayout1 = rootView.findViewById(R.id.gasSensorCollapsibleContainer1);
        Switch switchButton1 = rootView.findViewById(R.id.gas_expand1);
        switchButton1.setChecked(true);
        switchButton1.setOnClickListener( v -> {
            if (switchButton1.isChecked()) {
                Toast.makeText(this.getContext(), "expanding...", Toast.LENGTH_LONG).show();
                expandView(expandableLayout1, 500);

            } else {
                Toast.makeText(this.getContext(), "collapsing...", Toast.LENGTH_LONG).show();
                collapseView(expandableLayout1, 500);
            }
        });

        //z prime graph
        gasGraph1 = rootView.findViewById(R.id.gas_sensor_graph_1);
        gasGraph1.setDragEnabled(true);
        gasGraph1.setScaleEnabled(true);
        gasGraph1.setDrawGridBackground(false);

        gasGraph1.setPinchZoom(true);

        LineData data1 = new LineData();
        data1.setValueTextColor(Color.RED);

        gasGraph1.setData(data1);

        XAxis xl = gasGraph1.getXAxis();
        xl.setTypeface(Typeface.SANS_SERIF);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setEnabled(true);
        xl.setDrawGridLines(true);
        xl.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis = gasGraph1.getAxisLeft();
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = gasGraph1.getAxisRight();
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setTextColor(Color.BLACK);
        rightAxis.setEnabled(true);

        //z double prime graph
        gasGraph2 = rootView.findViewById(R.id.gas_sensor_graph_2);
        gasGraph2.setDragEnabled(true);
        gasGraph2.setScaleEnabled(true);
        gasGraph2.setDrawGridBackground(false);

        gasGraph2.setPinchZoom(true);

        LineData data2 = new LineData();
        data2.setValueTextColor(Color.RED);

        gasGraph2.setData(data2);

        XAxis xl2 = gasGraph2.getXAxis();
        xl2.setTypeface(Typeface.SANS_SERIF);
        xl2.setTextColor(Color.BLACK);
        xl2.setDrawGridLines(true);
        xl2.setEnabled(true);
        xl2.setDrawGridLines(true);
        xl2.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis2 = gasGraph2.getAxisLeft();
        leftAxis2.setTypeface(Typeface.SANS_SERIF);
        leftAxis2.setTextColor(Color.BLACK);
        leftAxis2.setDrawGridLines(true);

        YAxis rightAxis2 = gasGraph2.getAxisRight();
        rightAxis2.setEnabled(false);
    }

    public class DateValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis){
            //Log.d(TAG, "x value: " + value + " i value: " + i);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date d = new Date();
            return (dateFormat.format(d));
        }
    }

    public void initializeTempHumidPressureGraphs(){
        LinearLayout expandableLayout3 = rootView.findViewById(R.id.collapsibleContainer3);
        Switch switchButton3 = rootView.findViewById(R.id.expand3);
        switchButton3.setChecked(true);
        switchButton3.setOnClickListener( v -> {
            if (switchButton3.isChecked()) {
                Toast.makeText(this.getContext(), "expanding...", Toast.LENGTH_LONG).show();
                expandView(expandableLayout3, 500);
            } else {
                Toast.makeText(this.getContext(), "collapsing...", Toast.LENGTH_LONG).show();
                collapseView(expandableLayout3, 500 );
            }
        });

        //temp graph
        tempGraph = rootView.findViewById(R.id.temperature_graph);
        tempGraph.setDragEnabled(true);
        tempGraph.setScaleEnabled(true);
        tempGraph.setDrawGridBackground(false);

        tempGraph.setPinchZoom(true);

        LineData tempData = new LineData();
        tempData.setValueTextColor(Color.RED);

        tempGraph.setData(tempData);

        XAxis tempX = tempGraph.getXAxis();
        tempX.setTypeface(Typeface.SANS_SERIF);
        tempX.setTextColor(Color.BLACK);
        tempX.setDrawGridLines(true);
        tempX.setEnabled(true);
        tempX.setDrawGridLines(true);
        tempX.setValueFormatter(new DateValueFormatter());

        YAxis leftAxisTemp = tempGraph.getAxisLeft();
        leftAxisTemp.setTypeface(Typeface.SANS_SERIF);
        leftAxisTemp.setTextColor(Color.BLACK);
        leftAxisTemp.setDrawGridLines(true);

        YAxis rightAxisTemp = tempGraph.getAxisRight();
        rightAxisTemp.setEnabled(false);

        //humidity graph
        humidityGraph = rootView.findViewById(R.id.humidity_graph);
        humidityGraph.setDragEnabled(true);
        humidityGraph.setScaleEnabled(true);
        humidityGraph.setDrawGridBackground(false);

        humidityGraph.setPinchZoom(true);

        LineData humidData = new LineData();
        humidData.setValueTextColor(Color.RED);

        humidityGraph.setData(humidData);

        XAxis humidX = humidityGraph.getXAxis();
        humidX.setTypeface(Typeface.SANS_SERIF);
        humidX.setTextColor(Color.BLACK);
        humidX.setDrawGridLines(true);
        humidX.setEnabled(true);
        humidX.setDrawGridLines(true);
        humidX.setValueFormatter(new DateValueFormatter());

        YAxis leftAxisHumid = humidityGraph.getAxisLeft();
        leftAxisHumid.setTypeface(Typeface.SANS_SERIF);
        leftAxisHumid.setTextColor(Color.BLACK);
        leftAxisHumid.setDrawGridLines(true);

        YAxis rightAxisHumid = humidityGraph.getAxisRight();
        rightAxisHumid.setEnabled(false);

        //pressure graph
        pressureGraph = rootView.findViewById(R.id.pressure_graph);
        pressureGraph.setDragEnabled(true);
        pressureGraph.setScaleEnabled(true);
        pressureGraph.setDrawGridBackground(false);

        pressureGraph.setPinchZoom(true);

        LineData presData = new LineData();
        presData.setValueTextColor(Color.RED);

        pressureGraph.setData(presData);

        XAxis presX = pressureGraph.getXAxis();
        presX.setTypeface(Typeface.SANS_SERIF);
        presX.setTextColor(Color.BLACK);
        presX.setDrawGridLines(true);
        presX.setEnabled(true);
        presX.setDrawGridLines(true);
        presX.setValueFormatter(new DateValueFormatter());

        YAxis leftAxisPres = pressureGraph.getAxisLeft();
        leftAxisPres.setTypeface(Typeface.SANS_SERIF);
        leftAxisPres.setTextColor(Color.BLACK);
        leftAxisPres.setDrawGridLines(true);

        YAxis rightAxisPres = pressureGraph.getAxisRight();
        rightAxisPres.setEnabled(false);
    }

    float i;
    int sensor1SelectedFreq = 0;
    int sensor2SelectedFreq = 0;
    int sensor3SelectedFreq = 0;
    int sensor4SelectedFreq = 0;

    int sensor1SelectedData = 0;
    int sensor2SelectedData = 0;
    int sensor3SelectedData = 0;
    int sensor4SelectedData = 0;

    List<Integer> sensor1frequencies = new ArrayList<>();
    List<Integer> sensor2frequencies = new ArrayList<>();
    List<Integer> sensor3frequencies = new ArrayList<>();
    List<Integer> sensor4frequencies = new ArrayList<>();

    Boolean frequenciesSet = false;

    public void updateGasGraphs(GasSensorData datum){
        List<Integer> sensor1frequencies = new ArrayList<>();
        List<Integer> sensor2frequencies = new ArrayList<>();
        List<Integer> sensor3frequencies = new ArrayList<>();
        List<Integer> sensor4frequencies = new ArrayList<>();

        GasSensorDataItem sensor1Data = null;
        GasSensorDataItem sensor2Data = null;
        GasSensorDataItem sensor3Data = null;
        GasSensorDataItem sensor4Data = null;

        for(GasSensorDataItem obj : datum.getSensorDataList()){
            if(obj.getGasSensor() == 1){
                if(!sensor1frequencies.contains(obj.getFrequency())){
                    sensor1frequencies.add(obj.frequency);
                }
            } else if(obj.getGasSensor() == 2){
                if(!sensor2frequencies.contains(obj.getFrequency())){
                    sensor2frequencies.add(obj.frequency);
                }
            } else if(obj.getGasSensor() == 3){
                if(!sensor3frequencies.contains(obj.getFrequency())){
                    sensor3frequencies.add(obj.frequency);
                }
            } else if(obj.getGasSensor() == 4){
                if(!sensor4frequencies.contains(obj.getFrequency())){
                    sensor4frequencies.add(obj.frequency);
                }
            }
        }

        Collections.sort(sensor1frequencies);
        Collections.sort(sensor2frequencies);
        Collections.sort(sensor3frequencies);
        Collections.sort(sensor4frequencies);

        for(GasSensorDataItem obj : datum.getSensorDataList()){
            if(obj.getGasSensor() == 1){
                if(obj.getFrequency() == sensor1frequencies.get(sensor1SelectedFreq)){
                    sensor1Data = obj;
                }
            } else if(obj.getGasSensor() == 2){
                if(obj.getFrequency() == sensor2frequencies.get(sensor2SelectedFreq)){
                    sensor2Data = obj;
                }
            } else if(obj.getGasSensor() == 3){
                if(obj.getFrequency() == sensor3frequencies.get(sensor3SelectedFreq)){
                    sensor3Data = obj;
                }
            } else if(obj.getGasSensor() == 4){
                if(obj.getFrequency() == sensor4frequencies.get(sensor4SelectedFreq)){
                    sensor4Data = obj;
                }
            }
        }

        if(rootView != null && !frequenciesSet){
            this.sensor1frequencies = sensor1frequencies;
            this.sensor2frequencies = sensor2frequencies;
            this.sensor3frequencies = sensor3frequencies;
            this.sensor4frequencies = sensor4frequencies;
            updateSensorFrequencyDropdowns(sensor1frequencies, sensor2frequencies, sensor3frequencies, sensor4frequencies);
            frequenciesSet = true;
        }

        i++;
        if(gasGraph1 != null ){
            LineData data1 = gasGraph1.getData();
            if (data1 != null) {

                int index = 0;
                if(sensor1Data != null){
                    ILineDataSet set = data1.getDataSetByIndex(index);
                    int sensor1yValue = getYvalue(sensor1Data);
                    if (set == null) {
                        String label = sensor1Data.getFrequency() + "kHz";
                        set = createSet(label);
                        ((LineDataSet) set).setColor(Color.RED);
                        ((LineDataSet) set).setLineWidth(4);
                        //set.setAxisDependency(YAxis.AxisDependency.LEFT);

                        set.addEntry(new Entry(i, sensor1yValue));
                        data1.addDataSet(set);
                    } else {
                        set.addEntry(new Entry(i, sensor1yValue));
                    }
                    index++;
                }

                if(sensor2Data != null){
                    ILineDataSet set = data1.getDataSetByIndex(index);
                    int sensor2yValue = getYvalue(sensor2Data);
                    if (set == null) {
                        String label = sensor2Data.getFrequency() + "kHz";
                        set = createSet(label);
                        ((LineDataSet) set).setColor(Color.BLUE);
                        ((LineDataSet) set).setLineWidth(2);
                        //set.setAxisDependency(YAxis.AxisDependency.RIGHT);

                        set.addEntry(new Entry(i, sensor2yValue));
                        data1.addDataSet(set);
                    } else {
                        set.addEntry(new Entry(i, sensor2yValue));
                    }
                }

                data1.notifyDataChanged();

                // let the chart know its data has changed
                gasGraph1.notifyDataSetChanged();

                // limit the number of visible entries
                gasGraph1.setVisibleXRangeMaximum(40);
                // chart.setVisibleYRange(30, AxisDependency.LEFT);

                // move to the latest entry
                gasGraph1.moveViewToX(data1.getEntryCount());
            }
        }
        if(gasGraph2 != null ){
            LineData data2 = gasGraph2.getData();
            if (data2 != null) {

                int index = 0;
                if(sensor3Data != null){
                    ILineDataSet set = data2.getDataSetByIndex(index);
                    int sensor3yValue = getYvalue(sensor3Data);
                    if (set == null) {
                        String label = sensor3Data.getFrequency() + "kHz";
                        set = createSet(label);
                        ((LineDataSet) set).setColor(Color.RED);
                        ((LineDataSet) set).setLineWidth(4);
                        //set.setAxisDependency(YAxis.AxisDependency.LEFT);

                        set.addEntry(new Entry(i, sensor3yValue));
                        data2.addDataSet(set);
                    } else {
                        set.addEntry(new Entry(i, sensor3yValue));
                    }
                    index++;
                }

                if(sensor4Data != null){
                    ILineDataSet set = data2.getDataSetByIndex(index);
                    int sensor4yValue = getYvalue(sensor4Data);
                    if (set == null) {
                        String label = sensor4Data.getFrequency() + "kHz";
                        set = createSet(label);
                        ((LineDataSet) set).setColor(Color.BLUE);
                        ((LineDataSet) set).setLineWidth(2);
                        //set.setAxisDependency(YAxis.AxisDependency.RIGHT);

                        set.addEntry(new Entry(i, sensor4yValue));
                        data2.addDataSet(set);
                    } else {
                        set.addEntry(new Entry(i, sensor4yValue));
                    }
                }

                data2.notifyDataChanged();

                gasGraph2.notifyDataSetChanged();

                gasGraph2.setVisibleXRangeMaximum(40);
                gasGraph2.moveViewToX(data2.getEntryCount());
            }
        }
    }

    float i2 = 0;
    public void updateTempHumidityPressureGraph(TempHumidPressure tempHumidPressure){
        i2++;
        if(tempGraph != null ){
            LineData data1 = tempGraph.getData();
            if (data1 != null) {

                ILineDataSet set = data1.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("temperature");
                    data1.addDataSet(set);
                }

                data1.addEntry(new Entry(i2, (float) tempHumidPressure.getTemp()), 0);
                data1.notifyDataChanged();

                tempGraph.notifyDataSetChanged();

                tempGraph.setVisibleXRangeMaximum(40);
                tempGraph.moveViewToX(data1.getEntryCount());
            }
        }
        if(humidityGraph != null ){
            LineData data2 = humidityGraph.getData();
            if (data2 != null) {

                ILineDataSet set = data2.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("humidity");
                    data2.addDataSet(set);
                }

                data2.addEntry(new Entry(i2, (float) tempHumidPressure.getHumid()), 0);
                data2.notifyDataChanged();

                humidityGraph.notifyDataSetChanged();

                humidityGraph.setVisibleXRangeMaximum(40);
                humidityGraph.moveViewToX(data2.getEntryCount());
            }
        }
        if(pressureGraph != null ){
            LineData data3 = pressureGraph.getData();
            if (data3 != null) {

                ILineDataSet set = data3.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("pressure");
                    data3.addDataSet(set);
                }

                data3.addEntry(new Entry(i2, (float) tempHumidPressure.getPres()), 0);
                data3.notifyDataChanged();

                pressureGraph.notifyDataSetChanged();

                pressureGraph.setVisibleXRangeMaximum(40);
                pressureGraph.moveViewToX(data3.getEntryCount());
            }
        }
    }


    private LineDataSet createSet(String label) {
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setCircleHoleColor(set.getColor());
        set.setLineWidth(2f);
        set.setCircleRadius(3);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    public int getYvalue(GasSensorDataItem data){
        if(data.getGasSensor() == 1){
            switch (sensor1SelectedData){
                case 0:
                    return (int) data.getZ_real();
                case 1:
                    return (int) data.getZ_imaginary();
                case 3:
                    return (int) data.getGas_ppm();
            }
        } else if(data.getGasSensor() == 2){
            switch (sensor2SelectedData){
                case 0:
                    return (int) data.getZ_real();
                case 1:
                    return (int) data.getZ_imaginary();
                case 3:
                    return (int) data.getGas_ppm();
            }
        } else if(data.getGasSensor() == 3){
            switch (sensor3SelectedData){
                case 0:
                    return (int) data.getZ_real();
                case 1:
                    return (int) data.getZ_imaginary();
                case 3:
                    return (int) data.getGas_ppm();
            }
        } else if(data.getGasSensor() == 4){
            switch (sensor4SelectedData){
                case 0:
                    return (int) data.getZ_real();
                case 1:
                    return (int) data.getZ_imaginary();
                case 3:
                    return (int) data.getGas_ppm();
            }
        }
        return 0;
    }

    public class Sensor1FrequencySelectorListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            sensor1SelectedFreq = pos;

            /*LineData data = gasGraph1.getData();
            if (data != null) {
                *//*ILineDataSet dataSet = data.getDataSetByIndex(0);
                dataSet.clear();
                data.notifyDataChanged();
                gasGraph1.notifyDataSetChanged();*//*
                data.clearValues();
                data.notifyDataChanged();
                gasGraph1.notifyDataSetChanged();
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    public class Sensor2FrequencySelectorListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            sensor2SelectedFreq = pos;

            /*LineData data = gasGraph1.getData();
            if (data != null) {
                data.clearValues();
                data.notifyDataChanged();
                gasGraph1.notifyDataSetChanged();
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    public class Sensor3FrequencySelectorListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            sensor3SelectedFreq = pos;

            /*LineData data = gasGraph2.getData();
            if (data != null) {
                data.clearValues();
                data.notifyDataChanged();
                gasGraph2.notifyDataSetChanged();
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    public class Sensor4FrequencySelectorListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            sensor4SelectedFreq = pos;

            /*LineData data = gasGraph2.getData();
            if (data != null) {
                data.clearValues();
                data.notifyDataChanged();
                gasGraph2.notifyDataSetChanged();
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    public class Sensor1DataSelectorListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            sensor1SelectedData = pos;

            /*LineData data = gasGraph1.getData();
            if (data != null) {
                data.clearValues();
                data.notifyDataChanged();
                gasGraph1.notifyDataSetChanged();
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    public class Sensor2DataSelectorListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            sensor2SelectedData = pos;

            /*LineData data = gasGraph1.getData();
            if (data != null) {
                data.clearValues();
                data.notifyDataChanged();
                gasGraph1.notifyDataSetChanged();
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    public class Sensor3DataSelectorListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            sensor3SelectedData = pos;

            /*LineData data = gasGraph2.getData();
            if (data != null) {
                data.clearValues();
                data.notifyDataChanged();
                gasGraph2.notifyDataSetChanged();
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    public class Sensor4DataSelectorListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            sensor4SelectedData = pos;

            /*LineData data = gasGraph2.getData();
            if (data != null) {
                data.clearValues();
                data.notifyDataChanged();
                gasGraph2.notifyDataSetChanged();
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }
}
