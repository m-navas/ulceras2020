package es.navas.ulceras.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Alert {
    HashMap<String, String> classes;
    AlertData data;

    public HashMap<String, String> getClasses() {
        return classes;
    }

    public AlertData getData() {
        return data;
    }



    public static class AlertData implements Comparable<AlertData>{
        Long t; // timestamp
        Integer S;
        Integer LI;
        Integer LD; // identificadores (se corresponde de momento con la postura)



        public AlertData(){
            this.t = 0L;
            this.S = null;
            this.LD = null;
            this.LI = null;
        }

        public AlertData (Long t, Integer s, Integer li, Integer ld){
            this.t = t;
            this.S = s; this.LI = li; this.LD = ld;
        }

        @Override
        public int compareTo(AlertData o) {
            if(t > o.t)
                return 1;
            else if (t < o.t)
                return -1;
            else
                return 0;
        }

        public Integer getS() {
            return S;
        }

        public void setS(Integer s) {
            S = s;
        }

        public Integer getLI() {
            return LI;
        }

        public void setLI(Integer LI) {
            this.LI = LI;
        }

        public Integer getLD() {
            return LD;
        }

        public void setLD(Integer LD) {
            this.LD = LD;
        }
    }

    public Alert(){
        classes = new HashMap<>();
        data = new AlertData();
    }


}
