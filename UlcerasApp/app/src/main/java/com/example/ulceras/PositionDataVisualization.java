package com.example.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

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

import static com.example.ulceras.MainActivity.decode;

public class PositionDataVisualization extends AppCompatActivity {

    ImageView iv_current_pos;
    CallbackConnection connection;
    String topic = "/test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_data_visualization);

        iv_current_pos = (ImageView)findViewById(R.id.iv_current_pos);
        iv_current_pos.setImageResource(R.drawable.none);


        new Thread(new AsynSub()).start();

    }

    private void changePosImage2(String newPos){
        if(newPos.equals("sup")){
            Utils.log("Cambio a sup");
            iv_current_pos.setImageResource(R.drawable.supino);
        }else if (newPos.equals("izq")){
            Utils.log("Cambio a izq");
            iv_current_pos.setImageResource(R.drawable.izq);
        }else if (newPos.equals("der")){
            iv_current_pos.setImageResource(R.drawable.der);
        }else if (newPos.equals("incorporado")){
            iv_current_pos.setImageResource(R.drawable.incorporado);
        }else if (newPos.equals("supino_der")){
            iv_current_pos.setImageResource(R.drawable.supino_der);
        }else if (newPos.equals("supino_izq")){
            iv_current_pos.setImageResource(R.drawable.supino_izq);
        }
    }

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

    public class MyListener implements Listener {

        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onPublish(UTF8Buffer utopic, Buffer body, Runnable ack) {
            String msg = "void";
            String sTopic = decode(utopic);
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                body.writeTo(baos);
                msg = baos.toString();
            } catch (Exception e) {
                Utils.log("Error al obtener el mensaje publicado en el topic " + sTopic);
            }

            Utils.log("Mensaje del topic <"+sTopic+ ">: "+msg);

            if (sTopic.equals(topic)) {
                changePosImage2(msg);
            }
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
            Utils.log("Error MyCallbackConnection!");
        }
    }

    public class AsynSub implements Runnable {

        String subTopic = topic;

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
                    Utils.log("Fallo en suscripcion a " + subTopic);
                }
            });
        }
    }
}
