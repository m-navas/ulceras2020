package com.example.ulceras;

public class Device {
    private String Name;
    private String MAC;

    public Device(){
        this.Name = null; this.MAC = null;
    }

    public Device (String name, String mac){
        this.Name = name;
        this.MAC = mac;
    }


    public String getName() {
        return Name;
    }

    public String getMac() {
        return MAC;
    }
}
