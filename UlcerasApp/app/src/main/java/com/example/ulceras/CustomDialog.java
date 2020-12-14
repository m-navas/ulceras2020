package com.example.ulceras;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button btn_exit, btn_change_sensor_type;
    private  static ArrayList<Entry> xValues = new ArrayList<>();
    private  static ArrayList<Entry> yValues = new ArrayList<>();
    private  static ArrayList<Entry> zValues = new ArrayList<>();
    private  static int timestamp = 0;
    private  static LineChart mChart, mChart2;
    private TextView tv_deviceA, tv_deviceB;



    public CustomDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mChart = (LineChart)findViewById(R.id.chartA);
        mChart2 = (LineChart)findViewById(R.id.chartB);
        btn_exit = (Button) findViewById(R.id.btn_dialog_exit);
        btn_change_sensor_type = (Button) findViewById(R.id.btn_change_sensor_type);

        mChart.setDragEnabled(true);
        mChart2.setDragEnabled(true);

        mChart.setScaleEnabled(true);
        mChart2.setScaleEnabled(true);

        btn_exit.setOnClickListener(this);
        btn_change_sensor_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_sensor_type();

            }
        });

        tv_deviceA = (TextView)findViewById(R.id.tv_deviceA);
        tv_deviceB = (TextView)findViewById(R.id.tv_deviceB);

        setDeviceNames();

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

    @Override
    public void onClick(View v) {
        dismiss();
    }

    public void setDeviceNames(){
        for(int i = 0; i < MainActivity.connectedDevices.size(); i++){
            if(i == 0)
                tv_deviceA.setText(MainActivity.connectedDevices.get(i).getName());
            else
                tv_deviceB.setText(MainActivity.connectedDevices.get(i).getName());
        }
    }

    public static void setData_chart_A(Vdata[] datos){
        for(Vdata data: datos){
            xValues.add(new Entry(timestamp, (float)data.getX()));
            yValues.add(new Entry(timestamp, (float)data.getY()));
            zValues.add(new Entry(timestamp, (float)data.getZ()));

            timestamp++;

            if(xValues.size() > 30){
                int dif = xValues.size() - 30;
                for(int i = 0; i < dif; i++){
                    xValues.remove(0);
                    yValues.remove(0);
                    zValues.remove(0);
                }

            }
        }

        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(xValues, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(yValues, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(zValues, "Z");
        setZ.setColor(Color.GREEN);

        LineData data = new LineData(setX, setY, setZ);

        mChart.setData(data);

        mChart.setVisibleXRangeMaximum(5);
        if(timestamp > 10)
            mChart.moveViewToX(timestamp - 10);
        else
            mChart.invalidate();

        if(timestamp == Integer.MAX_VALUE){
            timestamp = 0;
            xValues.clear();
            yValues.clear();
            zValues.clear();
            mChart.clear();
        }
    }

    public static void setData_chart_B(Vdata[] datos){
        for(Vdata data: datos){
            xValues.add(new Entry(timestamp, (float)data.getX()));
            yValues.add(new Entry(timestamp, (float)data.getY()));
            zValues.add(new Entry(timestamp, (float)data.getZ()));

            timestamp++;

            if(xValues.size() > 30){
                int dif = xValues.size() - 30;
                for(int i = 0; i < dif; i++){
                    xValues.remove(0);
                    yValues.remove(0);
                    zValues.remove(0);
                }

            }
        }

        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(xValues, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(yValues, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(zValues, "Z");
        setZ.setColor(Color.GREEN);

        LineData data = new LineData(setX, setY, setZ);

        mChart2.setData(data);

        mChart2.setVisibleXRangeMaximum(5);
        if(timestamp > 10)
            mChart2.moveViewToX(timestamp - 10);
        else
            mChart2.invalidate();

        if(timestamp == Integer.MAX_VALUE){
            timestamp = 0;
            xValues.clear();
            yValues.clear();
            zValues.clear();
            mChart2.clear();
        }
    }
}






