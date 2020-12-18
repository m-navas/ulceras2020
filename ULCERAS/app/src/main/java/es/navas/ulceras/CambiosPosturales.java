package es.navas.ulceras;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import es.navas.ulceras.Utilities.RegistroPosturas;
import es.navas.ulceras.Utilities.Utils;

import static es.navas.ulceras.MainActivity.registroGeneralClasses;
import static es.navas.ulceras.MainActivity.registroGeneralPosturas;
import static es.navas.ulceras.MainActivity.cambiosPosturalesState;


public class CambiosPosturales extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static Context cambiosPosturalesContext;

    private static HorizontalBarChart chartPosturas;
    private static ImageView iv_lastAct;

    private static int NUM_POSTURAS = 4; // numero de posturas distintas que se tienen en cuenta, vendra determinado según el caso de uso

    private static int colorcodes[] = new int[]{Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW, Color.rgb(255, 127, 0), Color.MAGENTA}; //colores para cada postura asociados al id
    private static float[] steps;

    static int MAX_STEPS = 960;

    static HashMap<String, Integer> labelsColors = new HashMap<>();

    Button btn_load_data;

    Spinner spinner_time_interval;

    Integer time_interval = 30; // por defecto realizamos una consulta de 30 min

    // UIHandler para ejecutar hilo de la UI en funciones static
    public static Handler UIHandler;

    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambios_posturales);

        cambiosPosturalesContext = this;

        chartPosturas = findViewById(R.id.chartCambiosPosturales);
        iv_lastAct = findViewById(R.id.iv_lastPosture);
        btn_load_data = findViewById(R.id.btn_load_history_data);
        spinner_time_interval = findViewById(R.id.spinner_time_interval);

        init();

        steps = new float[MAX_STEPS];
        for(int i = 0; i < MAX_STEPS; i++){
            steps[i] = 1; // largo de la barra, no tiene utilidad en este caso ya que cada "step" lo consideramos una unidad
        }

        cambiosPosturalesState = true;

        chartPosturas.getDescription().setText("Cambios de postura");

        drawChart(false, 0);

        btn_load_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Envia mensaje de solicitud de datos de cambios posturales almacenados en la BD
                new Thread(new AsynPub(getMqtt(), "/record_data/recovery/positions", Integer.toString(time_interval))).start();
                Utils.log("Peticion de consulta de datos en BD enviada");
            }
        });

        Integer time_intervals[] = {30, 120, 240}; // 30 min, 2 h y 4 h
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, time_intervals);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner_time_interval.setAdapter(adapter);
        spinner_time_interval.setOnItemSelectedListener(this);

    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        time_interval = (Integer) parent.getItemAtPosition(pos);
        Utils.log("Seleccionado: "+ time_interval);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void init(){
        // establecer numero de posturas distintas que se tienen en cuenta
        NUM_POSTURAS = registroGeneralClasses.size();

        // establecer labels y colores
        int i = 0;
        for (Map.Entry<String, String> entry : registroGeneralClasses.entrySet()) {
            labelsColors.put(entry.getKey(), colorcodes[i]);
            i++; // i NO PUEDE SER MAYOR A colorcodes.size() !!
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

    public static void drawChart(boolean historic, final int numDatos){
        if(historic) {
            Utils.log("Cargando "+numDatos+" datos");
            runOnUI(new Runnable() {
                public void run() {
                    Toast.makeText(cambiosPosturalesContext, "Cargados "+numDatos+" datos", Toast.LENGTH_SHORT).show();
                }
            });
            Utils.log("Cargados "+numDatos+" datos");
        }


        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, steps));

        BarDataSet bardataset = new BarDataSet(entries, "Posturas");

        // Colores
        int[] colorClassArray = new int[MAX_STEPS];

        int n = 0;
        for(int i = 0; i < registroGeneralPosturas.size(); i++){
            if(registroGeneralPosturas.get(i).getL().equals("None")) {
                colorClassArray[i] = Color.GRAY; // se rellenan todos los huecos vacios con el color gris
                n++;
            }else{
                break;
            }

        }

        for(int i = n; i < registroGeneralPosturas.size(); i++){
            colorClassArray[i] = labelsColors.get(registroGeneralPosturas.get(i).getL());
        }


        YAxis left = chartPosturas.getAxisLeft();
        //left.setLabelCount(4);
        //left.setValueFormatter(new MyValueFormatter());

        YAxis right = chartPosturas.getAxisRight();
        right.setEnabled(false);


        // Legend
        Legend legend = chartPosturas.getLegend();
        LegendEntry[] legendEntries = new LegendEntry[NUM_POSTURAS];


        int i = 0;
        for (Map.Entry<String, Integer> entry : labelsColors.entrySet()) {
            legendEntries[i] = new LegendEntry();
            legendEntries[i].label = entry.getKey();
            legendEntries[i].formColor = entry.getValue();
            i++;
        }

        legend.setCustom(legendEntries);


        // Set data & refresh chart
        BarData data = new BarData(bardataset);
        chartPosturas.setData(data); // set the data and list of labels into chart
        bardataset.setColors(colorClassArray);
        bardataset.setDrawValues(false);
        chartPosturas.invalidate();

        updateLastPosture(registroGeneralPosturas.get(registroGeneralPosturas.size()-1)); // Pasamos el último registro de actividad almacenado
    }

    private static void updateLastPosture (RegistroPosturas.Posturas lastPosture){
        switch (lastPosture.getL()){
            case "S":
                iv_lastAct.setImageResource(R.drawable.postura1);
                break;
            case "INC":
                iv_lastAct.setImageResource(R.drawable.postura2);
                break;
            case "LI":
                iv_lastAct.setImageResource(R.drawable.postura3);
                break;
            case "LD":
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

    // --- BEGIN MQTT ---
    private CallbackConnection getMqtt() {

        CallbackConnection callbackConnection;
        try {
            MQTT mqtt = new MQTT();
            mqtt.setHost(MainActivity.BROKER);
            mqtt.setClientId(System.currentTimeMillis() + "");
            callbackConnection = mqtt.callbackConnection();

            callbackConnection.listener(new MyListener());
            callbackConnection.connect(new MyCallbackConnection());
            //Log.d("DEV_", "Connected MQTT");
            Utils.log("Connected MQTT!");
        } catch (Exception e) {
            callbackConnection = null;
            e.printStackTrace();
            //Log.d("DEV_", "error in connection");
            Utils.log("MQTT Error connection: ");
            Utils.log(e.toString());
        }
        return callbackConnection;
    }

    private class MyListener implements Listener {

        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {

        }

        @Override
        public void onFailure(Throwable value) {

        }
    }

    private class MyCallbackConnection implements Callback<Void> {

        @Override
        public void onSuccess(Void value) {
            Utils.log("Conectado!");
        }

        @Override
        public void onFailure(Throwable value) {
            Utils.log("Error MyCallbackConnection!");
        }
    }

    private class MyCallbackDisconnect implements Callback<Void> {
        @Override
        public void onSuccess(Void value) {
            Utils.log("Desconectado!");
        }

        @Override
        public void onFailure(Throwable value) {
            Utils.log("Error MyCallbackDisconnect!");
        }
    }

    public class AsynPub implements Runnable{

        CallbackConnection connection;
        String topic, msg;

        AsynPub(CallbackConnection connection, String topic, String msg){
            this.connection=connection;
            this.topic = topic;
            this.msg = msg;
        }
        @Override
        public void run() {
            connection.publish(topic, msg.getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
                public void onSuccess(Void v) {
                    Utils.log("Publicado en  "+topic);
                    close();

                }
                public void onFailure(Throwable value) {
                    Utils.log("Fallo en  "+topic);
                    close();
                }
            });

        }

        private void close(){
            connection.disconnect(new Callback<Void>() {
                public void onSuccess(Void v) {
                    // called once the connection is disconnected.
                }
                public void onFailure(Throwable value) {
                    // Disconnects never fail.
                }
            });
        }
    }

    // --- END MQTT ---
}
