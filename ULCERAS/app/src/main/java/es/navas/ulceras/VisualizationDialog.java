package es.navas.ulceras;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

/*
    Dialogo que nos permite seleccionar el tipo de gráfico que deseammos visualizar:
        - Datos de lectura de los sensores en RT
        - Datos de cambios posturales en RT y carga de datos historicos
        - Datos de alertas
        - Datos historicos de los sensores
 */
public class VisualizationDialog extends Dialog implements View.OnClickListener {

    public Activity context;
    private Button btn_sensorsData, btn_positions, btn_alerts, btn_historic;


    public VisualizationDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.context = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.select_visualization);
        this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        btn_sensorsData = findViewById(R.id.btn_sensors_data);
        btn_positions = findViewById(R.id.btn_position);
        btn_alerts = findViewById(R.id.btn_alerts);
        btn_historic = findViewById(R.id.btn_historic_sensor_data);

        btn_sensorsData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, SensorDataVisualization.class);
                context.startActivity(i);
            }
        });

        btn_positions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, CambiosPosturales.class);
                context.startActivity(i);
            }
        });

        btn_alerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, AlertsChart.class);
                context.startActivity(i);
            }
        });

        btn_historic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, HistoricSensorData.class);
                context.startActivity(i);
            }
        });
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
