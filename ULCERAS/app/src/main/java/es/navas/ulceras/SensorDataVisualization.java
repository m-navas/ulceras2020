package es.navas.ulceras;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import static es.navas.ulceras.MainActivity.chartBData;
import static es.navas.ulceras.MainActivity.chartCData;
import static es.navas.ulceras.MainActivity.chartDData;
import static es.navas.ulceras.MainActivity.indexB;
import static es.navas.ulceras.MainActivity.indexC;
import static es.navas.ulceras.MainActivity.indexD;

public class SensorDataVisualization extends AppCompatActivity {

   // private Button btn_close;
    private static LineChart chartB, chartC, chartD;

    //private static int indexB, indexC, indexD;
    //
    //    //private static SensorData  chartBData, chartCData, chartDData;

    //Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data_visualization);

        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        chartB = (LineChart)findViewById(R.id.chartB);
        chartC = (LineChart)findViewById(R.id.chartC);
        chartD = (LineChart)findViewById(R.id.chartA);

       // btn_close = (Button) findViewById(R.id.btn_sd_close);


        chartB.setDragEnabled(true);
        chartC.setDragEnabled(true);
        chartD.setDragEnabled(true);


        chartB.setScaleEnabled(true);
        chartC.setScaleEnabled(true);
        chartD.setScaleEnabled(true);

      /*  btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

       */



        /*
        chartBData = new SensorData();
        chartCData = new SensorData();
        chartDData = new SensorData();

         */

/*
        indexB = 0;
        indexC = 0;
        indexD = 0;

 */

        // test -----

        //Vdata[] testdata = new Vdata[]{new Vdata(1, 2, 3, 0), new Vdata(3, 2, 0, 0), new Vdata(3, 0, 3, 0)};


        //LineChart mpLineChart;

        //mpLineChart = (LineChart) findViewById(R.id.chartA);


        /*LineDataSet lineDataSet1 = new LineDataSet(getDataValues(testdata), "DataSet 1");

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);

        LineData data = new LineData(dataSets);

        mpLineChart.setData(data);
        mpLineChart.invalidate();

         */

        //addDataA(testdata);
        //refreshChartA();

        //ejecutarTarea();

        if(chartDData.getSize() > 0)
            setDataChartD();

        if(chartBData.getSize() > 0)
            setDataChartB();

