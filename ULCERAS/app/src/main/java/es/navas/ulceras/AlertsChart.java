package es.navas.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import es.navas.ulceras.Utilities.Alert;
import es.navas.ulceras.Utilities.Utils;

import static es.navas.ulceras.MainActivity.chartDataHist;
import static es.navas.ulceras.MainActivity.indexHist;
import static es.navas.ulceras.MainActivity.registroGeneralAlertas;

public class AlertsChart extends AppCompatActivity {

    private static LineChart chartAlerts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts_chart);

        chartAlerts = findViewById(R.id.chartAlerts);

        chartAlerts.setDragEnabled(true);
        chartAlerts.setScaleEnabled(true);

        if(registroGeneralAlertas.size() > 0){
            drawChart();
        }
    }

    public static void drawChart() {
        Utils.log("Num elements A hist: "+registroGeneralAlertas.size());
        ArrayList<Entry> Sdata = new ArrayList<>();
        ArrayList<Entry> LIdata = new ArrayList<>();
        ArrayList<Entry> LDdata = new ArrayList<>();

        for(int i = 0; i < registroGeneralAlertas.size(); i++){
            Sdata.add(new Entry(i, registroGeneralAlertas.get(i).getS()));
            LIdata.add(new Entry(i, registroGeneralAlertas.get(i).getLI()));
            LDdata.add(new Entry(i, registroGeneralAlertas.get(i).getLD()));
        }

        LineDataSet setS, setLI, setLD;

        setS = new LineDataSet(Sdata, "Supino");
        setS.setColor(Color.RED);
        setLI = new LineDataSet(LIdata, "Lateral izquierdo");
        setLI.setColor(Color.BLUE);
        setLD = new LineDataSet(LDdata, "Lateral derecho");
        setLD.setColor(Color.GREEN);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setS); // add the data sets
        dataSets.add(setLI); // add the data sets
        dataSets.add(setLD); // add the data sets

        LineData data = new LineData(dataSets);


        chartAlerts.setData(data);

        chartAlerts.setVisibleXRangeMaximum(5);

        chartAlerts.moveViewToX(registroGeneralAlertas.size());

    }
}
