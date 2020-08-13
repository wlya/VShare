package com.vilian.vshare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private List<String> mNames;
    private List<String> mIPs;
    LayoutInflater layoutInflater;
    private TextView mName;
    private TextView mIP;
    public GridViewAdapter(Context context, List<String> names, List<String> ips) {
        this.context = context;
        this.mNames = names;
        this.mIPs = ips;
        layoutInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return mIPs.size();//注意此处
    }
    @Override
    public Object getItem(int position) {
        return mIPs.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.remote_item, null);
        mName = (TextView) convertView.findViewById(R.id.grid_name);
        mIP = (TextView) convertView.findViewById(R.id.grid_ip);
        mName.setText(mNames.get(position));
        mIP.setText(mIPs.get(position));
        return convertView;
    }
}