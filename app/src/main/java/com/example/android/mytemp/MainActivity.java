package com.example.android.mytemp;

import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<TemperatureData>, OnRefreshListener {

    private TextView tempTextView;
    private TextView timestampTextView;
    private TextView deviceNameTextView;
    private LineChart tempHistchart;
    private LineDataSet dataSet;
    private LineData lineData;
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Constant value for the temperature loader ID.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int TEMPERATURE_LOADER_ID = 1;

    private static final String LOG_TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate: ");
        setContentView(R.layout.activity_main);

        tempTextView = (TextView) findViewById(R.id.temperature_text_view);
        timestampTextView = (TextView) findViewById(R.id.timestamp_text_view);
        deviceNameTextView = (TextView) findViewById(R.id.name_text_view);
        tempHistchart = (LineChart) findViewById(R.id.chart);
        dataSet = null;
        lineData = null;

        tempHistchart.setNoDataText("");

        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(TEMPERATURE_LOADER_ID, null, this);



        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_view);
        swipeRefreshLayout.setOnRefreshListener(this);

        //Changing status bar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.lightTurquoise));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.lightPurple));

        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }


    @Override
    public Loader<TemperatureData> onCreateLoader(int id, Bundle args) {

        Log.d(LOG_TAG, "onCreateLoader: " + args);
        // Create a new loader for the given URL
        return new DataLoader(this, null);
    }

    @Override
    public void onLoadFinished(Loader<TemperatureData> loader, TemperatureData data) {

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        swipeRefreshLayout.setRefreshing(false);

        if (data == null) {
            tempTextView.setText(R.string.loading_error);
            tempTextView.setTextSize(18);
            return;
        }

        Log.d(LOG_TAG, "onLoadFinished: " + data);
        tempTextView.setText(data.getTemperature() + "°C");
        timestampTextView.setText(data.getCurrentTime());
        deviceNameTextView.setText(data.getDeviceName());

        //plot 24h temperature history on chart
        dataSet = new LineDataSet(data.getTemperature24hEntryList(), ""); // add entries to dataset

        // create a data object with the datasets
        lineData = new LineData(dataSet);
        tempHistchart.setData(lineData);
        formatLineChart();
        tempHistchart.invalidate();

    }

    @Override
    public void onLoaderReset(Loader<TemperatureData> loader) {

        Log.d(LOG_TAG, "onLoaderReset: ");


    }


    private void formatLineChart() {

        if (tempHistchart.getData() == null
                || tempHistchart == null
                || lineData == null
                || dataSet == null)
            return;

        lineData.setValueTextColor(Color.WHITE);
        lineData.setValueTextSize(9f);
        lineData.setHighlightEnabled(true);

        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setColor(Color.WHITE);
        // dataSet.setValueTextColor(ColorTemplate.getHoloBlue());
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setFillAlpha(65);
        dataSet.setFillColor(android.R.color.darker_gray);
        //dataSet.setHighLightColor(Color.rgb(244, 217, 217));
        dataSet.setHighlightEnabled(false);
        dataSet.setDrawCircleHole(false);

        // no description text
        tempHistchart.getDescription().setEnabled(false);
        //no zooming allowed
        tempHistchart.setScaleEnabled(false);
        // add animation
        tempHistchart.animateX(800);

        // tempHistchart.setMarker(markerView(this.getApplicationContext()));
        List<ILineDataSet> sets = tempHistchart.getData().getDataSets();
        for (ILineDataSet iSet : sets) {
            LineDataSet set = (LineDataSet) iSet;
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        }

        XAxis xAxis = tempHistchart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(false);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.enableGridDashedLine(5, 5, 0);
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                return dateFormat.format(new Date((long) value));
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-15f);
        xAxis.setLabelCount(6, true);
        xAxis.setYOffset(-8f);
        xAxis.setCenterAxisLabels(false);

        float minValue = dataSet.getYMin();
        float maxValue = dataSet.getYMax();
        //Initial values for Y axis
        int yMax = (int) maxValue + 1;
        int yMin = (int) minValue - 1;
        //Increment between temperatures in Y axis
        int minAmplitude = 10;
        //Increment between temperatures in Y axis
        int step = 1;

        int amplitude = Math.abs((int) yMax - (int) yMin);
        int average = (int) ((maxValue + minValue) / 2);

        //adjusting number of temperatures to show on Y axis according to max and min temperatures
        if (amplitude > minAmplitude * 4) {
            step = 6;
        } else if (amplitude > minAmplitude * 3)

        {
            step = 5;
        } else if (amplitude > minAmplitude * 2)

        {
            step = 4;
        } else if (amplitude > minAmplitude)

        {
            step = 2;
        } else if (amplitude <= minAmplitude)

        {
            yMax = average + minAmplitude / 2;
            yMin = average - minAmplitude / 2;
            step = 1;
        }
        if (amplitude % step != 0)
            yMax += -amplitude % step + step;


        YAxis leftAxis = tempHistchart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        //  leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setAxisLineColor(Color.WHITE);
        leftAxis.setGranularity(1);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(yMin);
        leftAxis.setAxisMaximum(yMax);
        leftAxis.setXOffset(10f);
        leftAxis.setYOffset(-3f);
        leftAxis.enableGridDashedLine(5, 10, 0);
        leftAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                BigDecimal intValue = new BigDecimal(value);
                intValue = intValue.setScale(0, RoundingMode.HALF_EVEN); // here the value is correct (625.30)

                return intValue + "ºC";
            }
        });

        YAxis rightAxis = tempHistchart.getAxisRight();
        rightAxis.setEnabled(false);

        // get the legend (only possible after setting data)
        Legend l = tempHistchart.getLegend();
        l.setEnabled(false);

    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(TEMPERATURE_LOADER_ID, null, this);
    }
}
