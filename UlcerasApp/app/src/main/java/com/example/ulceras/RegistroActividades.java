package com.example.ulceras;

import java.util.ArrayList;
import java.util.HashMap;

public class RegistroActividades {

    HashMap<Integer, String> posturas;
    ArrayList<Actividad> registroActividades;

    public RegistroActividades (){
        posturas = new HashMap<Integer, String>();
        registroActividades = new ArrayList<>();
    }

    public void setPosturas (ArrayList<String> labels){
        posturas.clear();

        for(int i = 0; i < labels.size(); i++){
            posturas.put(i+1, labels.get(i)); // comienza con id=1 en el hashmap
        }
    }

    public String getPostura(Integer id){
        return posturas.get(id);
    }

    // Recibo datos parametrizados a partir de un JSON
    // y lo añado al array que se representará en la gráfica
    public void nuevaActividad (Actividad nuevaActividad){
        registroActividades.add(nuevaActividad); // Incluye id de actividad + timestamp
    }
}
