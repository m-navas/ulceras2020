# Proyecto Ulceras 2020

## ULCERAS App

Aplicación Android que consulta y muestra los datos de los sensores y servicios del sistema.

## Main.py

Script Python encargado de la recepción de datos por MQTT de los sensores ZigBee, llamada al servicio de persistencia
para los nuevos datos recibidos y envio por MQTT de datos para terceras aplicaciones.

## MySQL-handler.py

Script que se encarga de implementar servicios que interactuan con la base de datos MySQL
para almacenar de forma persistente y posteriormente recuperar datos de lectura de sensores y
datos posturales

## Positions_data_pub_loop.py

Script que simula la generación y publicación de datos de cambios posturales.