# -*- coding: utf-8 -*-
"""
Created on Fri Dec  4 12:05:42 2020

positions_data_pub_loop v2

@author: NAVAS

Generador de datos posturales aleatorios

v1: encapsulamiento en formato JSON y publicación en /record_data/save/positions para su almacenamiento persistente en BD

v2:  publicación en topic /positions para aplicaciones externas

v3: añadido marcador para poder visualizar mejor como evolucionan las publicaciones

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
    position_index = 0 #indice del array de labels que indica la postura que se publicara
    
    index = 0

    while True:
        current_time = int(round(time.time() * 1000))
        if (current_time - last_time) > 600000: # cada 10 minutos cambiamos la postura
            last_time = current_time
            new_position_index = random.randint(0,len(labels)-1) #seleccion aleatoria de la nueva postura
            
            while new_position_index == position_index:
                new_position_index = random.randint(0,len(labels)-1) # controla que no se repita la postura anterior
            
            position_index = new_position_index
            print ("Nueva postura: " + labels[position_index] )

        client.loop()
        time.sleep(15)

        # Simulacion de encapsulamiento de datos en formato JSON y publicación en topic
        json_data = "{\"classes\":{\"S\":\"Supino\",\"LI\":\"Lateral izquierdo\",\"LD\":\"Lateral derecho\"},\"data\":[{\"t\":"+str(current_time)+", \"l\":\""+labels[position_index]+"\"},{\"t\":"+str(current_time + 1)+", \"l\":\""+labels[position_index]+"\"},{\"t\":"+str(current_time + 2)+", \"l\":\""+labels[position_index]+"\"}]}"
        publish.single("/record_data/save/positions", json_data, hostname=broker)
        publish.single("/positions", json_data, hostname=broker) # publicacion para aplicaciones externas
        print("Publicado"+str(index))
        index = (index + 1) % 10