        if(chartCData.getSize() > 0)
            setDataChartC();
    }

    /*
    private void refreshChartA(){
        Utils.log("Actualizando grafico A");
        Utils.log("Num datos: "+chartAdata.xData.size());

        LineDataSet lineDataSetX = new LineDataSet(chartAdata.getXEntries(), "X");
        lineDataSetX.setColor(Color.RED);
        LineDataSet lineDataSetY = new LineDataSet(chartAdata.getYEntries(), "Y");
        lineDataSetY.setColor(Color.GREEN);
        LineDataSet lineDataSetZ = new LineDataSet(chartAdata.getZEntries(), "Z");
        lineDataSetZ.setColor(Color.BLUE);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSetX);
        dataSets.add(lineDataSetY);
        dataSets.add(lineDataSetZ);

        LineData data = new LineData(dataSets);

        mpLineChart.setData(data);
        if(indexA > 10)
            mpLineChart.moveViewToX(indexA - 10);
        else
            mpLineChart.invalidate();

        Utils.log("Grafico A actualizado");
    }

    public static void addDataA(Vdata[] dat){
        for(Vdata d: dat){
            chartAdata.add(indexA, (float)d.getX(), (float)d.getY(), (float)d.getZ());
            indexA++;
        }
    }


    private ArrayList<Entry> dataValues1(){
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        dataVals.add(new Entry(0,20));
        dataVals.add(new Entry(1,24));
        dataVals.add(new Entry(2,2));
        dataVals.add(new Entry(3,10));
        return dataVals;
    }

    private static ArrayList<Entry> getDataValues(Vdata[] datos){
        Utils.log("getDataValues");
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        Utils.log("num datos: "+datos.length);
        for(int i = 0; i < datos.length; i++){
            Utils.log("add data");
            Float x = (float)datos[i].getX();
            dataVals.add(new Entry((float)indexA, x));
            indexA++;
        }
        Utils.log("indexA"+indexA);
        return dataVals;
    }

    public static void addDataChartA(Vdata[] datos){
        Utils.log("entra");
        LineDataSet lineDataSet1 = new LineDataSet(getDataValues(datos), "DataSet 1");

        Utils.log("entra2");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);

        Utils.log("entra3");
        LineData data = new LineData(dataSets);
        chartA.setData(data);
        chartA.invalidate();
        Utils.log("fin");
    }

     */


    /*public static void setDataChartA(Vdata[] datos){
        Utils.log("A size: "+datos.length);
        addData(datos);


        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(AxValues, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(AyValues, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(AzValues, "Z");
        setZ.setColor(Color.GREEN);

        LineData data = new LineData(setX, setY, setZ);

        chartA.setData(data);

        chartA.setVisibleXRangeMaximum(5);
        if(indexA > 10)
            chartA.moveViewToX(indexA - 10);
        else
            chartA.invalidate();

        if(indexA >= Integer.MAX_VALUE-50){
            indexA = 0;
            chartAData.clear();
            chartA.clear();
        }
    }

    /*private static void addData(Vdata[] datos) {
        for(Vdata d: datos){
            AxValues.add(new Entry(indexA, (float)d.getX()));
            AyValues.add(new Entry(indexA, (float)d.getY()));
            AzValues.add(new Entry(indexA, (float)d.getZ()));
            indexA++;
        }
    }

     */



    public static void setDataChartB(){

        Utils.log("ENTRA");
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
        if(indexB > 10)
            chartB.moveViewToX(indexB);
        else
            chartB.invalidate();

        if(indexB == Integer.MAX_VALUE){
            indexB = 0;
            chartBData.clear();
            chartB.clear();
        }

        Utils.log("FIN");
    }

    public static void setDataChartC(){
        Utils.log("ENTRA");
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
        if(indexC > 10)
            chartC.moveViewToX(indexC);
        else
            chartC.invalidate();

        if(indexC == Integer.MAX_VALUE){
            indexC = 0;
            chartCData.clear();
            chartC.clear();
        }
        Utils.log("FIN");
    }

    public static void setDataChartD(){
        Utils.log("ENTRA");
        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartDData.xData, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartDData.yData, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartDData.zData, "Z");
        setZ.setColor(Color.GREEN);

        //Utils.log("set linedataset");

        LineData data = new LineData(setX, setY, setZ);

        chartD.setData(data);

        //Utils.log("setData");

        chartD.setVisibleXRangeMaximum(5);
        //Utils.log("2");
        if(indexD > 10) {

            chartD.moveViewToX(indexD);
            //Utils.log("moveViewToX");
        }else {
            chartD.invalidate();
            //Utils.log("invalidate");
        }

        if(indexD == Integer.MAX_VALUE){
            //Utils.log("nodebe");
            indexD = 0;
            chartDData.clear();
            chartD.clear();
        }
        Utils.log("FIN");
    }


    /*
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

        public void add(int index, float x, float y, float z){
            this.xData.add(new Entry(index, x));
            this.yData.add(new Entry(index, y));
            this.zData.add(new Entry(index, z));
        }

        public ArrayList<Entry> getXEntries (){
            return xData;
        }

        public ArrayList<Entry> getYEntries (){
            return yData;
        }

        public ArrayList<Entry> getZEntries (){
            return zData;
        }
    }

    /*
    private void ejecutarTarea() {
        handler.postDelayed(new Runnable() {
            public void run() {

                // función a ejecutar
                refreshChartA();

                handler.postDelayed(this, 2500);
            }

        }, 2500);

    }

     */

}
