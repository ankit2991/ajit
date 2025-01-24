package callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ISD_Adapter extends RecyclerView.Adapter<ISD_Adapter.ViewHolder> {
    List<CountryToPhonePrefix> list;

    List<CountryToPhonePrefix> alDataTemp = new ArrayList<>();
    private String CountryID;
    String stQuerry = "";

    public ISD_Adapter(List<CountryToPhonePrefix> all) {
        list = all;
        this.alDataTemp.addAll(list);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView code, name;

        public ViewHolder(View view) {
            super(view);
            code = (TextView) view.findViewById(R.id.code);
            name = (TextView) view.findViewById(R.id.name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.isd_code_item_layout, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        holder.code.setText("+" + alDataTemp.get(position).getPrefix());
        holder.name.setText("" + alDataTemp.get(position).getCountryName());
    }

    @Override
    public int getItemCount() {
        return alDataTemp.size();
    }


    public void filter(String charText) {
        charText = charText.toLowerCase().replace(" ", "");
        this.stQuerry = charText;
        this.alDataTemp.clear();
        if (charText.length() == 0) {
            this.alDataTemp.addAll(this.list);
        } else {
            Iterator it = this.list.iterator();
            while (it.hasNext()) {
                CountryToPhonePrefix wp = (CountryToPhonePrefix) it.next();
                if (new StringBuilder(String.valueOf(wp.getCountryName().trim())).append(wp.getPrefix()).toString().replace(" ", "").toLowerCase().contains(charText)) {
                    this.alDataTemp.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
}
