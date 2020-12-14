package com.example.ulceras;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public static ArrayList<RegistroPosturas.Posturas> registroGeneralPosturas; // Array FIFO donde almacenamos los nuevos datos de posturas
    public static HashMap<String, String> registroGeneralClasses;
    public static boolean cambiosPosturalesState = false;

    private static Context mainContext;
    private static final String CHANNEL_ID = "my-canal-id";
    public static ArrayList<Device> connectedDevices = new ArrayList<>();
    private ImageView btn_disconnect, btn_chart, btn_scan, btn_position_RT, btn_help, btn_history;
    private TextView tv_start;
    private Boolean connected = false;
    CustomDialog cdd;
    SelectVisualizationDialog selectVisualizationDialog;
    CustomScanDialog scanDialog;
    public static Activity c;
    public static final String EXTRA_JSON = "com.example.interfaz.JSON";
    public static final String EXTRA_CONNECTION = "com.example.interfaz.CONNECTION";
    public static final int MAX_DEVICES = 3;
    public static String sensorType;
    boolean startedChartPosturas = false;
    public static int MAX_STEPS = 960; // máximo número de cambios (segmentos de la barra) que se mostraran en el gráfico de cambios posturales

    // SIMULACIÓN GRAFICO CAMBIO POSTURAL
    private ArrayList<Actividad> registro = new ArrayList<>(); // CAMBIAR POR REGISTRO REAL
    private ArrayList<String> labels = new ArrayList<String>();
    static int id = 1;
    // SIMULACIÓN GRAFICO CAMBIO POSTURAL

    Handler handler = new Handler();

    ArrayList<Device> devices;

    public static CallbackConnection connection, scanConnection;

    // --- BEGIN MQTT SETTINGS ---

    //static public String BROKER = "tcp://192.168.0.101:1883"; // IP portatil router Sitecom
    //static public String BROKER = "tcp://192.168.43.143:1883"; // IP Thinkpad red WiFi-Sotano
    static public String BROKER = "tcp://192.168.4.1:1883"; // IP Raspberry PI AC

    //static public String BROKER = "tcp://172.20.10.3:1883"; // IP portatil iPhone

    private CallbackConnection getMqtt() {

        CallbackConnection callbackConnection;
        try {
            MQTT mqtt = new MQTT();
            mqtt.setHost(BROKER);
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

    //--- END MQTT SETTINGS ---


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContext = this;

        registroGeneralPosturas = new ArrayList<>();
        for(int i = 0; i < 960; i++)
            registroGeneralPosturas.add(new RegistroPosturas.Posturas(0L, "None"));

        registroGeneralClasses = new HashMap<>();

        // SIMULACION
        registro.add(new Actividad(1, 123l));
        registro.add(new Actividad(2, 124l));
        registro.add(new Actividad(1, 125l));

        labels.add("sup"); //id 1
        labels.add("inc"); //id 2 ...
        labels.add("der");
        labels.add("izq");
        // SIMULACION

        c = this;
        devices = new ArrayList<Device>();

        tv_start = findViewById(R.id.tv_start);
        btn_disconnect = findViewById(R.id.btn_disconnect);
        btn_chart = findViewById(R.id.btn_chart);
        btn_scan = findViewById(R.id.btn_scan);
        btn_position_RT = findViewById(R.id.btn_position_RT);
        btn_help = findViewById(R.id.btn_help);
        btn_history = findViewById(R.id.btn_history);

        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryChart.class);
                startActivity(intent);
            }
        });

        //*** Prueba grafico cambios posturales
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChartPosturas.class);
                startActivity(intent);
                startedChartPosturas = true;
            }
        });
        //***

        //cdd = null;
        //selectVisualizationDialog = null;
        selectVisualizationDialog = new SelectVisualizationDialog(c);
        sensorType = "acc";

        btn_position_RT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(MainActivity.this, PositionRT.class);
                startActivity(intent);

                 */

                Intent intent = new Intent(MainActivity.this, CambiosPosturales.class);
                startActivity(intent);
            }
        });

        btn_disconnect.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.disconnect));

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Utils.log("Numero de dispositivos: "+connectedDevices.size());

                if (connectedDevices.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No hay dispositivos conectados!", Toast.LENGTH_SHORT).show();
                } else {
                    for (Device d : connectedDevices) {
                        disconnectDevice(d);
                    }
                    connectedDevices.clear();
                    Utils.log("Num disp tras desconexión: " + connectedDevices.size());

                    connection.disconnect(new MyCallbackDisconnect());
                    Toast.makeText(MainActivity.this, "Dispositivos desconectados!", Toast.LENGTH_SHORT).show();
                    connected = !connected;
                    //connectedDevices.clear();
                }
            }
        });

        btn_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectVisualizationDialog != null) {
                    //cdd.show();
                    selectVisualizationDialog.show();

                    //Intent intent = new Intent(MainActivity.this, SensorDataVisualization.class);
                    //startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "No hay dispositivos conectados!", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });

        //connection = getMqtt();

        new Thread(new AsynSub()).start();

        //ejecutarTarea();

        createNotificationChannel();


        /*String jsonPosturas = "{\"classes\":{\"S\":\"Supino\",\"LI\":\"Lateral izquierdo\",\"LD\":\"Lateral derecho\"},\"data\":[{\"t\":123456, \"l\":\"S\"},{\"t\":123457, \"l\":\"LI\"},{\"t\":123458, \"l\":\"LD\"}]}";

        Utils.log(jsonPosturas);

        nuevosDatosPosturas(jsonPosturas);

        Utils.log("Estado registro general posturas:");
        for(int i = 0 ; i < registroGeneralPosturas.size(); i++){
            if(i > 950)
                Utils.log("Timestamp: "+registroGeneralPosturas.get(i).t + " - Label: "+registroGeneralPosturas.get(i).l);
        }



        RegistroPosturas test = new RegistroPosturas(3);

        Gson gson = new Gson();

        String output = gson.toJson(test);

        Utils.log(output);

         */

    }

    // Añade los nuevos datos recibidos al array de datos que representaremos en el gráfico
    private void nuevosDatosPosturas(String json){
        Utils.log("ENTRA nuevosDatosPosturas");
        Gson gson = new Gson();

        Type type = new TypeToken<RegistroPosturas>(){}.getType();
        RegistroPosturas nuevoRegistro = gson.fromJson(json, type);

        Utils.log("Deserializa");

        List<RegistroPosturas.Posturas> data = nuevoRegistro.getData();

        Utils.log("Obtiene datos");

        Collections.sort(data);

        Utils.log("Ordena datos");

        for (RegistroPosturas.Posturas p : data){
            //Utils.log("Timestamp: "+ p.t); // check sort OK!
            registroGeneralPosturas.add(p);
            registroGeneralPosturas.remove(0);
        }

        Utils.log("Datos añadidos al registro general");
        Utils.log("tamaño registro: " + registroGeneralPosturas.size());

        /*for (int i = registroGeneralPosturas.size() - 1; i > registroGeneralPosturas.size() - 6; i --){
            Utils.log("Dato "+i+": "+registroGeneralPosturas.get(i).t);
        }

         */

        //Establecer el numero de posturas distintas (classes)
        if(registroGeneralClasses.isEmpty()){
            for (Map.Entry<String, String> entry : nuevoRegistro.getClasses().entrySet()) {
                System.out.println("clave=" + entry.getKey() + ", valor=" + entry.getValue());
                registroGeneralClasses.put(entry.getKey(), entry.getValue());
            }
        }

        if(cambiosPosturalesState)
            CambiosPosturales.drawChart();


    }

    private static void notify(String msg){
        // Establecer la accion de toque de la notificación
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(mainContext, HistoryChart.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mainContext, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mainContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.alert_icon)
                .setContentTitle("Alerta")
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Mostrar la notificacion
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainContext);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());


    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MI_CANAL";
            String description = "Canal de notificaciones de alertas";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    // Send MQTT command to disconnect a device
    private void disconnectDevice(Device d) {
        CallbackConnection c = getMqtt();
        String msg = d.getMac();
        String topic = "/disconnect";

        new Thread(new AsynPub(c, topic, msg)).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Establish MQTT subscription to selected devices in Scan Activity
        if (!connected && !connectedDevices.isEmpty()) {
            //Utils.log("Conectando dispositivos");
            new Thread(new AsynSub_devices()).start();
            Toast.makeText(MainActivity.this, "Dispositivos conectados!", Toast.LENGTH_LONG).show();
            selectVisualizationDialog = new SelectVisualizationDialog(c);
            connected = !connected;
        }

    }

    // Sends MQTT command to start BLE Scan and subscribes to receive the list of found devices
    private void scan() {
        connection = getMqtt();
        String msg = "run";
        String topic = "/scan";

        new Thread(new AsynPub(connection, topic, msg)).start();

        new Thread(new AsynSubScan()).start();

    }

    public static void getHistory(String msg){
        //connection = getMqtt();

        String topic = "/retrieve";

        new Thread(new AsynPub(connection, topic, msg)).start();
        Utils.log("Publicado mqtt en /retrieve con msg:"+msg);
    }

    public class MyCallbackConnection implements Callback<Void> {

        @Override
        public void onSuccess(Void value) {
            Utils.log("Conectado!");
        }

        @Override
        public void onFailure(Throwable value) {
            Utils.log("Error MyCallbackConnection!");
        }
    }

    public class MyCallbackDisconnect implements Callback<Void> {
        @Override
        public void onSuccess(Void value) {
            Utils.log("Desconectado!");
        }

        @Override
        public void onFailure(Throwable value) {
            Utils.log("Error MyCallbackDisconnect!");
        }
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

            //Utils.log("Mensaje del topic <"+sTopic+ ">: "+msg);

            if (sTopic.equals("/scanreply")) {
                //Utils.log("Mensaje recibido de "+sTopic);
                //Utils.log(msg);
                ArrayList<Device> deviceArrayList = new ArrayList<>();

                connection.disconnect(new MyCallbackDisconnect());
                showScanActivity(msg);

                ack.run();

            } else if (sTopic.equals("/publoop")) {
                /*
                // Añadir nuevo valor de postura al registro
                Gson gson = new Gson();
                Actividad newAct = gson.fromJson(msg, Actividad.class);

                registro.add(newAct);
                if(registro.size() > MAX_STEPS)
                    registro.remove(0);

                if(startedChartPosturas)
                    ChartPosturas.drawChart(registro, labels);

                 */
                Utils.log(msg);
                nuevosDatosPosturas(msg);
                ack.run();
            }else if (sTopic.equals("/notification")) {
                // Mostrar notificación
                Utils.log("Notificacion! "+msg);
                MainActivity.notify(msg);
            }else if (sTopic.equals("/savedata")){
                Utils.log("Mensaje MQTT recibido de /savedata: "+msg);
                if(!msg.equals("[]")){
                    Utils.log("si hay datos en el tiempo especificado");
                    Gson gson = new Gson();
                    SensorData[] historyData = gson.fromJson(msg, SensorData[].class);
                    Utils.log("Num datos recibidos: "+historyData.length);
                    HistoryChart.drawChart(historyData);
                }else{
                    Utils.log("no hay datos en el tiempo especificado");
                }

                ack.run();
            } else if (sTopic.equals("/positionssavedata")){
                Utils.log("Mensaje MQTT recibido de /positionssavedata: "+msg);
                /*if(!msg.equals("[]")){
                    Utils.log("si hay datos en el tiempo especificado");
                    Gson gson = new Gson();
                    SensorData[] historyData = gson.fromJson(msg, SensorData[].class);
                    Utils.log("Num datos recibidos: "+historyData.length);
                    HistoryChart.drawChart(historyData);
                }else{
                    Utils.log("no hay datos en el tiempo especificado");
                }

                ack.run();

                 */

                nuevosDatosPosturas(msg);
                ack.run();
            } else {

                Gson gson = new Gson();
                Vdata[] datos = gson.fromJson(msg, Vdata[].class);

                if (!connectedDevices.isEmpty()) { //CHECK
                    if (!sTopic.contains(sensorType)) {
                        if (sTopic.contains(connectedDevices.get(0).getName())) {
                            // cdd.setData_chart_A(datos);
                            SensorDataVisualization.setDataChartA(datos);
                        } else if (sTopic.contains(connectedDevices.get(1).getName())) {
                            // cdd.setData_chart_B(datos);
                            SensorDataVisualization.setDataChartB(datos);
                        } else if (sTopic.contains(connectedDevices.get(2).getName())){
                            Utils.log("Nuevos datos Chart C");
                            SensorDataVisualization.setDataChartC(datos);
                        }
                    }
                }

                ack.run();
            }
        }

        @Override
        public void onFailure(Throwable value) {

        }
    }

    public class AsynSub_devices implements Runnable {

        String subTopic = "/case/inertial/#";

        @Override
        public void run() {
            //Utils.log("Suscribiendo a "+subTopic);
            Topic[] topics = {new Topic(subTopic, QoS.AT_LEAST_ONCE)};
            //connection = getMqtt(); !! COMENTADO PARA PROBAR
            connection.subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    Utils.log("Subscrito a " + subTopic);
                }

                public void onFailure(Throwable value) {
                    Utils.log("Fallo en suscripcion a " + subTopic);
                }
            });
        }
    }

    public class AsynSubScan implements Runnable {

        String subTopic = "/scanreply";

        @Override
        public void run() {
            //Utils.log("Suscribiendo a "+subTopic);
            Topic[] topics = {new Topic(subTopic, QoS.AT_LEAST_ONCE)};
            connection = getMqtt();
            connection.subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    Utils.log("Subscrito a " + subTopic);
                }

                public void onFailure(Throwable value) {
                    Utils.log("Fallo en " + subTopic);
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

    public static class AsynPub implements Runnable {

        CallbackConnection connection;
        String pub_msg;
        String pub_topic;

        AsynPub(CallbackConnection connection, String pub_topic, String pub_msg) {
            this.connection = connection;
            this.pub_topic = pub_topic;
            this.pub_msg = pub_msg;
        }

        @Override
        public void run() {
            //System.out.println("MQTT a publicar  "+eTopic.getText().toString());
            connection.publish(pub_topic, pub_msg.getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
                public void onSuccess(Void v) {
                    Utils.log("MQTT Publicado en  " + pub_topic);
                    close();

                }

                public void onFailure(Throwable value) {
                    Utils.log("MQTT Fallo en  " + pub_topic);
                    close();
                }
            });

        }

        private void close() {
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

    public class AsynSub implements Runnable {

        //String subTopic = "/postura";
        String subTopic = "/#";

        @Override
        public void run() {
            //Utils.log("Suscribiendo a "+subTopic);
            Topic[] topics = {new Topic(subTopic, QoS.AT_LEAST_ONCE)};
            connection = getMqtt();
            connection.subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    Utils.log("Subscrito a " + subTopic);
                }

                public void onFailure(Throwable value) {
                    Utils.log("Fallo en " + subTopic);
                }
            });
        }
    }


    private void showScanActivity(String json) {
        Intent intent = new Intent(MainActivity.this, Scan.class);
        intent.putExtra(EXTRA_JSON, json);
        startActivity(intent);
    }

    public void ejecutarTarea() {
        handler.postDelayed(new Runnable() {
            public void run() {

                // función a ejecutar
                tarea();

                handler.postDelayed(this, 5000);
            }

        }, 5000);

    }

    private void tarea(){
        /*Random rGen = new Random();

        PositionRT.addData(rGen.nextInt(4));
        Utils.log("Envio dato de postura");*/

        registro.add(new Actividad(id, 126l));
        if(registro.size() > MAX_STEPS)
            registro.remove(0);
        id++;
        if(id > 4) id=1;

        if(startedChartPosturas)
            ChartPosturas.drawChart(registro, labels);

    }



}
