package com.example.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import static com.example.ulceras.MainActivity.MAX_STEPS;

public class ChartPosturas extends AppCompatActivity {

    private static HorizontalBarChart chartPosturas;
    private static ImageView iv_lastAct;

    private static int NUM_POSTURAS = 4; // numero de posturas distintas que se tienen en cuenta, vendra determinado según el caso de uso
    private static ArrayList<Actividad> registro = new ArrayList<>(); // CAMBIAR POR REGISTRO REAL
    private static ArrayList<String> labels = new ArrayList<String>();
    private static int colorcodes[] = new int[]{Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW}; //colores para cada postura asociados al id
    private static float[] steps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_posturas);

        //SIMULACION DATOS
        registro.add(new Actividad(1, 123l));
        registro.add(new Actividad(2, 124l));
        registro.add(new Actividad(1, 125l));

        labels.add("sup"); //id 1
        labels.add("inc"); //id 2 ...
        labels.add("der");
        labels.add("izq");

        chartPosturas = findViewById(R.id.chartPosturas);
        iv_lastAct = findViewById(R.id.iv_lastAct);

        steps = new float[MAX_STEPS];
        for(int i = 0; i < MAX_STEPS; i++){
            steps[i] = 10; // largo de la barra, no tiene utilidad en este caso ya que cada "step" lo consideramos una unidad
        }






    }

    // Modificar el número de cambios posturales reflejados en el gráfico
    public void updateMaxSteps(int _n){
        MAX_STEPS = _n;
        steps = new float[MAX_STEPS];
        for(int i = 0; i < MAX_STEPS; i++){
            steps[i] = 10; // largo de la barra, no tiene utilidad en este caso ya que cada "step" lo consideramos una unidad
        }
    }

    public static void drawChart(ArrayList<Actividad> reg, ArrayList<String> labelsPosturas){
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, steps));

        BarDataSet bardataset = new BarDataSet(entries, "Posturas");

        /*ArrayList<String> labels = new ArrayList<String>();
        labels.add("2016");
        labels.add("2015");
        labels.add("2014");
        labels.add("2013");
        labels.add("2012");
        labels.add("2011");*/

        // Colores
        int[] colorClassArray = new int[MAX_STEPS];

        int n = 0;
        if(reg.size() < MAX_STEPS){
            n = MAX_STEPS - reg.size();

            for(int i = 0; i < n; i++){
                colorClassArray[i] = Color.GRAY; // se rellenan todos los huecos vacios con el color gris
            }
        }

        for(int i = 0; i < reg.size(); i++){
            colorClassArray[n] = colorcodes[reg.get(i).getId()-1];
            n++;
        }

        // Xaxis
        final ArrayList<String> xLabel = new ArrayList<>();
        xLabel.add("9");
        xLabel.add("15");
        xLabel.add("21");
        xLabel.add("27");
        xLabel.add("33");
        xLabel.add("Ahora");



        YAxis left = chartPosturas.getAxisLeft();
        left.setLabelCount(4);
        left.setValueFormatter(new MyValueFormatter());

        YAxis right = chartPosturas.getAxisRight();
        right.setEnabled(false);


        // Legend

        Legend legend = chartPosturas.getLegend();
        LegendEntry[] legendEntries = new LegendEntry[NUM_POSTURAS];

        for(int i = 0; i < colorcodes.length; i++){
            legendEntries[i] = new LegendEntry();
            legendEntries[i].label = labelsPosturas.get(i);
            legendEntries[i].formColor = colorcodes[i];
        }

        legend.setCustom(legendEntries);

        // Set data & refresh chart
        BarData data = new BarData(bardataset);
        chartPosturas.setData(data); // set the data and list of labels into chart
        //chartPosturas.setDescription();  // set the description
        bardataset.setColors(colorClassArray);
        bardataset.setDrawValues(false);
        chartPosturas.invalidate(); //refresh

        updateLastPosture(reg.get(reg.size()-1)); // Pasamos el último registro de actividad almacenado
    }

    private static void updateLastPosture (Actividad lastAct){
        switch (lastAct.getId()){
            case 1:
                iv_lastAct.setImageResource(R.drawable.postura1);
                break;
            case 2:
                iv_lastAct.setImageResource(R.drawable.postura2);
                break;
            case 3:
                iv_lastAct.setImageResource(R.drawable.postura3);
                break;
            case 4:
                iv_lastAct.setImageResource(R.drawable.postura4);
                break;
        }

        return;
    }

    private static class MyValueFormatter extends ValueFormatter
    {

        public MyValueFormatter() {

        }

        @Override
        public String getFormattedValue(float value) {
            switch ((int)value){
                case 200:
                    return "Ahora";
                case 100:
                    return "-10 s";
                case 0:
                    return "-20 s";
            }
            return "";
        }
    }
}
