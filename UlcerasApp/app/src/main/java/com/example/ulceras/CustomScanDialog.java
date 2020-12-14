package com.example.ulceras;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class CustomScanDialog extends Dialog{
    public Activity a;
    private ListView lv_scan;
    private MyListViewAdapter lv_adapter;
    private Button btn_exit;
    private ArrayList<Device> devices_list;

    public CustomScanDialog(Activity a, ArrayList<Device> devices_list) {
        super(a);
        // TODO Auto-generated constructor stub
        this.a = a;
        this.devices_list = devices_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.scan_dialog);

        lv_scan = (ListView)findViewById(R.id.listview_scan_devices);
        btn_exit = (Button)findViewById(R.id.btn_scan_exit);
        //devices_list = getListItems();

        lv_adapter = new MyListViewAdapter(a, devices_list);
        lv_scan.setAdapter(lv_adapter);

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //finalize();
            }
        });

    }

    private ArrayList<Device> getListItems(){
        ArrayList<Device> listItems = new ArrayList<>();
        for(int i = 0; i < 10; i++)
            listItems.add(new Device("default", "11:11:11:11:11"));

        return listItems;
    }

    public void setItems (ArrayList<Device> newItems){
        devices_list = newItems;

        lv_adapter = new MyListViewAdapter(a, devices_list);
        lv_scan.setAdapter(lv_adapter);
    }
}
