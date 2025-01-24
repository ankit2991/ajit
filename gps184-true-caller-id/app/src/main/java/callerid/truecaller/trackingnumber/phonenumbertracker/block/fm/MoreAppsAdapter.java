package callerid.truecaller.trackingnumber.phonenumbertracker.block.fm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.squareup.picasso.Picasso;

import java.util.List;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.AppAddDataList;


public class MoreAppsAdapter extends RecyclerView.Adapter<MoreAppsAdapter.MyViewHolder> {
    Activity activity;
    List<AppAddDataList> recList;
    View view;
    LayoutInflater inflater;

    public MoreAppsAdapter(Activity activity, List<AppAddDataList> recList) {
        this.activity = activity;
        this.recList = recList;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = inflater.inflate(R.layout.item_download1, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        Picasso.get().load(recList.get(position).getNative_icon()).into(holder.img_thumb);
        holder.txt_app_name.setText(recList.get(position).getNative_title());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent appStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(recList.get(position).getApp_link()));

                    activity.startActivity(appStoreIntent);
                } catch (android.content.ActivityNotFoundException exception) {
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return recList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img_thumb;
        TextView txt_app_name;

        public MyViewHolder(@NonNull View view) {
            super(view);
            img_thumb = view.findViewById(R.id.img_thumb);
            txt_app_name = view.findViewById(R.id.txt_app_name);
        }
    }
}
