package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.AppAddDataList;


import java.util.List;


public class Adapter_moreStart extends RecyclerView.Adapter<Adapter_moreStart.ViewHolder> {
    List<AppAddDataList> aartiModelList;
    Context context;

    public Adapter_moreStart(List<AppAddDataList> aartiModelList, StartPage mainList_activity) {
        this.aartiModelList = aartiModelList;
        context = mainList_activity;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);


        View listItem = layoutInflater.inflate(R.layout.iteem_module_ads, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        Picasso.get().load(aartiModelList.get(position).getNative_banner()).into(holder.imageView);

        holder.txtname.setText(aartiModelList.get(position).getNative_title());


        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String st = aartiModelList.get(position).getApp_link();
                    Uri uri = Uri.parse(st);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Something went wrong!! Please try again!!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return aartiModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView txtname;
        public RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);


            this.imageView = itemView.findViewById(R.id.img_view);
            this.txtname = itemView.findViewById(R.id.txt_name_app);
            relativeLayout = itemView.findViewById(R.id.relative_layout);
        }
    }
}
