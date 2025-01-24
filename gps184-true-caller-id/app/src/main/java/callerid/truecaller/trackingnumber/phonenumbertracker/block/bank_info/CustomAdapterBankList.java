package callerid.truecaller.trackingnumber.phonenumbertracker.block.bank_info;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

public class CustomAdapterBankList extends RecyclerView.Adapter<CustomAdapterBankList.MyViewHolder> {

    private final Context context;
    private ArrayList<DataModel3> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        ImageView imageViewIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            this.imageViewIcon = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }

    public CustomAdapterBankList(ArrayList<DataModel3> data, Context context) {
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout, parent, false);

        view.setOnClickListener(BankInfoActivity.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView textViewName = holder.textViewName;
        ImageView imageView = holder.imageViewIcon;
        textViewName.setText(dataSet.get(listPosition).getName());
        Picasso.get().load(Uri.parse("file:///android_asset/" + dataSet.get(listPosition).getImage() + ".png")).placeholder(R.drawable.icc_bank).into(imageView);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}