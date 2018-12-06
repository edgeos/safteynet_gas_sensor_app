package com.wearables.ge.safteynet_gas_sensor.activities.ui;

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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.wearables.ge.safteynet_gas_sensor.R;
import com.wearables.ge.safteynet_gas_sensor.utils.GasSensorData;
import com.wearables.ge.safteynet_gas_sensor.utils.TempHumidPressure;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GasHistoryTabFragment extends Fragment {
    private static final String TAG = "Gas History Tab Fragment";

    public static final String TAB_NAME = "History";

    private ScaleAnimation expandAnimation = new ScaleAnimation(1, 1, 0, 1);
    private ScaleAnimation collapseAnimation = new ScaleAnimation(1, 1, 1, 0);

    GraphView tempGraph;
    GraphView humidityGraph;
    GraphView pressureGraph;

    GraphView gasGraph1;
    GraphView gasGraph2;
    GraphView gasPpmGraph;

    View rootView;

    LineGraphSeries<DataPoint>  temperatureSeries = new LineGraphSeries<>();
    LineGraphSeries<DataPoint>  humiditySeries = new LineGraphSeries<>();
    LineGraphSeries<DataPoint>  pressureSeries = new LineGraphSeries<>();

    LineGraphSeries<DataPoint>  gasGraph1Series = new LineGraphSeries<>();
    LineGraphSeries<DataPoint>  gasGraph2Series = new LineGraphSeries<>();
    LineGraphSeries<DataPoint>  gasPpmSeries = new LineGraphSeries<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_tab_gas_history, container, false);
        initializeGasSensorGraphs();
        initializeTempHumidPressureGraphs();
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

    LabelFormatter simpleTimeLabel = new LabelFormatter() {
        @Override
        public String formatLabel(double value, boolean isValueX) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            if(isValueX){
                Date d = new Date((long) value);
                return (dateFormat.format(d));
            }
            return "" + (int) value;
        }

        @Override
        public void setViewport(Viewport viewport) {

        }
    };

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

        gasGraph1 = rootView.findViewById(R.id.gas_sensor_graph_1);
        Viewport gasGraph1Viewport = gasGraph1.getViewport();
        gasGraph1Viewport.setYAxisBoundsManual(true);
        gasGraph1Viewport.setXAxisBoundsManual(true);
        gasGraph1.addSeries(gasGraph1Series);
        GridLabelRenderer gas1GridLabel = gasGraph1.getGridLabelRenderer();
        gas1GridLabel.setHorizontalAxisTitle(getString(R.string.gas_sensor_graph_1_x_axis_label));
        gas1GridLabel.setVerticalAxisTitle(getString(R.string.gas_sensor_graph_1_y_axis_label));
        /*gas1GridLabel.setHumanRounding(false);
        gas1GridLabel.setNumHorizontalLabels(10);
        gas1GridLabel.setNumVerticalLabels(10);*/
        gasGraph1.getGridLabelRenderer().setLabelFormatter(simpleTimeLabel);

        gasGraph2 = rootView.findViewById(R.id.gas_sensor_graph_2);
        Viewport gasGraph2Viewport = gasGraph2.getViewport();
        gasGraph2Viewport.setYAxisBoundsManual(true);
        gasGraph2Viewport.setXAxisBoundsManual(true);
        gasGraph2.addSeries(gasGraph2Series);
        GridLabelRenderer gas2GridLabel = gasGraph2.getGridLabelRenderer();
        gas2GridLabel.setHorizontalAxisTitle(getString(R.string.gas_sensor_graph_2_x_axis_label));
        gas2GridLabel.setVerticalAxisTitle(getString(R.string.gas_sensor_graph_2_y_axis_label));
        gasGraph2.getGridLabelRenderer().setLabelFormatter(simpleTimeLabel);

        gasPpmGraph = rootView.findViewById(R.id.gas_sensor_graph_3);
        Viewport gasPpmGraphViewport = gasPpmGraph.getViewport();
        gasPpmGraphViewport.setYAxisBoundsManual(true);
        gasPpmGraphViewport.setXAxisBoundsManual(true);
        gasPpmGraph.addSeries(gasPpmSeries);
        GridLabelRenderer gasPpmGridLabel = gasPpmGraph.getGridLabelRenderer();
        gasPpmGridLabel.setHorizontalAxisTitle(getString(R.string.gas_ppm_graph_x_axis_label));
        gasPpmGridLabel.setVerticalAxisTitle(getString(R.string.gas_ppm_graph_y_axis_label));
        gasPpmGraph.getGridLabelRenderer().setLabelFormatter(simpleTimeLabel);
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

        tempGraph = rootView.findViewById(R.id.temperature_graph);
        Viewport tempGraphViewport = tempGraph.getViewport();
        tempGraphViewport.setYAxisBoundsManual(true);
        tempGraphViewport.setXAxisBoundsManual(true);
        tempGraph.addSeries(temperatureSeries);
        GridLabelRenderer tempGridLabel = tempGraph.getGridLabelRenderer();
        tempGridLabel.setHorizontalAxisTitle(getString(R.string.temperature_graph_x_axis_label));
        tempGridLabel.setVerticalAxisTitle(getString(R.string.temperature_graph_y_axis_label));
        tempGraph.getGridLabelRenderer().setLabelFormatter(simpleTimeLabel);

        humidityGraph = rootView.findViewById(R.id.humidity_graph);
        Viewport humidityGraphViewport = humidityGraph.getViewport();
        humidityGraphViewport.setYAxisBoundsManual(true);
        humidityGraphViewport.setXAxisBoundsManual(true);
        humidityGraph.addSeries(humiditySeries);
        GridLabelRenderer humidityGridLabel = humidityGraph.getGridLabelRenderer();
        humidityGridLabel.setHorizontalAxisTitle(getString(R.string.humidity_graph_x_axis_label));
        humidityGridLabel.setVerticalAxisTitle(getString(R.string.humidity_graph_y_axis_label));
        humidityGraph.getGridLabelRenderer().setLabelFormatter(simpleTimeLabel);

        pressureGraph = rootView.findViewById(R.id.pressure_graph);
        Viewport pressureGraphViewport = pressureGraph.getViewport();
        pressureGraphViewport.setYAxisBoundsManual(true);
        pressureGraphViewport.setXAxisBoundsManual(true);
        pressureGraph.addSeries(pressureSeries);
        GridLabelRenderer pressureGridLabel = pressureGraph.getGridLabelRenderer();
        pressureGridLabel.setHorizontalAxisTitle(getString(R.string.pressure_graph_x_axis_label));
        pressureGridLabel.setVerticalAxisTitle(getString(R.string.pressure_graph_y_axis_label));
        pressureGraph.getGridLabelRenderer().setLabelFormatter(simpleTimeLabel);
    }

    public void updateTempHumidityPressureGraph(TempHumidPressure tempHumidPressure){
        if(temperatureSeries == null || humiditySeries == null || pressureSeries == null){
            temperatureSeries = new LineGraphSeries<>();
            humiditySeries = new LineGraphSeries<>();
            pressureSeries = new LineGraphSeries<>();
        }
        temperatureSeries.appendData(new DataPoint(tempHumidPressure.getDate(), tempHumidPressure.getTemp()), false, 300);
        humiditySeries.appendData(new DataPoint(tempHumidPressure.getDate(), tempHumidPressure.getHumid()), false, 300);
        pressureSeries.appendData(new DataPoint(tempHumidPressure.getDate(), tempHumidPressure.getPres()), false, 300);

        if(tempGraph != null && humidityGraph != null && pressureGraph != null){
            tempGraph.getViewport().setMinX(temperatureSeries.getLowestValueX());
            tempGraph.getViewport().setMaxX(temperatureSeries.getHighestValueX());
            tempGraph.getViewport().setMinY(temperatureSeries.getLowestValueY());
            tempGraph.getViewport().setMaxY(temperatureSeries.getHighestValueY());

            humidityGraph.getViewport().setMinX(humiditySeries.getLowestValueX());
            humidityGraph.getViewport().setMaxX(humiditySeries.getHighestValueX());
            humidityGraph.getViewport().setMinY(humiditySeries.getLowestValueY());
            humidityGraph.getViewport().setMaxY(humiditySeries.getHighestValueY());

            pressureGraph.getViewport().setMinX(pressureSeries.getLowestValueX());
            pressureGraph.getViewport().setMaxX(pressureSeries.getHighestValueX());
            pressureGraph.getViewport().setMinY(pressureSeries.getLowestValueY());
            pressureGraph.getViewport().setMaxY(pressureSeries.getHighestValueY());
        }
    }

    public void updateGasGraphs(GasSensorData data){
        if(gasGraph1Series == null || gasGraph2Series == null || gasPpmSeries == null){
            gasGraph1Series = new LineGraphSeries<>();
            gasGraph2Series = new LineGraphSeries<>();
            gasPpmSeries = new LineGraphSeries<>();
        }
        gasGraph1Series.appendData(new DataPoint(data.getTime(), data.getZ_real()),  false, 60);
        gasGraph2Series.appendData(new DataPoint(data.getTime(), data.getZ_imaginary()),  false, 60);
        gasPpmSeries.appendData(new DataPoint(data.getTime(), data.getGas_ppm()),  false, 60);

        if(gasGraph1 != null && gasGraph2 != null && gasPpmGraph != null){
            gasGraph1.getViewport().setMinX(gasGraph1Series.getLowestValueX());
            gasGraph1.getViewport().setMaxX(gasGraph1Series.getHighestValueX());
            gasGraph1.getViewport().setMinY(gasGraph1Series.getLowestValueY());
            gasGraph1.getViewport().setMaxY(gasGraph1Series.getHighestValueY());

            gasGraph2.getViewport().setMinX(gasGraph2Series.getLowestValueX());
            gasGraph2.getViewport().setMaxX(gasGraph2Series.getHighestValueX());
            gasGraph2.getViewport().setMinY(gasGraph2Series.getLowestValueY());
            gasGraph2.getViewport().setMaxY(gasGraph2Series.getHighestValueY());

            gasPpmGraph.getViewport().setMinX(gasPpmSeries.getLowestValueX());
            gasPpmGraph.getViewport().setMaxX(gasPpmSeries.getHighestValueX());
            gasPpmGraph.getViewport().setMinY(gasPpmSeries.getLowestValueY());
            gasPpmGraph.getViewport().setMaxY(gasPpmSeries.getHighestValueY());
        }
    }
}
