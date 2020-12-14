package com.example.ulceras;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

public class SelectVisualizationDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    private Button btn_sensorsData, btn_position, btn_alert;




    public SelectVisualizationDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.select_visualization);
        this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        btn_sensorsData = findViewById(R.id.btn_sensors_data);
        btn_position = findViewById(R.id.btn_position);
        btn_alert = findViewById(R.id.btn_alerts);

        btn_sensorsData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, SensorDataVisualization.class);
                c.startActivity(i);
            }
        });

        btn_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, PositionDataVisualization.class);
                c.startActivity(i);
            }
        });

        btn_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, SensorDataVisualization.class);
                c.startActivity(i);
            }
        });
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

}
