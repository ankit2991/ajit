package com.gpsnavigation.maps.gpsroutefinder.routemap.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gpsnavigation.maps.gpsroutefinder.routemap.R;
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.VoiceModel;


import java.util.ArrayList;


public class VoiceAdapter extends RecyclerView.Adapter<VoiceAdapter.VoiceHolder>{

    ArrayList<VoiceModel> locations;
    ItemListener mListener;
    public VoiceAdapter(ArrayList<VoiceModel> nearByModels, Context c,ItemListener itemListener)
    {
        locations=nearByModels;
        this.mListener = itemListener;
    }

    public interface ItemListener {
        void onItemClick(VoiceModel item);
    }


    @NonNull
    @Override
    public VoiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voice_search, parent, false);
        return new VoiceHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VoiceHolder holder, final int position) {
        holder.location.setText(locations.get(position).getLocation());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(locations.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {

        return locations.size();
    }


    public class VoiceHolder extends RecyclerView.ViewHolder{

        public TextView location;
        public VoiceHolder(View itemView) {
            super(itemView);
            location=itemView.findViewById(R.id.location);
        }
    }
}
