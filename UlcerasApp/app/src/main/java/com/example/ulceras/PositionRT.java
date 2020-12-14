package com.example.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PositionRT extends AppCompatActivity {
    private static HorizontalBarChart stackedChart;
    static ArrayList<BarEntry> dataValues = new ArrayList<>();
    static String [] labels = {"izq", "der", "supino", "incorporado"};
    static int colorcodes[] = new int[]{Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW};
    static ArrayList<Integer> positionsData = new ArrayList<>();
    private static final Integer MAX_SECONDS = 30;
    private static final Float MAX_SECONDS_F = 30.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_rt);

        stackedChart = findViewById(R.id.stacked_chart);

        XAxis xAxis = stackedChart.getXAxis();
        xAxis.setEnabled(false);

        YAxis yl = stackedChart.getAxisLeft();
        yl.setEnabled(true);
        yl.setValueFormatter(new MyYAxisFormatter());

        YAxis yr = stackedChart.getAxisRight();
        yr.setEnabled(false);

        yr.setValueFormatter(new MyYAxisFormatter());

    }

    public static void addData(int positionIndex){
        positionsData.add(positionIndex);

        if(positionsData.size() > MAX_SECONDS){
            positionsData.remove(0);
        }

        if (stackedChart != null)
            drawChart();
    }

    private static void drawChart(){


        float[] steps = new float[MAX_SECONDS];
        for(int i = 0; i < MAX_SECONDS; i++){
            steps[i] = 10; // ancho de la barra, no tiene utilidad en este caso ya que cada "step" lo consideramos 1 segundo
        }

        Utils.log("N elementos steps: "+steps.length);

        dataValues.add(new BarEntry(0f, steps));

        BarDataSet barDataSet = new BarDataSet(dataValues, "Datos");

        int[] colorClassArray = new int[MAX_SECONDS];

        int n = 0;
        if(positionsData.size() < MAX_SECONDS){
            n = MAX_SECONDS - positionsData.size();

            for(int i = 0; i < n; i++){
                colorClassArray[i] = Color.GRAY; // no hay datos
            }
        }

        for(int i = 0; i < positionsData.size(); i++){
            colorClassArray[n] = colorcodes[positionsData.get(i)];
            n++;
        }

        Utils.log("Colors array size: "+colorClassArray.length);

        for(int i: colorClassArray)
            Utils.log(Integer.toString(i));

        barDataSet.setColors(colorClassArray);

        // Set custom legend
        Legend legend = stackedChart.getLegend();
        LegendEntry[] legendEntries = new LegendEntry[4];

        for(int i = 0; i < colorcodes.length; i++){
            legendEntries[i] = new LegendEntry();
            legendEntries[i].label = labels[i];
            legendEntries[i].formColor = colorcodes[i];
        }

        legend.setCustom(legendEntries);


        BarData barData = new BarData();
        barData.addDataSet(barDataSet);


        stackedChart.setData(barData);
        stackedChart.setExtraOffsets(0f, 30.0f, 0f, 30.0f);
        stackedChart.invalidate();
    }

    private static ArrayList<String> getYAxisValues() {
        ArrayList<String> labels = new ArrayList<>();

        for(int i = 0; i < MAX_SECONDS; i++){
            labels.add("-"+ (MAX_SECONDS-i));
        }

        return labels;
    }


    // Y Axis labels formatter
    private static class MyYAxisFormatter extends ValueFormatter{
        public MyYAxisFormatter(){

        }

        @Override
        public String getFormattedValue(float value) {
            if(value == 0.0f)
                return "- 30 s";
            else if (value == 100.0f)
                return "- 20 s";
            else if (value == 200.0f)
                return "- 10 s";
            else if (value == 300.0f)
                return "Actual";
            else
                return "";
        }
    }

    // Ejemplo con timestamp
    public class FooFormatter extends ValueFormatter {
        private long referenceTimestamp; // minimum timestamp in your data set
        private DateFormat mDataFormat;
        private Date mDate;

        public FooFormatter(long referenceTimestamp) {
            this.referenceTimestamp = referenceTimestamp;
            this.mDataFormat = new SimpleDateFormat("yyyy-MM-dd");
            this.mDate = new Date();
        }

        @Override
        public String getFormattedValue(float value) {
            // convertedTimestamp = originalTimestamp - referenceTimestamp
            long convertedTimestamp = (long) value;

            // Retrieve original timestamp
            long originalTimestamp = referenceTimestamp + convertedTimestamp;

            // Convert timestamp to hour:minute
            return getDateString(originalTimestamp);
        }

        private String getDateString(long timestamp) {
            try {
                mDate.setTime(timestamp);
                return mDataFormat.format(mDate);
            } catch(Exception ex) {
                return "xx";
            }
        }
    }


}
