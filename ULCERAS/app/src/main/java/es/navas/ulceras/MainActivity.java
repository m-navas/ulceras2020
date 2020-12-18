package es.navas.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import es.navas.ulceras.Utilities.RegistroPosturas;
import es.navas.ulceras.Utilities.Sensor;
import es.navas.ulceras.Utilities.SensorData;
import es.navas.ulceras.Utilities.Utils;
import es.navas.ulceras.Utilities.Vdata;


public class MainActivity extends AppCompatActivity {

    Context mainContext;

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

    // --- BEGIN SensorData Chart Parameters ---
    public static int indexB, indexC, indexA;
    public static SensorData chartBData, chartCData, chartAData;
    // --- END SensorData Chart Parameters ---

    private ImageView btn_connection, btn_chart, btn_visualization, btn_help, btn_history;
    private TextView tv_connection;

    VisualizationDialog visualizationDialog;
    private ArrayList<Sensor> myDevices = new ArrayList<>();

    // --- BEGIN Sensors connection state ---
    private boolean connected;
    // --- END Sensors connection state ---

    // --- BEGIN Posturas ---
    public static ArrayList<RegistroPosturas.Posturas> registroGeneralPosturas; // Array FIFO donde almacenamos los nuevos datos de posturas
    public static HashMap<String, String> registroGeneralClasses; // Estructura que contiene las distintas clases (posturas) que manejamos, se cambia dinamicamente en cada ejecucion de la App
    public static boolean cambiosPosturalesState = false;
    // --- END Posturas ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContext = this;

        Utils.log("Setup");
        connected = false;

        new Thread(new AsynSub()).start(); // Subscription to topics
        visualizationDialog = new VisualizationDialog(this); // Inicializar cuadro de dialogo con el context del MainActivity

        myDevices.add(new Sensor("Vibration_sensor_1")); // name: nombre que le hemos asignado al sensor en el script Python, NO COINCIDE con nombre de la entidad del sensor en HomeAssistant
        myDevices.add(new Sensor("Vibration_sensor_2"));
        myDevices.add(new Sensor("Vibration_sensor_3"));

        // SensorData parameters init
        sensorDataInit();

        // Posturas
        registroGeneralPosturas = new ArrayList<>();
        for(int i = 0; i < 960; i++)
            registroGeneralPosturas.add(new RegistroPosturas.Posturas(0L, "None"));

        registroGeneralClasses = new HashMap<>();

        Utils.log("Setup done, Hello world!");

        // Interfaz

        btn_visualization = findViewById(R.id.btn_visualization);
        btn_connection = findViewById(R.id.btn_connection);
        tv_connection = findViewById(R.id.tv_connection);

