package es.navas.ulceras;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class SensorData{
    public ArrayList<Entry> xData = new ArrayList<>();
    public ArrayList<Entry> yData = new ArrayList<>();
    public ArrayList<Entry> zData = new ArrayList<>();

    public SensorData(){

    }

    public void clear(){
        xData.clear();
        yData.clear();
        zData.clear();
    }

    public void add(int index, float x, float y, float z){
        this.xData.add(new Entry(index, x));
        this.yData.add(new Entry(index, y));
        this.zData.add(new Entry(index, z));
    }

    public int getSize(){
        return xData.size();
    }

    public ArrayList<Entry> getXEntries (){
        return xData;
    }

    public ArrayList<Entry> getYEntries (){
        return yData;
    }

    public ArrayList<Entry> getZEntries (){
        return zData;
    }
}
