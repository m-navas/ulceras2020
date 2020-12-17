# -*- coding: utf-8 -*-
"""
Created on Wed Dec  2 10:44:08 2020

@author: NAVAS

Script que se encarga de implementar servicios que interactuan con la base de datos MySQL
para almacenar de forma persistente y posteriormente recuperar datos de lectura de sensores y
datos posturales

"""

import mysql.connector

import json
import paho.mqtt.client as mqtt
from collections import namedtuple
import time;


broker = "127.0.0.1"

mqtt_client = mqtt.Client()

class PositionData(object):
    """
    
    Abstracción de los datos posturales (marca temporal y label de postura)
    
    """
    def __init__(self,timestamp,label):
        self.t = timestamp
        self.l = label
    def __repr__(self):
        return self.toJson()
    def add_record_as_data(self,_record):
        self.__dict__.update(_record.__dict__)
    def add_record_as_attr(self,_record):
        self.record = _record
    def toJson(self):
        return json.dumps(self.__dict__)
    
def jsonDefault(OrderedDict):
    return OrderedDict.__dict__

class VData:
    """
    
    Abstracción de los datos leidos por un sensor, incluyendo los valores de cada eje (x,y,z) y 
    la etiqueta temporal de la lectura.
    
    """
    def __init__(self,x,y,z,t):
        #print ("time")
        self.x = x
        self.y = y
        self.z = z
        self.t = t
    def __repr__(self):
        return json.dumps(self, default=jsonDefault, indent=4)
    def add_record_as_data(self,_record):
        self.__dict__.update(_record.__dict__)
    def add_record_as_attr(self,_record):
        self.record = _record

class SensorData(object):
    """
    
    Representacion de la lectura de un sensor para su almacenamiento persistente en BD
    
    """
    def __init__(self,sensor,properties,x,y,z,timestamp):
        self.sensor = sensor
        self.properties = properties
        self.x = x
        self.y = y
        self.z = z
        self.timestamp = timestamp
    def __repr__(self):
        return self.toJson()
    def add_record_as_data(self,_record):
        self.__dict__.update(_record.__dict__)
    def add_record_as_attr(self,_record):
        self.record = _record
    def toJson(self):
        return json.dumps(self.__dict__)

def _json_object_hook(d): return namedtuple('X', d.keys())(*d.values())
def json2obj(data): return json.loads(data, object_hook=_json_object_hook)


def on_publish(client,userdata,result):             #create function for callback
    print("Data published successfully \n")
    pass

def on_connect(client, userdata, rc):
        print('Connected with result code ' + rc)
        # Subscribing in on_connect() means that if we lose the connection and
        # reconnect then subscriptions will be renewed. There are other methods to achieve this..
        client.subscribe("/record_data/#", 0)
        print("Suscrito a topic /record_data/#")

 
def on_message(client, userdata, msg):
    """
    
    Listener que recibe los mensajes MQTT publicados en los topics subscritos

    """
    
    print("New MQTT msg received!")
    data = str(msg.payload)

    if msg.topic == "/record_data/save/sensors_data":
        print("Nuevos datos recibidos")
        save_sensors(data)
    elif msg.topic == "/record_data/recovery/sensors":
        print("Solicitud de datos recibida")
        recovery_sensors(data)
    elif msg.topic == "/record_data/save/positions":
        print("Nuevos datos de cambios de postura recibidos")
        save_positions(data)
    elif msg.topic == "/record_data/recovery/positions":
        print("Solicitud de datos de cambios de postura recibida")
        recovery_positions(data)


def on_subscribe(mqttc, obj, mid, granted_qos):
    print("Subscribed: " + str(mid) + " " + str(granted_qos))

def on_log(mqttc, obj, level, string):
    print(string)


def recovery_sensors(minutes):
    """
    
    Consulta en la BD MySQL los datos de lecturas de sensores almacenados en los ultimos X minutos
    y los envia por MQTT en el topic /record_data/sensors/reply en formato JSON

    Parameters
    ----------
    minutes : Integer
        Número de minutos que deseamos recuperar de la BD desde el momento actual 

    Returns
    -------
    None.

    """
    millis = int(round(time.time() * 1000)) #current
    target_millis = int(minutes,10) * 60000 #millis dif
    millis = millis - target_millis #target millis time

    try:
        connection = mysql.connector.connect(host='localhost',
                                            database='ulceras_db',
                                            user='mnavas',
                                            password='mnavas123')

        mycursor = connection.cursor(buffered=True)

        sql_select_query = """SELECT * FROM sensors_data WHERE timestamp >= %s"""
        id = (millis,)

        mycursor.execute(sql_select_query, id)

        myresult = mycursor.fetchall()

        mycursor.close()

        items = []

        for x in myresult:
            sensordata = SensorData(x[0], x[1], x[2], x[3], x[4], x[5])
            items.append(sensordata)

        #print(items)
        json_data = str(items)
        #print("-----")
        #print(json_data)

        mqtt_client.publish("/record_data/recovery/sensors/reply", json_data)
        print("Mensaje publicado en /record_data/sensors/reply")

    except mysql.connector.Error as error:
        print("Failed to retrieve data from sensor_data table {}".format(error))

    finally:
        if (connection.is_connected()):
            connection.close()
            print("MySQL connection is closed")


