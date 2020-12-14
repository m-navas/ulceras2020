package com.example.ulceras;

public class SensorData {
    String sensor, properties;
    Float x, y, z;
    Long timestamp;

    public SensorData (String _tactigon, String _property, Float _x, Float _y, Float _z, Long _timestamp){
        sensor = _tactigon;
        properties = _property;
        x = _x;
        y = _y;
        z = _z;
        timestamp = _timestamp;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Float getZ() {
        return z;
    }

    public void setZ(Float z) {
        this.z = z;
    }



    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
