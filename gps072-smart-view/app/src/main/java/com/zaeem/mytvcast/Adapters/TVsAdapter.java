package com.zaeem.mytvcast.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zaeem.mytvcast.Model.DeviceModel;
import com.zaeem.mytvcast.R;

import java.util.ArrayList;

public class TVsAdapter extends RecyclerView.Adapter<TVsAdapter.DeviceViewHolder> {

    private ArrayList<DeviceModel> devices;
    private Activity activity;

    public TVsAdapter(ArrayList<DeviceModel> devices, Activity activity) {
        this.devices = devices;
        this.activity = activity;
    }

    @NonNull
    @Override
    public TVsAdapter.DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connect_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TVsAdapter.DeviceViewHolder holder, int position) {
        holder.title.setText(devices.get(position).name);
        holder.series.setText(devices.get(position).series);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView series;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tv_name);
            series = itemView.findViewById(R.id.tv_series);

        }
    }
}
