package com.example.android.mytemp;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.github.mikephil.charting.data.Entry;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

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
    private List<Entry> temperature24hEntryList;

    //Linechart with temperature history
    private LineChartView lineChartViewTempHistory;


    public TemperatureData() {

        currentTemperature = "";

        temperature24hEntryList = null;

        deviceHtmlArea = null;

        lineChartViewTempHistory = null;
    }


    public String getTemperature() {
        return currentTemperature;
    }

    public void setTemperature(String _temperature) {
        currentTemperature = _temperature;
    }

    public List<Entry> getTemperature24hEntryList() {
        return temperature24hEntryList;
    }

    public void setTemperature24hEntryList(List<Entry> temperature24hEntryList) {
        this.temperature24hEntryList = temperature24hEntryList;
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
