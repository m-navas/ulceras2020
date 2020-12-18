package es.navas.ulceras.Utilities;

public class Sensor {
    Long timestamp;
    Float y, x, z;
    private String sensor;
    String properties;

    public Sensor() {

        this.sensor = null;
        this.y = null;
        this.x = null;
        this.z = null;
        this.sensor = null;
        this.properties = null;
    }

    public Sensor(String name) {
        this.sensor = name;
        this.y = null;
        this.x = null;
        this.z = null;
        this.sensor = null;
        this.properties = null;
    }

    public Sensor(Long timestamp, Float y, Float x, Float z, String sensor, String properties){
        this.timestamp = timestamp;
        this.y = y;
        this.x = x;
        this.z = z;
        this.sensor = sensor;
        this.properties = properties;
    }


    public String getSensorName() {
        return sensor;
    }

    public Float getX(){
        return x;
    }

    public Float getY(){
        return y;
    }

    public Float getZ(){
        return z;
    }

}
