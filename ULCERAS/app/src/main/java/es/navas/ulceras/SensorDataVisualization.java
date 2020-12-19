package es.navas.ulceras;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import static es.navas.ulceras.MainActivity.chartBData;
import static es.navas.ulceras.MainActivity.chartCData;
import static es.navas.ulceras.MainActivity.chartAData;
import static es.navas.ulceras.MainActivity.indexB;
import static es.navas.ulceras.MainActivity.indexC;
import static es.navas.ulceras.MainActivity.indexA;

public class SensorDataVisualization extends AppCompatActivity {

    private static LineChart chartB, chartC, chartA;

    static boolean draw; // Variable que actua a modo de semaforo para que no existan conflictos en la librería MPAndroidChart a la hora de crear los gráficos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data_visualization);


        chartB = (LineChart)findViewById(R.id.chartB);
        chartC = (LineChart)findViewById(R.id.chartC);
        chartA = (LineChart)findViewById(R.id.chartA);


        chartB.setDragEnabled(true);
        chartC.setDragEnabled(true);
        chartA.setDragEnabled(true);


        chartB.setScaleEnabled(true);
        chartC.setScaleEnabled(true);
        chartA.setScaleEnabled(true);

        draw = true;


        if(chartAData.getSize() > 0)
            setDataChartA();

        if(chartBData.getSize() > 0)
            setDataChartB();

        if(chartCData.getSize() > 0)
            setDataChartC();


    }


    public static void setDataChartB(){
        draw = false; // impide que se intente dibujar otro gráfico hasta que se complete este

        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartBData.getXEntries(), "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartBData.getYEntries(), "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartBData.getZEntries(), "Z");
        setZ.setColor(Color.GREEN);

        LineData data = new LineData(setX, setY, setZ);

        chartB.setData(data);

        chartB.setVisibleXRangeMaximum(5);
        if(indexB > 10)
            chartB.moveViewToX(indexB);
        else
            chartB.invalidate();

        if(indexB == Integer.MAX_VALUE){
            indexB = 0;
            chartBData.clear();
            chartB.clear();
        }

        draw = true;
    }

    public static void setDataChartC(){

        draw = false; // impide que se intente dibujar otro gráfico hasta que se complete este

        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartCData.getXEntries(), "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartCData.getYEntries(), "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartCData.getZEntries(), "Z");
        setZ.setColor(Color.GREEN);

        LineData data = new LineData(setX, setY, setZ);

        chartC.setData(data);

        chartC.setVisibleXRangeMaximum(5);
        if(indexC > 10)
            chartC.moveViewToX(indexC);
        else
            chartC.invalidate();

        if(indexC == Integer.MAX_VALUE){
            indexC = 0;
            chartCData.clear();
            chartC.clear();
        }

        draw = true;

    }

    public static void setDataChartA(){

        draw = false; // impide que se intente dibujar otro gráfico hasta que se complete este

        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartAData.getXEntries(), "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartAData.getYEntries(), "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartAData.getZEntries(), "Z");
        setZ.setColor(Color.GREEN);

        LineData data = new LineData(setX, setY, setZ);

        chartA.setData(data);

        chartA.setVisibleXRangeMaximum(5);

        if(indexA > 10) {
            chartA.moveViewToX(indexA); // mueve la vista al último dato
        }else {
            chartA.invalidate();
        }

        if(indexA == Integer.MAX_VALUE){
            indexA = 0;
            chartAData.clear();
            chartA.clear();
        }

        draw = true;

    }

}
