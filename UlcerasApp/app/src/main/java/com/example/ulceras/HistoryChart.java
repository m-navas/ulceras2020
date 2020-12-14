package com.example.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class HistoryChart extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Button btn_getHistory;
    String selectedItem;
    private static Sensor_Data chartData;
    private static int index = 0;
    private static LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_chart);

        chart = (LineChart)findViewById(R.id.historyChart);
        chart.setDragEnabled(true);

        btn_getHistory = (Button)findViewById(R.id.btn_get_history);

        Spinner spinner = (Spinner) findViewById(R.id.time_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_spinner_items, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);



        btn_getHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getHistory(selectedItem);
                Utils.log("getHistory peticion");
            }
        });

        chartData = new Sensor_Data();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected.
        selectedItem = parent.getItemAtPosition(pos).toString();
        Utils.log("Spinner item: "+selectedItem);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        Toast.makeText(this, "Seleccione una opción", Toast.LENGTH_SHORT).show();
    }

    public static void drawChart(SensorData[] historyData){
        index = 0;
        chartData.clear();
        Utils.log("Drawing history chart");

        for(SensorData data: historyData){
            //Utils.log("chartData: "+data.getX()+" "+data.getY()+" "+data.getZ());
            chartData.add((float)data.getX(), (float)data.getY(), (float)data.getZ());
            index++;
        }

        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartData.xData, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartData.yData, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartData.zData, "Z");
        setZ.setColor(Color.GREEN);

        LineData data = new LineData(setX, setY, setZ);

        chart.setData(data);

        chart.setVisibleXRangeMaximum(5);
        if(index > 10)
            chart.moveViewToX(index - 10);
        else
            chart.invalidate();

        if(index == Integer.MAX_VALUE){
            index = 0;
            chartData.clear();
            chart.clear();
        }

    }

    private class Sensor_Data{
        public ArrayList<Entry> xData = new ArrayList<>();
        public ArrayList<Entry> yData = new ArrayList<>();
        public ArrayList<Entry> zData = new ArrayList<>();

        public Sensor_Data(){

        }

        public void clear(){
            xData.clear();
            yData.clear();
            zData.clear();
        }

        public void add(float x, float y, float z){
            this.xData.add(new Entry(index, x));
            this.yData.add(new Entry(index, y));
            this.zData.add(new Entry(index, z));
        }
    }
}
