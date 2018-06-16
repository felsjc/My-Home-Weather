package com.example.android.mytemp;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TemperatureData {

    //current temperature
    private String currentTemperature;

    //parsed page after login
    private Document initialPage;

    //name of the device
    private String deviceName;

    //HTML extract with my selected device data
    private Element deviceHtmlArea;

    //current date and time
    private String currentTime;

    //Last 24h temperature history
    private LineSet temperature24hLineSet;

    //Linechart with temperature history
    private LineChartView lineChartViewTempHistory;


    public TemperatureData() {

        currentTemperature = "";

        temperature24hLineSet = null;

        deviceHtmlArea = null;

        lineChartViewTempHistory = null;
    }


    public String getTemperature() {
        return currentTemperature;
    }

    public void setTemperature(String _temperature) {
        currentTemperature = _temperature;
    }

    public LineSet getTemperature24hLineSet() {
        return temperature24hLineSet;
    }

    public void setTemperature24hLineSet(LineSet temperature24hLineSet) {
        this.temperature24hLineSet = temperature24hLineSet;
    }

    public String getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(String currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public Document getInitialPage() {
        return initialPage;
    }

    public void setInitialPage(Document initialPage) {
        this.initialPage = initialPage;
    }

    public Element getDeviceHtmlArea() {
        return deviceHtmlArea;
    }

    public void setDeviceHtmlArea(Element deviceHtmlArea) {
        this.deviceHtmlArea = deviceHtmlArea;
    }

    public LineChartView getLineChartViewTempHistory() {
        return lineChartViewTempHistory;
    }

    public void setLineChartViewTempHistory(LineChartView lineChartViewTempHistory) {
        this.lineChartViewTempHistory = lineChartViewTempHistory;
    }
}
