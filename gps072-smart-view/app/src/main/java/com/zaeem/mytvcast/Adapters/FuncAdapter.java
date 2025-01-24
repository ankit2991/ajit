package com.zaeem.mytvcast.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zaeem.mytvcast.R;

public class FuncAdapter extends RecyclerView.Adapter<FuncAdapter.BtnsViewHolder> {

    private String[] titles;
    private int[] images;
    private int[] shades;
    private Activity activity;

    public FuncAdapter(String[] titles, int[] images, int[] shades, Activity activity) {
        this.titles = titles;
        this.images = images;
        this.shades = shades;
        this.activity = activity;
    }

    @NonNull
    @Override
    public FuncAdapter.BtnsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new BtnsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuncAdapter.BtnsViewHolder holder, int position) {
        holder.title.setText(titles[position]);
        holder.image.setImageResource(images[position]);
        holder.shade.setImageResource(shades[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    public static class BtnsViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView image;
        private ImageView shade;

        public BtnsViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);
            shade = itemView.findViewById(R.id.shade);

        }
    }
}