def recovery_positions(minutes):
    """
    
    Consulta en la BD los datos posturales almacenados en los X ultimos minutos y los envía
    en formato JSON por MQTT

    Parameters
    ----------
    minutes : Integer
        Numero de minutos a recuperar desde el momento actual.

    Returns
    -------
    None.

    """
    millis = int(round(time.time() * 1000)) #current
    target_millis = int(minutes,10) * 60000 #millis dif
    millis = millis - target_millis #target millis time

    try:
        connection = mysql.connector.connect(host='localhost',
                                            database='ulceras_db',
                                            user='mnavas',
                                            password='mnavas123')

        mycursor = connection.cursor(buffered=True)

        sql_select_query = """SELECT * FROM positions_data WHERE timestamp >= %s"""
        id = (millis,)

        mycursor.execute(sql_select_query, id)

        myresult = mycursor.fetchall()

        mycursor.close()

        items = []

        for x in myresult:
            positiondata = PositionData(x[0], x[1])
            items.append(positiondata)

        #print(items)
        json_data = "{\"classes\":{\"S\":\"Supino\",\"LI\":\"Lateral izquierdo\",\"LD\":\"Lateral derecho\"},\"data\":" + str(items) + "}"
        #print("-----")
        print(json_data)

        mqtt_client.publish("/record_data/recovery/positions/reply", json_data)
        print("Mensaje publicado en /record_data/recovery/positions/reply")

    except mysql.connector.Error as error:
        print("Failed to retrieve data from sensor_data table {}".format(error))

    finally:
        if (connection.is_connected()):
            connection.close()
            print("MySQL connection is closed")



def save_sensors(data):
    """

    Deserializa los datos enviados por un sensor en formato JSON y los almacena en la BD local MySQL

    Parameters
    ----------
    data : JSON
        Datos que proceden de la lectura de un sensor, en formato VData (x, y, z, timestamp)
        con la estructura JSON.

    Returns
    -------
    None.

    """
    decoded_data = json2obj(data)

    x = decoded_data.vdata.x
    y = decoded_data.vdata.y
    z = decoded_data.vdata.z
    t = decoded_data.vdata.t

    try:
        connection = mysql.connector.connect(host='localhost',
                                         database='ulceras_db',
                                         user='mnavas',
                                         password='mnavas123')
        mySql_insert_query = """INSERT INTO sensors_data (sensor, property, x, y, z, timestamp)
                           VALUES
                           ('"""+decoded_data.sensor+"""', '"""+decoded_data.properties+"""', """+str(x)+""", """+str(y)+""", """+str(z)+""", """+str(t)+""") """

        cursor = connection.cursor()
        cursor.execute(mySql_insert_query)
        connection.commit()
        print(cursor.rowcount, "Record inserted successfully into sensor_data table")
        cursor.close()

    except mysql.connector.Error as error:
        print("Failed to insert record into sensor_data table {}".format(error))

    finally:
        if (connection.is_connected()):
            connection.close()
            print("MySQL connection is closed")

def save_positions(input_msg):
    """
    
    Parameters
    ----------
    input_msg : TYPE
        DESCRIPTION.

    Returns
    -------
    None.

    """

    data = json.loads(input_msg)


    try:
        connection = mysql.connector.connect(host='localhost',
                                         database='ulceras_db',
                                         user='mnavas',
                                         password='mnavas123')
        for i in data["data"]:
            positionData = PositionData(i['t'], i['l'])

            #mySql_insert_query = """INSERT INTO positions_data (timestamp, label) VALUES ('"""+str(positionData.t)+""", """+positionData.l+""") """
            mySql_insert_query = """INSERT INTO positions_data (timestamp, label)
                           VALUES
                           ('"""+str(positionData.t)+"""', '"""+positionData.l+"""') """
            print(mySql_insert_query)

            cursor = connection.cursor()
            cursor.execute(mySql_insert_query)
            connection.commit()
            print(cursor.rowcount, "Record inserted successfully into positions_data table")
            cursor.close()

    except mysql.connector.Error as error:
        print("Failed to insert record into positions_data table {}".format(error))

    finally:
        if (connection.is_connected()):
            connection.close()
            print("MySQL connection is closed")



if __name__=='__main__':
    mqtt_client.on_message = on_message
    mqtt_client.on_connect = on_connect
    mqtt_client.on_publish = on_publish
    mqtt_client.on_subscribe = on_subscribe

    mqtt_client.connect(broker,1883,5)

    mqtt_client.subscribe("/record_data/#", 0)


    while True:
            mqtt_client.loop()