package com.example.android.mytemp;

import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AndroidException;
import android.util.Log;
import android.widget.TextView;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.LineChartView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<TemperatureData> {

    private TextView tempTextView;
    private TextView timestampTextView;
    private TextView deviceNameTextView;

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

        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(TEMPERATURE_LOADER_ID, null, this);


    }

    @Override
    public Loader<TemperatureData> onCreateLoader(int id, Bundle args) {

        Log.d(LOG_TAG, "onCreateLoader: " + args);
        // Create a new loader for the given URL
        return new DataLoader(this, null);
    }

    @Override
    public void onLoadFinished(Loader<TemperatureData> loader, TemperatureData data) {

        if (data != null) {

            Log.d(LOG_TAG, "onLoadFinished: " + data);
            tempTextView.setText(data.getTemperature() + "°C");
            timestampTextView.setText(data.getCurrentTime());
            deviceNameTextView.setText(data.getDeviceName());


            //plot 24h temperature history on chart
            if (data.getTemperature24hLineSet() != null) {

                LineChartView lineChartViewTempHist = (LineChartView) findViewById(R.id.linechart);
                lineChartViewTempHist.reset();
                lineChartViewTempHist = getFormattedChart(lineChartViewTempHist, data.getTemperature24hLineSet());

                lineChartViewTempHist.show();
            }


        }

    }

    @Override
    public void onLoaderReset(Loader<TemperatureData> loader) {

        Log.d(LOG_TAG, "onLoaderReset: ");


    }

    private LineChartView getFormattedChart(LineChartView lineChartViewTempHist, LineSet dataset) {

        lineChartViewTempHist.reset();
        dataset.setSmooth(true);
        dataset.setThickness(4);
        dataset.setShadow(200,50,50,android.R.color.white);
        boolean test = dataset.hasShadow();
        //dataset.setColor();
        //dataset.setDotsColor(getResources().getColor(android.R.color.white));
        //dataset.setDotsRadius(5);


        lineChartViewTempHist.addData(dataset);

        float minValue = dataset.getMin().getValue();
        float maxValue = dataset.getMax().getValue();

        //dummy test values
        //minValue = 23;
        //maxValue = 23;

        //Initial values for Y axis
        int yMax = (int) maxValue + 1;
        int yMin = (int) minValue - 1;
        //Increment between temperatures in Y axis
        int minAmplitude = 10;
        //Increment between temperatures in Y axis
        int step = 1;
        //Number of horizontal lines in the grid
        int hGridLines = 5;

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
        amplitude = Math.abs(yMax - yMin);
        hGridLines = (int) amplitude / step;

        lineChartViewTempHist.setAxisBorderValues(yMin, yMax, step);
        lineChartViewTempHist.setAxisLabelsSpacing(20);


        //Grid
        Paint gridPaint = new Paint(R.color.colorAccent);
        gridPaint.setStyle(Paint.Style.FILL);
        gridPaint.setPathEffect(new

                DashPathEffect(new float[]{
                10, 10
        }, 0));
        lineChartViewTempHist.setGrid(hGridLines, 4, gridPaint);

        lineChartViewTempHist.setYLabels(AxisRenderer.LabelPosition.OUTSIDE);
        lineChartViewTempHist.setXLabels(AxisRenderer.LabelPosition.OUTSIDE);

        //                lineChartViewTempHist.setXLabels(AxisRenderer.LabelPosition.NONE);
        DecimalFormat format = new DecimalFormat("##.#");
        return lineChartViewTempHist;
    }
}
