package com.example.ulceras;

public class Actividad {
    private Integer id;
    private Long timestamp;

    public Actividad (Integer _id, Long _timestamp){
        id = _id;
        timestamp = _timestamp;
    }

    public Integer getId() {
        return id;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
