package callerid.truecaller.trackingnumber.phonenumbertracker.block.sim_info;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;

import java.util.ArrayList;

public class CustomAdapterSimInfo extends RecyclerView.Adapter<CustomAdapterSimInfo.MyViewHolder> {

    private final AppCompatActivity context;
    private ArrayList<C2557h> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout f13073u;
        private ImageView f13072t;
        private TextView f13074v;


        public MyViewHolder(View itemView) {
            super(itemView);
            f13073u = (RelativeLayout) itemView.findViewById(R.id.layoutMain);
            f13072t = (ImageView) itemView.findViewById(R.id.imgLogo);
            f13074v = (TextView) itemView.findViewById(R.id.txtName);
        }
    }

    public CustomAdapterSimInfo(AppCompatActivity context, ArrayList<C2557h> data) {
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_operator, parent, false);


        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {


        holder.f13074v.setText(((C2557h) dataSet.get(listPosition)).f13006c);
        holder.f13072t.setImageResource(((C2557h) dataSet.get(listPosition)).mo11251a());
        Picasso.get().load(Integer.valueOf(((C2557h) dataSet.get(listPosition)).mo11251a())).into(holder.f13072t);
        holder.f13073u.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count) {
                    Utils.ad_count = 0;
                    Utils.showInter(context, new adShowCallBack() {
                        @Override
                        public void adShown(Boolean bol) {
                            Intent intent = new Intent(context, DetailActivity.class);
                            intent.putExtra("position", listPosition);
                            context.startActivity(intent);
                        }

                        @Override
                        public void adFailed(Boolean fal) {
                            Intent intent = new Intent(context, DetailActivity.class);
                            intent.putExtra("position", listPosition);
                            context.startActivity(intent);
                        }
                    });

                }else{
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("position", listPosition);
                    context.startActivity(intent);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}