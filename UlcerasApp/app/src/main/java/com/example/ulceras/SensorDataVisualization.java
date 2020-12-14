package com.example.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class SensorDataVisualization extends AppCompatActivity {

    private Button btn_close, btn_change_sensor_type;
    private static LineChart chartA, chartB, chartC;
    private ArrayList<Entry> xValues = new ArrayList<>();
    private ArrayList<Entry> yValues = new ArrayList<>();
    private ArrayList<Entry> zValues = new ArrayList<>();
    private static int timestamp = 0;

    private static SensorData chartAData, chartBData, chartCData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensordatavisualization);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        chartA = (LineChart)findViewById(R.id.chartA);
        chartB = (LineChart)findViewById(R.id.chartB);
        chartC = (LineChart)findViewById(R.id.chartC);
        btn_close = (Button) findViewById(R.id.btn_sd_close);
        btn_change_sensor_type = (Button) findViewById(R.id.btn_sd_change_sensor_type);

        chartA.setDragEnabled(true);
        chartB.setDragEnabled(true);
        chartC.setDragEnabled(true);

        chartA.setScaleEnabled(true);
        chartB.setScaleEnabled(true);
        chartC.setScaleEnabled(true);

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_change_sensor_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_sensor_type();

            }
        });

        chartAData = new SensorData();
        chartBData = new SensorData();
        chartCData = new SensorData();

    }

    private void change_sensor_type() {
        if(MainActivity.sensorType.equals("acc")) {
            MainActivity.sensorType = "gyr";
            btn_change_sensor_type.setText("Mostrar ACC");
            Toast.makeText(MainActivity.c, "Mostrando datos GYR", Toast.LENGTH_LONG).show();
        }else {
            MainActivity.sensorType = "acc";
            btn_change_sensor_type.setText("Mostrar GYR");
            Toast.makeText(MainActivity.c, "Mostrando datos ACC", Toast.LENGTH_LONG).show();
        }
    }

    public static void setDataChartA(Vdata[] datos){
        for(Vdata data: datos){
           /* chartAData.xData.add(new Entry(timestamp, (float)data.getX()));
            chartAData.yData.add(new Entry(timestamp, (float)data.getY()));
            chartAData.zData.add(new Entry(timestamp, (float)data.getZ()));*/

            chartAData.add((float)data.getX(), (float)data.getY(), (float)data.getZ());

            timestamp++;

            if(chartAData.xData.size() > 30){
                int dif = chartAData.xData.size() - 30;
                for(int i = 0; i < dif; i++){
                    chartAData.xData.remove(0);
                    chartAData.yData.remove(0);
                    chartAData.zData.remove(0);
                }

            }
        }

        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartAData.xData, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartAData.yData, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartAData.zData, "Z");
        setZ.setColor(Color.GREEN);

        LineData data = new LineData(setX, setY, setZ);

        chartA.setData(data);

        chartA.setVisibleXRangeMaximum(5);
        if(timestamp > 10)
            chartA.moveViewToX(timestamp - 10);
        else
            chartA.invalidate();

        if(timestamp == Integer.MAX_VALUE){
            timestamp = 0;
            chartAData.clear();
            chartA.clear();
        }
    }

    public static void setDataChartB(Vdata[] datos){
        for(Vdata data: datos){
            chartBData.add((float)data.getX(), (float)data.getY(), (float)data.getZ());

            timestamp++;

            if(chartBData.xData.size() > 30){
                int dif = chartBData.xData.size() - 30;
                for(int i = 0; i < dif; i++){
                    chartBData.xData.remove(0);
                    chartBData.yData.remove(0);
                    chartBData.zData.remove(0);
                }

            }
        }

        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartBData.xData, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartBData.yData, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartBData.zData, "Z");
        setZ.setColor(Color.GREEN);

        LineData data = new LineData(setX, setY, setZ);

        chartB.setData(data);

        chartB.setVisibleXRangeMaximum(5);
        if(timestamp > 10)
            chartB.moveViewToX(timestamp - 10);
        else
            chartB.invalidate();

        if(timestamp == Integer.MAX_VALUE){
            timestamp = 0;
            chartBData.clear();
            chartB.clear();
        }
    }

    public static void setDataChartC(Vdata[] datos){
        for(Vdata data: datos){
            chartCData.add((float)data.getX(), (float)data.getY(), (float)data.getZ());

            timestamp++;

            if(chartCData.xData.size() > 30){
                int dif = chartCData.xData.size() - 30;
                for(int i = 0; i < dif; i++){
                    chartCData.xData.remove(0);
                    chartCData.yData.remove(0);
                    chartCData.zData.remove(0);
                }

            }
        }

        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartCData.xData, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartCData.yData, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartCData.zData, "Z");
        setZ.setColor(Color.GREEN);

        LineData data = new LineData(setX, setY, setZ);

        chartC.setData(data);

        chartC.setVisibleXRangeMaximum(5);
        if(timestamp > 10)
            chartC.moveViewToX(timestamp - 10);
        else
            chartC.invalidate();

        if(timestamp == Integer.MAX_VALUE){
            timestamp = 0;
            chartCData.clear();
            chartC.clear();
        }
    }


    private class SensorData{
        public ArrayList<Entry> xData = new ArrayList<>();
        public ArrayList<Entry> yData = new ArrayList<>();
        public ArrayList<Entry> zData = new ArrayList<>();

        public SensorData(){

        }

        public void clear(){
            xData.clear();
            yData.clear();
            zData.clear();
        }

        public void add(float x, float y, float z){
            this.xData.add(new Entry(timestamp, x));
            this.yData.add(new Entry(timestamp, y));
            this.zData.add(new Entry(timestamp, z));
        }
    }

}
