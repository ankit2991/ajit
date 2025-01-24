package callerid.truecaller.trackingnumber.phonenumbertracker.block.near_by_place;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

import java.util.ArrayList;

public class CustomAdapterNearby extends RecyclerView.Adapter<CustomAdapterNearby.MyViewHolder> {

    private final Context context;
    private ArrayList<DataModel2> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewVersion;
        ImageView imageViewIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            this.imageViewIcon = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }

    public CustomAdapterNearby(Context context, ArrayList<DataModel2> data) {
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout2, parent, false);

        view.setOnClickListener(NearByPlaceActivity.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        TextView textViewName = holder.textViewName;
        ImageView imageView = holder.imageViewIcon;
        holder.textViewName.setSelected(true);

        textViewName.setText(dataSet.get(listPosition).getName());
        imageView.setImageResource(getdrawablefromRes(dataSet.get(listPosition).getImage()));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    private int getdrawablefromRes(String imagename) {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(imagename, "drawable", packageName);
        return resId;
    }
}