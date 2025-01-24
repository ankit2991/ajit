package callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.adapter.STD_Adapter;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.data.STD_Data;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.utils.MySQLiteHelper;

import java.util.ArrayList;

public class City_Fragment extends Fragment {
    STD_Adapter adapter;
    ArrayList<STD_Data> alData = new ArrayList();
    MySQLiteHelper db;
    ListView lvListView;
    TextView tvSelectedState;


    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_city, container);
        this.lvListView = (ListView) v.findViewById(R.id.listview);
        this.tvSelectedState = (TextView) v.findViewById(R.id.tv_selected_state);
        this.db = new MySQLiteHelper(getActivity());
        this.alData = this.db.getSTDList(getActivity());
        try {
            this.adapter = new STD_Adapter(getActivity(), this.alData);
            this.lvListView.setAdapter(this.adapter);
        } catch (Exception e) {
        }
        this.tvSelectedState.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    City_Fragment.this.alData.clear();
                    City_Fragment.this.alData = City_Fragment.this.db.getSTDList(City_Fragment.this.getActivity());
                    City_Fragment.this.adapter = new STD_Adapter(City_Fragment.this.getActivity(), City_Fragment.this.alData);
                    City_Fragment.this.lvListView.setAdapter(City_Fragment.this.adapter);
                    City_Fragment.this.tvSelectedState.setVisibility(8);
                } catch (Exception e) {
                }
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void CityperState(int State_ID, String state) {
        this.alData.clear();
        this.alData = this.db.getSTDListForState(getActivity(), State_ID);
        this.adapter = new STD_Adapter(getActivity(), this.alData);
        this.lvListView.setAdapter(this.adapter);
        this.tvSelectedState.setText(state);
        this.tvSelectedState.setVisibility(0);
    }
}