        btn_visualization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visualizationDialog.show();
            }
        });

        btn_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSensorsState();
            }
        });


        // Intentar conectar con sensores al inicio
        changeSensorsState();

    }

    // --- BEGIN Posturas ---
    // Añade los nuevos datos recibidos al array de datos que representaremos en el gráfico
    private void nuevosDatosPosturas(String json, boolean historic){
        Gson gson = new Gson();

        // Deserialización
        Type type = new TypeToken<RegistroPosturas>(){}.getType();
        RegistroPosturas nuevoRegistro = gson.fromJson(json, type);


        // Sustrae los datos
        List<RegistroPosturas.Posturas> data = nuevoRegistro.getData();


        // Ordena los datos por el timestamp
        Collections.sort(data);


        // Añadimos los nuevos datos al registro
        for (RegistroPosturas.Posturas p : data){
            registroGeneralPosturas.add(p);
            registroGeneralPosturas.remove(0);
        }


        //Establecer el numero de posturas distintas (classes) si no se ha realizado en esta ejecución de la App
        if(registroGeneralClasses.isEmpty()){
            for (Map.Entry<String, String> entry : nuevoRegistro.getClasses().entrySet()) {
                System.out.println("clave=" + entry.getKey() + ", valor=" + entry.getValue());
                registroGeneralClasses.put(entry.getKey(), entry.getValue());
            }
        }

        //if(cambiosPosturalesState)
        CambiosPosturales.drawChart(historic, data.size());

    }
    // --- END Posturas ---

    // --- BEGIN Conexion/Desconexion ---

    private void changeSensorsState(){
        String msg;
        if(connected)
            msg = "F"; // Si conectado -> desconectar
        else
            msg = "T"; // Si desconectado -> conectar

        new Thread(new AsynPub(getMqtt(), "/connect", msg)).start();
    }

    // --- END Conexion/Desconexion

    // --- BEGIN SensorData ---
    private void sensorDataInit(){
        chartBData = new SensorData();
        chartCData = new SensorData();
        chartAData = new SensorData();

        indexB = 0;
        indexC = 0;
        indexA = 0;
    }

    // --- END SensorData ---

    // --- BEGIN MQTT ---
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

            if(sTopic.equals("/record_data/recovery/positions/reply")){ // recuperacion de datos de cambios posturales desde la BD
                Utils.log("Mensaje MQTT recibido de /record_data/recovery/positions/reply: "+msg);


                nuevosDatosPosturas(msg, true);
                ack.run();
            } else if(sTopic.equals("/positions")){ // datos de cambios posturales

                Utils.log(msg);
                nuevosDatosPosturas(msg, false);
                ack.run();
            } else if( sTopic.equals("/connect/reply")) { // conexion/desconexion
                if(msg.equals("Connected"))
                    connected = true;
                else if (msg.equals("Disconnected"))
                    connected = false;

                updateConnectionUI();

            }else if (sTopic.equals("/test")) {
                Utils.log("Test MQTT " + msg);

            }else{ // SensorData en tiempo real

                // Deserializamos
                Gson gson = new Gson();
                Vdata dato = gson.fromJson(msg, Vdata.class);

                if (sTopic.contains(myDevices.get(0).getName())) { // Sensor 1
                    Utils.log("Nuevos datos Sensor 1");

                    // Añadimos dato al objeto chartData que contiene los datos que se dibujarán en el grafico
                    chartAData.add(indexA, (float)dato.getX(), (float)dato.getY(), (float)dato.getZ());
                    indexA++;

                    if(chartAData.getSize() > 30){ // Borramos datos antiguos si se superan las 30 lecturas de datos
                        chartAData.xData.remove(0);
                        chartAData.yData.remove(0);
                        chartAData.zData.remove(0);
                    }

                    // Actualizamos grafico
                    SensorDataVisualization.setDataChartA();

                } else if (sTopic.contains(myDevices.get(1).getName())) { // Sensor 2

                    Utils.log("Nuevos datos Sensor 2");

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
            Topic[] topics = {new Topic("/positions", QoS.AT_LEAST_ONCE), new Topic("/case/inertial/#", QoS.AT_LEAST_ONCE), new Topic("/connect/reply", QoS.AT_LEAST_ONCE), new Topic("/record_data/recovery/positions/reply", QoS.AT_LEAST_ONCE)};
            getMqtt().subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    Utils.log("Subscrito a mis topics");
                    updateConnectionUI();
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

    // --- END MQTT ---

    /*
        Actualiza las imagenes y texto del botón de la interfaz de usuario que gestiona la conexión/desconexión de los sensores
     */
    private void updateConnectionUI() {

        new Thread() {
            public void run() {

                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Utils.log("Run!");
                                if(connected) {
                                    Utils.log("Sensores estan conectados");
                                    //btn_connection.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.disconnect)); // imagen de desconectar
                                    btn_connection.setImageResource(R.drawable.disconnect);
                                    tv_connection.setText("Desconectar");
                                    Toast.makeText(getBaseContext(), "Sensores conectados!", Toast.LENGTH_SHORT).show();
                                }else {
                                    Utils.log("Sensores estan desconectados");
                                    //btn_connection.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.start)); // imagen de conectar
                                    btn_connection.setImageResource(R.drawable.start);
                                    tv_connection.setText("Conectar");
                                    Toast.makeText(getBaseContext(), "Sensores desconectados!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

            }
        }.start();
    }
}
