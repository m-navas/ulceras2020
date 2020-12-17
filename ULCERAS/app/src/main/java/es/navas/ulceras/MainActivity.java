package es.navas.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class MainActivity extends AppCompatActivity {

    // --- BEGIN MQTT ---
    static public String BROKER="tcp://192.168.4.1:1883";

    private CallbackConnection getMqtt(){

        CallbackConnection callbackConnection;
        try {
            MQTT mqtt=new MQTT();
            mqtt.setHost(BROKER);
            mqtt.setClientId(System.currentTimeMillis()+"");
            callbackConnection=mqtt.callbackConnection();

            callbackConnection.listener(new MyListener());
            callbackConnection.connect(new MyCallbackConnection());
        } catch (Exception e) {
            callbackConnection=null;
            e.printStackTrace();
            Log.d("MQTT", "error in connection");
        }
        return callbackConnection;
    }

    // --- END MQTT ---

    // --- SensorData Chart Parameters ---
    public static int indexB, indexC, indexD;

    public static SensorData  chartBData, chartCData, chartDData;

    private ImageView btn_disconnect, btn_chart, btn_visualization, btn_position_RT, btn_help, btn_history;

    VisualizationDialog visualizationDialog;
    private ArrayList<Sensor> myDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.log("Setup");

        new Thread(new AsynSub()).start(); // Subscription to topics
        visualizationDialog = new VisualizationDialog(this); // Inicializar cuadro de dialogo con el context del MainActivity

        myDevices.add(new Sensor("Vibration_sensor_1")); // name: nombre que le hemos asignado al sensor en el script Python, NO COINCIDE con nombre de la entidad del sensor en HomeAssistant
        myDevices.add(new Sensor("Vibration_sensor_2"));
        myDevices.add(new Sensor("Vibration_sensor_3"));

        Utils.log("Setup done, Hello world!");

        btn_visualization = findViewById(R.id.btn_visualization);

        btn_visualization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visualizationDialog.show();
            }
        });

        // SensorData parameters init
        sensorDataInit();
    }

    private void addNewSensorData(Vdata[] datos, SensorData chartData, int index){
        for(Vdata data: datos){
            chartData.add(index, (float)data.getX(), (float)data.getY(), (float)data.getZ());

            index++;

            if(chartData.getSize() > 30){
                int dif = chartData.getSize() - 30;
                for(int i = 0; i < dif; i++){
                    chartData.xData.remove(0);
                    chartData.yData.remove(0);
                    chartData.zData.remove(0);
                }

            }
        }

    }

    private void sensorDataInit(){
        chartBData = new SensorData();
        chartCData = new SensorData();
        chartDData = new SensorData();

        indexB = 0;
        indexC = 0;
        indexD = 0;
    }



    public class MyListener implements Listener {

        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
            String msg = "void";
            String sTopic = decode(topic);
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                body.writeTo(baos);
                msg = baos.toString();
            } catch (Exception e) {
                Utils.log("Error al obtener el mensaje publicado en el topic " + sTopic);
            }

            Utils.log("Mensaje del topic <"+sTopic+ ">: "+ msg);

            if (sTopic.equals("/test")) {
                Utils.log("Test MQTT " + msg);

                // Deserializamos
                Gson gson = new Gson();
                Vdata dato = gson.fromJson(msg, Vdata.class);

                Utils.log("dato recibido: "+dato.getX()+" - "+dato.getY()+" - "+dato.getZ()+" - "+dato.getT());

                // Añadimos dato al objeto chartData que contiene los datos que se dibujarán en el grafico
                chartDData.add(indexD, (float)dato.getX(), (float)dato.getY(), (float)dato.getZ());
                indexD++;

                if(chartDData.getSize() > 30){ // Borramos datos antiguos si se superan las 30 lecturas de datos
                    chartDData.xData.remove(0);
                    chartDData.yData.remove(0);
                    chartDData.zData.remove(0);
                }

                // Actualizamos grafico
                SensorDataVisualization.setDataChartD();

            }else{ // SensorData
                /*
                Gson gson = new Gson();
                Vdata[] datos = gson.fromJson(msg, Vdata[].class);

                for(int i = 0; i < datos.length; i++){
                    Utils.log("datos "+i+":"+(float)datos[i].getX()+" - "+(float)datos[i].getY()+" - "+(float)datos[i].getZ());
                }

                 */

                // Deserializamos
                Gson gson = new Gson();
                Vdata dato = gson.fromJson(msg, Vdata.class);

                if (sTopic.contains(myDevices.get(0).getName())) { // Sensor 1
                    Utils.log("Nuevos datos Sensor 1");
                    /*
                    PROCESADO DE UN BLOQUE DE DATOS
                    // cdd.setData_chart_A(datos);

                    //addNewSensorData(datos, chartDData, indexD);
                    for(Vdata data: datos){
                        chartDData.add(indexD, (float)data.getX(), (float)data.getY(), (float)data.getZ());

                        indexD++;

                        if(chartDData.getSize() > 30){
                            int dif = chartDData.getSize() - 30;
                            for(int i = 0; i < dif; i++){
                                chartDData.xData.remove(0);
                                chartDData.yData.remove(0);
                                chartDData.zData.remove(0);
                            }

                        }
                    }
                    Utils.log("chartDData size: "+chartDData.getSize()+" - indexD: "+indexD);
                    SensorDataVisualization.setDataChartD();

                     */


                    //Utils.log("dato recibido: "+dato.getX()+" - "+dato.getY()+" - "+dato.getZ()+" - "+dato.getT());

                    // Añadimos dato al objeto chartData que contiene los datos que se dibujarán en el grafico
                    chartDData.add(indexD, (float)dato.getX(), (float)dato.getY(), (float)dato.getZ());
                    indexD++;

                    if(chartDData.getSize() > 30){ // Borramos datos antiguos si se superan las 30 lecturas de datos
                        chartDData.xData.remove(0);
                        chartDData.yData.remove(0);
                        chartDData.zData.remove(0);
                    }

                    // Actualizamos grafico
                    SensorDataVisualization.setDataChartD();

                } else if (sTopic.contains(myDevices.get(1).getName())) { // Sensor 2

                    Utils.log("Nuevos datos Sensor 2");
                    /*
                    for(Vdata data: datos){
                        chartBData.add(indexB, (float)data.getX(), (float)data.getY(), (float)data.getZ());

                        indexB++;

                        if(chartBData.getSize() > 30){
                            int dif = chartBData.getSize() - 30;
                            for(int i = 0; i < dif; i++){
                                chartBData.xData.remove(0);
                                chartBData.yData.remove(0);
                                chartBData.zData.remove(0);
                            }

                        }
                    }

                    Utils.log("chartBData size: "+chartBData.getSize()+" - indexB: "+indexB);
                    SensorDataVisualization.setDataChartB();

                     */
                    // Añadimos dato al objeto chartData que contiene los datos que se dibujarán en el grafico
                    chartBData.add(indexB, (float)dato.getX(), (float)dato.getY(), (float)dato.getZ());
                    indexB++;

                    if(chartBData.getSize() > 30){ // Borramos datos antiguos si se superan las 30 lecturas de datos
                        chartBData.xData.remove(0);
                        chartBData.yData.remove(0);
                        chartBData.zData.remove(0);
                    }

                    // Actualizamos grafico
                    SensorDataVisualization.setDataChartB();

                } else if (sTopic.contains(myDevices.get(2).getName())){ // Sensor 3
                    Utils.log("Nuevos datos Sensor 3");
                    /*
                    for(Vdata data: datos){
                        chartCData.add(indexC, (float)data.getX(), (float)data.getY(), (float)data.getZ());

                        indexC++;

                        if(chartCData.getSize() > 30){
                            int dif = chartCData.getSize() - 30;
                            for(int i = 0; i < dif; i++){
                                chartCData.xData.remove(0);
                                chartCData.yData.remove(0);
                                chartCData.zData.remove(0);
                            }

                        }
                    }

                    Utils.log("chartCData size: "+chartCData.getSize()+" - indexC: "+indexC);
                    SensorDataVisualization.setDataChartC();

                     */
                    // Añadimos dato al objeto chartData que contiene los datos que se dibujarán en el grafico
                    chartCData.add(indexC, (float)dato.getX(), (float)dato.getY(), (float)dato.getZ());
                    indexC++;

                    if(chartCData.getSize() > 30){ // Borramos datos antiguos si se superan las 30 lecturas de datos
                        chartCData.xData.remove(0);
                        chartCData.yData.remove(0);
                        chartCData.zData.remove(0);
                    }

                    // Actualizamos grafico
                    SensorDataVisualization.setDataChartC();
                }
            }

            ack.run();
        }

        @Override
        public void onFailure(Throwable value) {

        }
    }

    public class MyCallbackConnection implements Callback<Void> {

        @Override
        public void onSuccess(Void value) {
            Utils.log("Conectado!");

        }

        @Override
        public void onFailure(Throwable value) {

        }
    }


    public class AsynSub implements Runnable{


        @Override
        public void run() {
            Topic[] topics = {new Topic("/test", QoS.AT_LEAST_ONCE), new Topic("/case/inertial/#", QoS.AT_LEAST_ONCE)};
            getMqtt().subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    Utils.log("Subscripto a mis topics");
                }
                public void onFailure(Throwable value) {
                    Utils.log("Fallo en mis topics");
                }
            });
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

    static public String decode(Buffer buffer) {
        try {
            return new String(buffer.getData(), buffer.getOffset(), buffer.getLength(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("A UnsupportedEncodingException was thrown for teh UTF-8 encoding. (This should never happen)");
        }
    }
}
