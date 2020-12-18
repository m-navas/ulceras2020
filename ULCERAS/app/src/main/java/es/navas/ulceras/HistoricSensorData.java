package es.navas.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.util.ArrayList;

import es.navas.ulceras.Utilities.Utils;

import static es.navas.ulceras.MainActivity.chartAData;
import static es.navas.ulceras.MainActivity.chartADataHist;
import static es.navas.ulceras.MainActivity.chartBData;
import static es.navas.ulceras.MainActivity.chartBDataHist;
import static es.navas.ulceras.MainActivity.chartCData;
import static es.navas.ulceras.MainActivity.chartCDataHist;
import static es.navas.ulceras.MainActivity.chartDataHist;
import static es.navas.ulceras.MainActivity.indexA;
import static es.navas.ulceras.MainActivity.indexAhist;
import static es.navas.ulceras.MainActivity.indexB;
import static es.navas.ulceras.MainActivity.indexBhist;
import static es.navas.ulceras.MainActivity.indexC;
import static es.navas.ulceras.MainActivity.indexChist;
import static es.navas.ulceras.MainActivity.indexHist;

public class HistoricSensorData extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    Button btn_load_data, btn_select_sensor;
    Spinner spinner_load_data, spinner_select_sensor;
    Integer time_interval = 30; // por defecto realizamos una consulta de 30 min

    private static LineChart chartA;

    static Integer selectedSensor = 1;
    TextView tv_sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_sensor_data);

        btn_load_data = findViewById(R.id.btn_load_history);
        btn_select_sensor = findViewById(R.id.btn_select_sensor);
        spinner_load_data = findViewById(R.id.spinner_historic_time_interval);
        spinner_select_sensor = findViewById(R.id.spinner_select_sensor);
        tv_sensor = findViewById(R.id.tv_historic_sensor);

        tv_sensor.setText("Sensor 1");

        //chartB = (LineChart)findViewById(R.id.chartBhist);
        //chartC = (LineChart)findViewById(R.id.chartChist);
        chartA = (LineChart)findViewById(R.id.chartAhist);

        //chartB.setDragEnabled(true);
        //chartC.setDragEnabled(true);
        chartA.setDragEnabled(true);


        //chartB.setScaleEnabled(true);
        //chartC.setScaleEnabled(true);
        chartA.setScaleEnabled(true);


        if(chartAData.getSize() > 0)
            setDataChartAHist();

        if(chartBData.getSize() > 0)
            setDataChartBhist();

        if(chartCData.getSize() > 0)
            setDataChartCHist();

        Integer time_intervals[] = {30, 120, 240}; // 30 min, 2 h y 4 h
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, time_intervals);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner_load_data.setAdapter(adapter);
        spinner_load_data.setOnItemSelectedListener(this);

        Integer sensors[] = {1, 2, 3};
        ArrayAdapter<Integer> sensorsAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, sensors);
        sensorsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner_select_sensor.setAdapter(sensorsAdapter);
        spinner_select_sensor.setOnItemSelectedListener(this);

        btn_load_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sensorName = "Vibration_sensor_1";
                switch (selectedSensor){
                    case 1:
                        sensorName = "Vibration_sensor_1";
                        break;
                    case 2:
                        sensorName = "Vibration_sensor_2";
                        break;
                    case 3:
                        sensorName = "Vibration_sensor_3";
                        break;
                }
                String msg = "{ \"sensor\":\""+ sensorName +"\", \"minutes\":"+ time_interval+"}";
                new Thread(new AsynPub(getMqtt(), "/record_data/recovery/sensors", msg)).start();
            }
        });

        btn_select_sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*chartDataHist.clear();
                indexHist = 0;
                updateUI();
                setDataChartHist();

                 */
                String sensorName = "Vibration_sensor_1";
                switch (selectedSensor){
                    case 1:
                        sensorName = "Vibration_sensor_1";
                        break;
                    case 2:
                        sensorName = "Vibration_sensor_2";
                        break;
                    case 3:
                        sensorName = "Vibration_sensor_3";
                        break;
                }
                String msg = "{ \"sensor\":\""+ sensorName +"\", \"minutes\":"+ time_interval+"}";
                new Thread(new AsynPub(getMqtt(), "/record_data/recovery/sensors", msg)).start();

                updateUI();
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        if((Integer) parent.getItemAtPosition(pos) == 1 || (Integer) parent.getItemAtPosition(pos) == 2 || (Integer) parent.getItemAtPosition(pos) == 3){
            selectedSensor = (Integer) parent.getItemAtPosition(pos);
            Utils.log("Seleccionado sensor "+selectedSensor);

        }else{
            time_interval = (Integer) parent.getItemAtPosition(pos);
            Utils.log("Seleccionado tiempo: "+ time_interval);
        }


    }

    public static void drawChart() {
        Utils.log("drawChart sensor "+selectedSensor);
        switch (selectedSensor){
            case 1:
                /*chartA.setVisibility(View.VISIBLE);
                chartB.setVisibility(View.GONE);
                chartC.setVisibility(View.GONE);

                 */
                setDataChartAHist();
                break;
            case 2:
                /*chartB.setVisibility(View.VISIBLE);
                chartA.setVisibility(View.GONE);
                chartC.setVisibility(View.GONE);

                 */
                setDataChartBhist();
                break;
            case 3:
                /*chartC.setVisibility(View.VISIBLE);
                chartB.setVisibility(View.GONE);
                chartA.setVisibility(View.GONE);

                 */
                setDataChartCHist();
                break;
        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public static void setDataChartHist(){
        Utils.log("Num elements A hist: "+chartDataHist.xData.size());
        resetChart();
        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartDataHist.xData, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartDataHist.yData, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartDataHist.zData, "Z");
        setZ.setColor(Color.GREEN);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setX); // add the data sets
        dataSets.add(setY); // add the data sets
        dataSets.add(setZ); // add the data sets

        LineData data = new LineData(dataSets);


        chartA.setData(data);

        chartA.setVisibleXRangeMaximum(5);

        chartA.moveViewToX(indexHist);

        if(indexHist == Integer.MAX_VALUE){
            indexHist = 0;
            chartDataHist.clear();
            chartA.clear();
        }

    }

    public static void setDataChartAHist(){
        Utils.log("Num elements A hist: "+chartADataHist.xData.size());
        resetChart();
        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartADataHist.xData, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartADataHist.yData, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartADataHist.zData, "Z");
        setZ.setColor(Color.GREEN);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setX); // add the data sets
        dataSets.add(setY); // add the data sets
        dataSets.add(setZ); // add the data sets

        LineData data = new LineData(dataSets);


        chartA.setData(data);

        chartA.setVisibleXRangeMaximum(5);

        chartA.invalidate();

        if(indexAhist == Integer.MAX_VALUE){
            indexAhist = 0;
            chartADataHist.clear();
            chartA.clear();
        }

    }

    public static void setDataChartBhist(){
        Utils.log("Num elements B hist: "+chartBDataHist.xData.size());
        resetChart();
        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartBDataHist.xData, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartBDataHist.yData, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartBDataHist.zData, "Z");
        setZ.setColor(Color.GREEN);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setX); // add the data sets
        dataSets.add(setY); // add the data sets
        dataSets.add(setZ); // add the data sets

        LineData data = new LineData(dataSets);

        chartA.setData(data);

        chartA.setVisibleXRangeMaximum(5);
        chartA.invalidate();

        if(indexBhist == Integer.MAX_VALUE){
            indexBhist = 0;
            chartBDataHist.clear();
            chartA.clear();
        }


    }

    public static void setDataChartCHist(){
        Utils.log("Num elements C hist: "+chartCDataHist.xData.size());
        resetChart();
        LineDataSet setX, setY, setZ;

        setX = new LineDataSet(chartCDataHist.xData, "X");
        setX.setColor(Color.RED);
        setY = new LineDataSet(chartCDataHist.yData, "Y");
        setY.setColor(Color.BLUE);
        setZ = new LineDataSet(chartCDataHist.zData, "Z");
        setZ.setColor(Color.GREEN);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setX); // add the data sets
        dataSets.add(setY); // add the data sets
        dataSets.add(setZ); // add the data sets

        LineData data = new LineData(dataSets);

        chartA.setData(data);

        chartA.setVisibleXRangeMaximum(5);

        chartA.invalidate();

        if(indexChist == Integer.MAX_VALUE){
            indexChist = 0;
            chartCDataHist.clear();
            chartA.clear();
        }

    }

    private void updateUI() {

        new Thread() {
            public void run() {

                try {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                           Toast.makeText(HistoricSensorData.this, "Mostrando datos de sensor "+selectedSensor, Toast.LENGTH_SHORT).show();
                           tv_sensor.setText("Sensor "+selectedSensor);
                        }
                    });
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();
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

    private static void resetChart() {
        if(chartA.getData() != null)
            chartA.getData().clearValues();

        chartA.clear();
        chartA.fitScreen();
        chartA.invalidate();
    }
}
