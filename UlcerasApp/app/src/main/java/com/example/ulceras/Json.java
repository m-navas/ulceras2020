package com.example.ulceras;

import android.util.Pair;

import java.util.ArrayList;

public class Json {
    private ArrayList<Pair<Integer, String>> labels;
    private ArrayList<Pair<Long, Integer>> data;

    public Json (){
        labels = new ArrayList<>();
        labels.add(new Pair<Integer, String>(0, "izq"));
        labels.add(new Pair<Integer, String>(1, "der"));
        labels.add(new Pair<Integer, String>(2, "supino"));
        labels.add(new Pair<Integer, String>(3, "incorporado"));

        data = new ArrayList<>();
    }

    public void addData(Long t, Integer v){
        data.add(new Pair<>(t,v));
    }

    public void clearData(){
        data.clear();
    }
}
