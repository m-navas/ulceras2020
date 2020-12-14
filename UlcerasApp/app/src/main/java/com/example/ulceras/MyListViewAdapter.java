package com.example.ulceras;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Device> listItems;

    public MyListViewAdapter(Context context, ArrayList<Device> listItems){
        this.context = context; this.listItems = listItems;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Device getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Device item = (Device) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.item, null);

        TextView tv_device_name = (TextView) convertView.findViewById(R.id.tv_device_name);
        TextView tv_device_mac = (TextView) convertView.findViewById(R.id.tv_device_mac);

        tv_device_name.setText(item.getName());
        tv_device_mac.setText(item.getMac());

        return convertView;
    }


}
