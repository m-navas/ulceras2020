package es.navas.ulceras.Utilities;

public class Vdata {
    //Valores de los ejes del sensor
    private double x;
    private double y;
    private double z;
    //Etiqueta temporal
    private long t;

    public Vdata (double nx, double ny, double nz, long timestamp){
        x = nx; y = ny; z = nz; t = timestamp;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public long getT() {
        return t;
    }
}
