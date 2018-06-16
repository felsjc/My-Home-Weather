package com.example.android.mytemp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import com.db.chart.view.LineChartView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Loads a list of earthquakes by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class DataLoader extends AsyncTaskLoader<TemperatureData> {


    //Tags for log messages
    private static final String TAG = "DataLoader";
    public static final boolean INPUT_VERBOSE = false;
    private static final boolean EXIT_VERBOSE = false;

    // URL to data server
    private static final String REQUEST_URL = "https://secure.sarmalink.com/devices/my";

    //Test USER
    private static String TEST_USER = "dummytestuser";
    private static String TEST_PASS = "111222333";

    //Stores login session with main page
    Connection.Response currentSession;

    //Holds temperature fetched from webportal
    TemperatureData temperatureData;

    /**
     * Constructs a new {@link DataLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public DataLoader(Context context, String url) {


        super(context);
        this.currentSession = null;
        this.temperatureData = null;
        Log.d(TAG, "DataLoader: " + (INPUT_VERBOSE ? url : ""));

        int userStringId = getContext().getResources().getIdentifier("user", "string", getContext().getApplicationContext().getPackageName());
        int passStringId = getContext().getResources().getIdentifier("pass", "string", getContext().getApplicationContext().getPackageName());
        if (userStringId != 0 && passStringId != 0) {
            TEST_USER = getContext().getResources().getString(userStringId);
            TEST_PASS = TEST_USER = getContext().getResources().getString(passStringId);
        }
    }

    @Override
    protected void onStartLoading() {

        if (this.temperatureData != null) {
            // Use cached data
            deliverResult(temperatureData);
        } else {
            //we have no data,
            //so kick off loading it
            temperatureData = new TemperatureData();

            Log.d(TAG, "ENTER onStartLoading: ");
            forceLoad();
            Log.d(TAG, "EXIT onStartLoading: ");
        }

    }


    @Override
    public TemperatureData loadInBackground() {

        Log.d(TAG, "ENTER loadInBackground: ");

        // Perform the network request, parse the response, and extract the temperature

        //TODO: Make it fetch data from page
        this.currentSession = doLogin(TEST_USER, TEST_PASS, REQUEST_URL);

        Document mainPage = QueryUtils.parse(currentSession);

        if (this.currentSession != null) {

            Elements elements = mainPage.select("[class*=view view-user-devices view-id-user_devices]");

            if (elements.size() > 0) {
                Element elem = elements.get(0);
                temperatureData.setDeviceHtmlArea(elements.get(0));
            }

            temperatureData.setInitialPage(mainPage);

            temperatureData.setCurrentTemperature(QueryUtils.extractCurrentTemperature(temperatureData.getDeviceHtmlArea()));

            temperatureData.setDeviceName(QueryUtils.extractDeviceName(temperatureData.getDeviceHtmlArea()));

            temperatureData.setCurrentTime(QueryUtils.extractTimestamp(temperatureData.getDeviceHtmlArea()));

            temperatureData.setTemperature24hLineSet(QueryUtils.getTempHistChart(temperatureData.getDeviceHtmlArea(), currentSession.cookies()));

        }

        Log.d(TAG, "EXIT loadInBackground:");
        return temperatureData;
    }

    /**
     * Provides authentication to the portal
     *
     * @param user     Portal user
     * @param password Password for account
     */
    public Connection.Response doLogin(String user, String password, String url) {

        Log.d(TAG, "ENTER doLogin: " + (INPUT_VERBOSE ? url : ""));
        Connection.Response loginResponse = null;

        try {

            //Retrieving the login from main page
            Document loginFormDoc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                    .method(Connection.Method.GET)
                    .ignoreHttpErrors(true)
                    //.timeout(5000)
                    .get();

            //Extract login form
            Elements foundForms = loginFormDoc.select("[id*=user-login]");
            QueryUtils.checkElements("[id=user-login-form]", foundForms);

            //fill in with user and password and then connect
            Connection conn = foundForms.forms().get(0).submit();
            conn.data("name").value(user);
            conn.data("pass").value(password);
            loginResponse = conn.execute();

        } catch (Exception e) {
            Log.e(TAG, "doLogin: ", e);
        }
        Log.d(TAG, "EXIT doLogin: SUCCESS");
        return loginResponse;
    }

    @Override
    public void deliverResult(TemperatureData data) {

        // We’ll save the data for later retrieval
        this.temperatureData = data;
        super.deliverResult(data);
    }
}
