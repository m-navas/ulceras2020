package es.navas.ulceras;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;

public class HistoricSensorData extends AppCompatActivity {

    Button btn_load_data, btn_select_sensor;
    Spinner spinner_load_data, spinner_select_sensor;
    Integer time_interval = 30; // por defecto realizamos una consulta de 30 min

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_sensor_data);

        btn_load_data = findViewById(R.id.btn_load_history);
        btn_select_sensor = findViewById(R.id.btn_select_sensor);
        spinner_load_data = findViewById(R.id.spinner_historic_time_interval);
        spinner_select_sensor = findViewById(R.id.spinner_select_sensor);


    }
}
