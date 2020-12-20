# -*- coding: utf-8 -*-
"""
@author: NAVAS

alerts_data_pub_loop


"""


import time
import paho.mqtt.client as mqtt
import paho.mqtt.publish as publish
import random

broker = "127.0.0.1"

labels = ["S", "LI", "LD"]

def on_connect(client, userdata, rc):
        print('Connected with result code ' + rc)

def on_message(client, userdata, msg):
    print("New MQTT msg received!")

def on_publish(client, obj, mid):
    None

def on_subscribe(mqttc, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))

def on_log(mqttc, obj, level, string):
    print(string)


if __name__ == '__main__':
    client = mqtt.Client()
    client.on_message = on_message
    client.on_connect = on_connect
    client.on_publish = on_publish
    client.on_subscribe = on_subscribe

    client.connect(broker,1883,5)

    last_time = int(round(time.time() * 1000))
    
    index = 0

    while True:
        current_time = int(round(time.time() * 1000))
        alert_level_S = str(random.randint(0,100))
        alert_level_LI = str(random.randint(0,100))
        alert_level_LD = str(random.randint(0,100))

        client.loop()
        time.sleep(15)

        # Simulacion de encapsulamiento de datos en formato JSON y publicación en topic
        json_data = "{\"classes\":{\"S\":\"Supino\",\"LI\":\"Lateral izquierdo\",\"LD\":\"Lateral derecho\"},\"data\":{\"t\":"+str(current_time)+", \"S\":"+alert_level_S+", \"LI\":"+alert_level_LI+", \"LD\":"+alert_level_LD+"}}"
        print(json_data)
        
        publish.single("/record_data/save/alert", json_data, hostname=broker)
        publish.single("/alert", json_data, hostname=broker) # publicacion para aplicaciones externas
        print("Publicado"+str(index))
        index = (index + 1) % 10