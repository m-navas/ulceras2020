package es.navas.ulceras;

public class Sensor {
    private String Name;

    public Sensor() {
        this.Name = null;
    }

    public Sensor(String name) {
        this.Name = name;
    }


    public String getName() {
        return Name;
    }

}
