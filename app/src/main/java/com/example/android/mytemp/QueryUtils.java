package com.example.android.mytemp;

import android.util.Log;

import com.db.chart.model.LineSet;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

import static android.content.ContentValues.TAG;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    /**
     * Tag for the log messages
     */
    public static final String TAG = QueryUtils.class.getSimpleName();

    public static final boolean INPUT_VERBOSE = false;
    private static final boolean EXIT_VERBOSE = false;

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
        Log.d(TAG, "QueryUtils: CONSTRUCTOR");
    }


    /**
     * Extracts temperature from a given parsed page
     *
     * @param element a parsed page containing all the data from which temperature will be extracted
     * @return formatted temperature
     */
    public static String extractCurrentTemperature(Element element) {

        Log.d(TAG, "ENTER extractCurrentTemperature: " + (INPUT_VERBOSE ? element : ""));
        String temperature = "";

        if (element == null)
            return temperature;

        /**Temperature is given by <span class="termo">+20.88</span> on the page
         * So scan for it and extract its value
         */
        try {
            Elements tempElements = element.select("[class=termo]");
            checkElements("[class=termo]", tempElements);

            Element tempElement = tempElements.get(0);

            if (tempElement != null)
                temperature = tempElement.text();
        } catch (Exception e) {
            Log.e(TAG, "extractCurrentTemperature: ", e);
        }

        Log.d(TAG, "EXIT extractCurrentTemperature: " + (EXIT_VERBOSE ? temperature : ""));
        return temperature;
    }

    public static String extractDeviceName(Element element) {
        Log.d(TAG, "ENTER extractDeviceName: " + (INPUT_VERBOSE ? element : ""));
        String deviceName = "";

        if (element == null)
            return deviceName;

        try {
            Elements tempElements = element.select("[class=device-title]");
            checkElements("[class=device-title]", tempElements);

            Element tempElement = tempElements.get(0);

            if (tempElement != null)
                deviceName = tempElement.text();
        } catch (Exception e) {
            Log.e(TAG, "extractDeviceName: ", e);
        }

        Log.d(TAG, "EXIT extractDeviceName: " + (EXIT_VERBOSE ? deviceName : ""));
        return deviceName;
    }


    public static String extractTimestamp(Element element) {

        Log.d(TAG, "ENTER extractTimestamp: " + (INPUT_VERBOSE ? element : ""));
        String timestamp = "";

        if (element == null)
            return timestamp;

        try {
            Elements tempElements = element.select("[class=timestamp]");
            checkElements("[class=timestamp]", tempElements);

            Element tempElement = tempElements.get(0);

            if (tempElement != null)
                timestamp = tempElement.text();
        } catch (Exception e) {
            Log.e(TAG, "extractTimestamp: ", e);
        }

        Log.d(TAG, "EXIT extractTimestamp: " + (EXIT_VERBOSE ? timestamp : ""));
        return timestamp;
    }

    /**
     * Download csv file containing temperature history of the day
     *
     * @param element
     */
    public static String extractTempHist(Element element, Map<String, String> cookies) {

        Log.d(TAG, "ENTER extractTempHist: " + (INPUT_VERBOSE ? element : ""));

        //Link to download the csv file
        String tempArchiveLink = getArchiveLink(element);
        //List of temperatures will be stored here
        String temperatures = "";

        if (element == null || tempArchiveLink == "")
            return "";

        try {
            Connection.Response connResponse;
            Document archivePage = null;

            //Load page with form to download temperature history
            archivePage = Jsoup.connect(tempArchiveLink)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .get();

            //Select download history form
            Elements foundForms = archivePage.select("[id=uzraugi-termo-operations-form]");
            checkElements("[uzraugi-termo-operations-form]", foundForms);
            FormElement auxFormElement = foundForms.forms().get(0);

            //Removing op="delete data" from the form, otherwise the response will be a page confirming data history exclusion instead csv file with temp data
            auxFormElement.elements().remove(2);

            //add cookie from existing session and submit form to download csv with past temperatures
            Connection conn = auxFormElement.submit();
            conn.cookies(cookies);
            temperatures = conn.execute().body();

            //Reformat list to remove quote marks and first line with column descriptions
            temperatures = temperatures.replace("\"", "");
            temperatures = temperatures.replace("t,val\r\n", "");

        } catch (
                Exception e) {
            Log.e(TAG, "extractTempHist: ", e);
            return "";
        }

        Log.d(TAG, "EXIT extractTempHist: " + (EXIT_VERBOSE ? temperatures : ""));
        return temperatures;
    }

    private static String getArchiveLink(Element element) {

        Log.d(TAG, "ENTER getArchiveLink: " + (INPUT_VERBOSE ? element : ""));

        //final String DEVICE_ID_QUERY = "flag-wrapper flag-my-favourite-devices flag-my-favourite-devices-";
        final String DEVICE_ID_QUERY = "flag-wrapper flag-my-favourite-devices flag-my-favourite-devices-";
        final String DEVICE_LIST_QUERY = "[id=content-area]";
        final String TIME_STAMP_QUERY = "[class=timestamp]";
        String linkTemplate = "https://secure.sarmalink.com/node/DEVICE_ID/archive/START_DATE_TIME/END_DATE_TIME";
        String archiveLink = "";

        if (element == null)
            return "";

        try {

            //Select elements that contain id of the device (the number in front of "flag-device-alarms-")
            Elements deviceElements = element.select("[class*=" + DEVICE_ID_QUERY + "]");
            checkElements("[class*=" + DEVICE_ID_QUERY + "]", deviceElements);

            //Find device id on string
            String deviceId = deviceElements.get(0).toString();
            int pos = deviceId.lastIndexOf(DEVICE_ID_QUERY);
            pos += DEVICE_ID_QUERY.length();
            deviceId = deviceId.substring(pos, deviceId.indexOf("\">"));

            linkTemplate = linkTemplate.replace("DEVICE_ID", deviceId);

            //Find current timestamp from device
            deviceElements = element.select(TIME_STAMP_QUERY);
            checkElements(TIME_STAMP_QUERY, deviceElements);
            String timestamp = deviceElements.get(0).text();

            linkTemplate = linkTemplate.replace("END_DATE_TIME", timestamp);

            //Subtract one day from current date and use as start date
            //timestamp = timestamp.substring(0,10);
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startDate = formatter.parse(timestamp);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.DATE, -1);
            timestamp = formatter.format(cal.getTime());
            linkTemplate = linkTemplate.replace("START_DATE_TIME", timestamp);
            archiveLink = linkTemplate;

        } catch (Exception e) {
            Log.e(TAG, "getArchiveLink: ", e);
        }

        Log.d(TAG, "EXIT getArchiveLink: " + (EXIT_VERBOSE ? linkTemplate : ""));
        return archiveLink;
    }

    /**
     * Parses a given document to get HTML
     *
     * @param response is the response from a connection established previously
     * @return parsed HTML
     */
    public static Document parse(Connection.Response response) {

        Log.d(TAG, "ENTER parse: " + (INPUT_VERBOSE ? response : ""));

        try {
            if (response != null)
                return response.parse();
            Log.d(TAG, "EXIT parse: SUCCESSFUL");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "EXIT parse: NOTHING TO PARSE");
        return null;
    }


    /**
     * Checks if an element with a given name exists
     *
     * @param name
     * @param elem
     */
    public static void checkElement(String name, Element elem) {
        Log.d(TAG, "ENTER checkElement: " + (INPUT_VERBOSE ? elem : ""));
        if (elem == null) {
            throw new RuntimeException("Unable to find " + name);
        }
        Log.d(TAG, "EXIT checkElements: PASSED");
    }

    /**
     * Checks if an element with a given name exists
     *
     * @param name
     * @param elements
     */
    public static void checkElements(String name, Elements elements) {
        Log.d(TAG, "ENTER checkElements: " + (INPUT_VERBOSE ? elements : ""));
        if (elements.size() == 0) {
            throw new RuntimeException("Unable to find Elements with " + name);
        }

        Log.d(TAG, "EXIT checkElements: PASSED");
    }

    public static LineSet getTempHistChart(Element element, Map<String, String> cookies) {

        Log.d(TAG, "getTempHistChart: " + (INPUT_VERBOSE ? element : ""));
        LineSet tempHistChart = new LineSet();
        String tempHistory = extractTempHist(element, cookies);

        if (tempHistory == "")
            return null;

        String[] result = tempHistory.split("\r\n");

        //reduce the number of samples in chart to a fraction of 1/filterFactor
        int filterFactor = 50;
        //samples must be read from the end to show chart in correct order
        for (int i = result.length - 1; i >= 0; i -= filterFactor) {

            String currentSample = result[i];
            String[] parts = currentSample.split(",");
            float value = Float.parseFloat(parts[1]);
            String timeLabel = "";
            //only add labels to first and middle samples, all the rest is left blank (otherwise they get clumsy / overlapped)
            if (i == result.length - 1 || (i > result.length / 2 - filterFactor / 2 && i < result.length / 2 + filterFactor / 2)) {
                timeLabel = parts[0];

                DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                DateFormat outputDateFormat = new SimpleDateFormat("d/MMM");
                DateFormat outputTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                try {
                    timeLabel = outputDateFormat.format(inputDateFormat.parse(timeLabel))
                            + " - " + outputTimeFormat.format(inputDateFormat.parse(timeLabel));

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            tempHistChart.addPoint(timeLabel, value);
        }

        //Last sample is cut out by filter in previous loop, so add it manually
        if (result.length > 0) {
            String currentSample = result[0];
            String[] parts = currentSample.split(",");
            float value = Float.parseFloat(parts[1]);
            String timeLabel = "";
            timeLabel = parts[0];
            DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat outputDateFormat = new SimpleDateFormat("d/MMM");
            DateFormat outputTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
            try {
                timeLabel = outputDateFormat.format(inputDateFormat.parse(timeLabel))
                        + " - " + outputTimeFormat.format(inputDateFormat.parse(timeLabel));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            tempHistChart.addPoint(timeLabel, value);
        }

        Log.d(TAG, "EXIT checkElements: " + (EXIT_VERBOSE ? tempHistChart.size() : ""));
        return tempHistChart;

    }

    private static int mod(int x, int y) {
        int result = x % y;
        if (result < 0)
            result += y;
        return result;
    }
}