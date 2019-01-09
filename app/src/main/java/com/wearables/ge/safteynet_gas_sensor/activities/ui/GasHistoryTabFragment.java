package com.wearables.ge.safteynet_gas_sensor.activities.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
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
    LineChart gasPpmGraph;

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_tab_gas_history, container, false);
        initializeGasSensorGraphs();
        initializeTempHumidPressureGraphs();
        setRetainInstance(true);
        return rootView;

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
        rightAxis.setEnabled(false);

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

        //ppm graph
        gasPpmGraph = rootView.findViewById(R.id.gas_sensor_graph_3);
        gasPpmGraph.setDragEnabled(true);
        gasPpmGraph.setScaleEnabled(true);
        gasPpmGraph.setDrawGridBackground(false);

        gasPpmGraph.setPinchZoom(true);

        LineData data3 = new LineData();
        data3.setValueTextColor(Color.RED);

        gasPpmGraph.setData(data3);

        XAxis xl3 = gasPpmGraph.getXAxis();
        xl3.setTypeface(Typeface.SANS_SERIF);
        xl3.setTextColor(Color.BLACK);
        xl3.setDrawGridLines(true);
        xl3.setEnabled(true);
        xl3.setDrawGridLines(true);
        xl3.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis3 = gasPpmGraph.getAxisLeft();
        leftAxis3.setTypeface(Typeface.SANS_SERIF);
        leftAxis3.setTextColor(Color.BLACK);
        leftAxis3.setDrawGridLines(true);

        YAxis rightAxis3 = gasPpmGraph.getAxisRight();
        rightAxis3.setEnabled(false);
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

    /*public class ZPrimeDateValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis){
            Log.d(TAG, "ZPrimeDateValueFormatter: x value: " + (int) value + " i value: " + i);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date d = gasSensorDataList.get((int) i - 1).getDate();
            //Date d = new Date();
            return (dateFormat.format(d));
        }
    }*/

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
    //public List<GasSensorData> gasSensorDataList = new ArrayList<>();
    public void updateGasGraphs(GasSensorData datum){
        //we want to graph all the data at once
        GasSensorDataItem data = datum.getSensorDataList().get(0);
        List<GasSensorDataItem> sensorData = new ArrayList<>();
        for(GasSensorDataItem obj : datum.getSensorDataList()){
            if(obj.getGasSensor() == 1){
                sensorData.add(obj);
            }
        }

        i++;
        if(gasGraph1 != null ){
            LineData data1 = gasGraph1.getData();
            if (data1 != null) {

                ILineDataSet set = data1.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet();
                    data1.addDataSet(set);
                }

                data1.addEntry(new Entry(i, data.getZ_real()), 0);
                data1.notifyDataChanged();

                // let the chart know it's data has changed
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

                ILineDataSet set = data2.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet();
                    data2.addDataSet(set);
                }

                data2.addEntry(new Entry(i, data.getZ_imaginary()), 0);
                data2.notifyDataChanged();

                gasGraph2.notifyDataSetChanged();

                gasGraph2.setVisibleXRangeMaximum(40);
                gasGraph2.moveViewToX(data2.getEntryCount());
            }
        }
        if(gasPpmGraph != null ){
            LineData data3 = gasPpmGraph.getData();
            if (data3 != null) {

                ILineDataSet set = data3.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet();
                    data3.addDataSet(set);
                }

                data3.addEntry(new Entry(i, data.getGas_ppm()), 0);
                data3.notifyDataChanged();

                gasPpmGraph.notifyDataSetChanged();

                gasPpmGraph.setVisibleXRangeMaximum(40);
                gasPpmGraph.moveViewToX(data3.getEntryCount());
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
                    set = createSet();
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
                    set = createSet();
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
                    set = createSet();
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

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Dynamic Data");
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
}
