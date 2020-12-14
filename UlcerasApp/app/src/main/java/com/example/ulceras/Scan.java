package com.example.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static com.example.ulceras.MainActivity.MAX_DEVICES;
import static com.example.ulceras.MainActivity.connectedDevices;
import static com.example.ulceras.MainActivity.decode;

public class Scan extends AppCompatActivity {
    private ArrayList<Device> devices;
    private ListView lv_scan;
    private MyListViewAdapter lv_adapter;
    private Context scanContext;

    static private String BROKER = MainActivity.BROKER; //"tcp://192.168.0.101:1883"; // IP portatil router Sitecom

    private CallbackConnection getMqtt(){

        CallbackConnection callbackConnection;
        try {
            MQTT mqtt=new MQTT();
            mqtt.setHost(BROKER);
            mqtt.setClientId(System.currentTimeMillis()+"");
            callbackConnection=mqtt.callbackConnection();

            callbackConnection.listener(new Scan.MyListener());
            callbackConnection.connect(new Scan.MyCallbackConnection());
            //Log.d("DEV_", "Connected MQTT");
            Utils.log("Connected MQTT!");
        } catch (Exception e) {
            callbackConnection=null;
            e.printStackTrace();
            //Log.d("DEV_", "error in connection");
            Utils.log("MQTT Error connection: ");
            Utils.log(e.toString());
        }
        return callbackConnection;
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

    public class MyCallbackDisconnect implements Callback<Void>{
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
                Utils.log("Error al obtener el mensaje publicado en el topic "+sTopic);
            }

            //Utils.log("Mensaje del topic <"+sTopic+ ">: "+msg);

            Gson gson = new Gson();
            Device dev = gson.fromJson(msg, Device.class);

            if( sTopic.equals("/connected") ){

                MainActivity.connectedDevices.add(dev);
                //Utils.log("Conectado nuevo dispositivo " + dev.getName());
                Toast.makeText(scanContext, "Dispositivo "+dev.getName()+" conectado", Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        public void onFailure(Throwable value) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        devices = new ArrayList<>();

        scanContext = this;

        Intent intent = getIntent();
        String json = intent.getStringExtra(MainActivity.EXTRA_JSON);
        final CallbackConnection connection = getMqtt();

        Gson gson = new Gson();
        Device[] devices_list = gson.fromJson(json, Device[].class);

        if(devices.size() > 0)
            devices.clear();

        for(Device d: devices_list){
            //Utils.log(d.getName() + " - " + d.getMac());
            devices.add(new Device(d.getName(), d.getMac()));
        }

        Toast.makeText(this, "Num. dispositivos: " + devices.size() + " , Conectados: " + connectedDevices.size(), Toast.LENGTH_LONG).show();

        lv_scan = (ListView)findViewById(R.id.listview_scan_devices);
        lv_adapter = new MyListViewAdapter(this, devices);
        lv_scan.setAdapter(lv_adapter);

        lv_scan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Device device = lv_adapter.getItem(i);

                //Utils.log("Device selected: "+ device.getName() + " / "+device.getMac());

                Boolean connected = false;

                //Check if device is already connected
                for ( Device d: MainActivity.connectedDevices) {
                    if (d.getMac().equals(device.getMac())) {
                        connected = true;
                    }
                }

                if(connected){ // If device is already connected
                    Toast.makeText(scanContext, "Dispositivo " + device.getName() + " ya esta conectado", Toast.LENGTH_SHORT).show();
                    //Utils.log("Dispositivo ya conectado " + device.getName());
                }else{ // If device is not connected...
                    if(connectedDevices.size() < MAX_DEVICES){ // ...and there are less than 2 devices connected
                        new Thread(new Scan.AsynPub(getMqtt(), "/connect", device.getMac())).start();
                        new Thread(new AsynSub()).start();
                        Toast.makeText(scanContext, "Dispositivo " + device.getName() + " conectado", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(scanContext, "No se pueden conectar más dispositivos", Toast.LENGTH_SHORT).show();
                        //Utils.log("No se pueden conectar mas dispositivos, MAX_DEVICES: " + MAX_DEVICES);
                    }
                }
            }
        });
    }

    public class AsynPub implements Runnable{

        CallbackConnection connection;
        String pub_msg;
        String pub_topic;

        AsynPub(CallbackConnection connection, String pub_topic, String pub_msg){
            this.connection=connection;
            this.pub_topic=pub_topic;
            this.pub_msg=pub_msg;
        }

        @Override
        public void run() {
            //System.out.println("MQTT a publicar  "+eTopic.getText().toString());
            connection.publish(pub_topic, pub_msg.getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
                public void onSuccess(Void v) {
                    Utils.log("MQTT Publicado en  "+ pub_topic);
                    close();

                }
                public void onFailure(Throwable value) {
                    Utils.log("MQTT Fallo en  "+ pub_topic);
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


    public class AsynSub implements Runnable{

        String subTopic = "/connected";

        @Override
        public void run() {
            Utils.log("Suscribiendo a "+subTopic);
            Topic[] topics = {new Topic(subTopic, QoS.AT_LEAST_ONCE)};
            CallbackConnection connection = getMqtt();
            connection.subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    Utils.log("Subscrito a " + subTopic);
                }
                public void onFailure(Throwable value) {
                    Utils.log("Fallo en "+subTopic);
                }
            });
        }
    }
}
