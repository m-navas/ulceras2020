package com.example.ulceras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegistroPosturas {
    HashMap<String, String> classes;
    List<Posturas> data;

    public HashMap<String, String> getClasses() {
        return classes;
    }

    public List<Posturas> getData() {
        return data;
    }



    public static class Posturas implements Comparable<Posturas>{
        Long t;
        String l; // nombre postura

        public Posturas (Long t, String l){
            this.t = t;
            this.l = l;
        }

        @Override
        public int compareTo(Posturas o) {
            if(t > o.t)
                return 1;
            else if (t < o.t)
                return -1;
            else
                return 0;
        }
    }

    public RegistroPosturas(){
        classes = new HashMap<>();
        data = new ArrayList<>();
    }

    public RegistroPosturas(Integer a){
        classes = new HashMap<>();
        for(int i = 0; i < a; i++)
            classes.put("Test"+i, "Test");

        data = new ArrayList<>();

        for(int i = 0; i < a; i++)
            data.add(new Posturas(123456L+i, "S"));
    }
}
