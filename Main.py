# -*- coding: utf-8 -*-
"""
Spyder Editor

MQTT xiaomi sensors v3

Recepción de datos por MQTT de los sensores ZigBee, llamada al servicio de persistencia
para los nuevos datos recibidos y envio por MQTT de datos para terceras aplicaciones.

v2: Añadido servicio que permite el inicio/detención de la lectura de datos de sensores

v3: Corregido error de mensajes retenidos, ahora al suscribirnos al topic no nos llegan los mensajes
antiguos que no habian llegado al callback y corregido error de resuscripción, por el cual al volver a 
suscribirnos a los topic se creaban más de una instancia de suscripción que podría haber generado
comportamientos anómalos (repetición de mensajes)

v4: envio de datos de lectura de los sensores de uno en uno para su visualización directa en la aplicación Android

"""

import paho.mqtt.client as mqtt
import ast # para convertir string a tipo list
import time
import json

broker = "127.0.0.1"


class Sensor(object):
    """
    
    Abstracción de un sensor, contiene la información relevante que necesitamos conocer del mismo,
    como su identificador, propiedades y el flujo de datos que estamos almacenando de sus lecturas.
    
    """
    def __init__(self,sensor):
        self.sensor = sensor
        self.properties = "3-Axis orientation"
        self.streamdata = []
    def __repr__(self):
        return json.dumps(self, default=jsonDefault, indent=4)
    def add_record_as_data(self,_record):
        self.__dict__.update(_record.__dict__)
    def add_record_as_attr(self,_record):
        self.record = _record
    
    def add_data(self, vdata):
        self.streamdata.append(vdata)
    def get_streamdata_size(self):
        return len(self.streamdata)
    def get_streamdata_string(self):
        return str(self.streamdata)
    def clear_streamdata(self):
        self.streamdata = []
    def getSensor(self):
        return self.sensor
    def getProperties(self):
        return self.properties

def handle_new_sensor_data(mySensor, data):
    """
    
    Procesa los nuevos datos obtenidos mediante la lectura de un sensor, llamando al servicio de
    persistencia en BD y publicando los datos almacenados del sensor cuando sea necesario
    
    Parameters
    ----------
    mySensor : Sensor
        Sensor que ha emitido la lectura de los datos
    data : List <Integer>
        Nuevos datos recibidos por el sensor

    Returns
    -------
    None.

    """
    print(mySensor.getSensor())
    #print(data) # datos brutos recibidos, pero modificados a tipo list de python
        
    vdata = VData(data[0], data[1], data[2]) # datos en formato VData, añadiendo timestamp
    print(str(vdata))
    data = str(vdata)
    myMqttClient.publish("/case/inertial/" + mySensor.getSensor() + "/data", data)
        
    # Almacenamiento persistente de la lectura del sensor
    savedata = SensorData(mySensor.getSensor(), mySensor.getProperties(), vdata)
    #print(savedata.__repr__())
    myMqttClient.publish("/record_data/save/sensors_data", savedata.__repr__()) # Llamada al servicio de almacenamiento persistente
    print("Published new "+mySensor.getSensor()+" data in /case/inertial/" + mySensor.getSensor() + "/data")
    # Añadimos los nuevos datos a la lista de datos del sensor
    mySensor.add_data(vdata)
    
    # Publicamos los datos y limpiamos lista cuando tenemos más de 10 datos
    #print(mySensor.get_streamdata_size())
    if mySensor.get_streamdata_size() > 10:
        #print ("Publish " + mySensor.getSensor() + " data")
        # Con los sensores xiaomi no hace falta publicar en bloque pues la frecuencia de datos es muy baja
        #myMqttClient.publish("/case/inertial/" + mySensor.getSensor() + "/data", mySensor.get_streamdata_string())
        mySensor.clear_streamdata()

sensor1 = Sensor("Vibration_sensor_1")
sensor2 = Sensor("Vibration_sensor_2")
sensor3 = Sensor("Vibration_sensor_3")

class SensorData(object):
    """
    
    Representacion de la lectura de un sensor para su almacenamiento persistente en BD
    
    """
    
    def __init__(self,sensor,properties,vdata):
        self.sensor = sensor
        self.properties = properties
        self.vdata = vdata
    def __repr__(self):
        return json.dumps(self, default=jsonDefault, indent=4)
    def add_record_as_data(self,_record):
        self.__dict__.update(_record.__dict__)
    def add_record_as_attr(self,_record):
        self.record = _record

def jsonDefault(OrderedDict):
    return OrderedDict.__dict__

class VData(object):
    """
    
    Abstracción de los datos leidos por un sensor, incluyendo los valores de cada eje (x,y,z) y 
    la etiqueta temporal de la lectura.
    
    """
    def __init__(self,x,y,z):
        #print ("time")
        self.t = millis = int(round(time.time() * 1000))
        self.x = round(x,2)
        self.y = round(y,2)
        self.z = round(z,2)
    def __repr__(self):
        return json.dumps(self, default=jsonDefault, indent=4)
    def add_record_as_data(self,_record):
        self.__dict__.update(_record.__dict__)
    def add_record_as_attr(self,_record):
        self.record = _record
        
def on_message(client, userdata, msg):
    #print("New MQTT msg received!")
    #msglength = len(str(msg.payload))
    if msg.retain == 1:
        return None
    
    data = str(msg.payload)
    
    if msg.topic == "/connect":
        
        if data == 'T':
            client.reinitialise()
            mqtt_setup(client)
            print("Connected!")
        elif data == 'F':
            client.unsubscribe("homeassistant/binary_sensor/#")
            print("Disconnected!")
            
    # Converting string to list
    data = ast.literal_eval(data)
    
    #print (msg.topic + " " + str(msg.qos) + " " + data)
    

   # if connected == True:
    if msg.topic == "homeassistant/binary_sensor/vibration_sensor/orientation":
        handle_new_sensor_data(sensor1, data)
    
    elif msg.topic == "homeassistant/binary_sensor/vibration_3/orientation":
        handle_new_sensor_data(sensor2, data)
        
    elif msg.topic == "homeassistant/binary_sensor/vibration_4/orientation":
        handle_new_sensor_data(sensor3, data)

def on_connect(client, userdata, rc):
        print('Connected with result code ' + rc)
        # Subscribing in on_connect() means that if we lose the connection and
        # reconnect then subscriptions will be renewed. There are other methods to achieve this..
        client.subscribe("homeassistant/binary_sensor/#", 0)
        client.subscribe("connect")
        print("Suscrito a topic homeassistant/binary_sensor/# ")     
        
def on_publish(client, obj, mid):
    None
    
def on_subscribe(mqttc, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))

def mqtt_setup(myclient):
    myclient.on_message = on_message
    myclient.on_connect = on_connect
    myclient.on_publish = on_publish
    myclient.on_subscribe = on_subscribe

    myclient.connect(broker,1883,5)

    myclient.subscribe("homeassistant/binary_sensor/#", 0)
    myclient.subscribe("/connect")

if __name__ == '__main__':
    myMqttClient = mqtt.Client()
    mqtt_setup(myMqttClient)
    
    

    while True:
        myMqttClient.loop()