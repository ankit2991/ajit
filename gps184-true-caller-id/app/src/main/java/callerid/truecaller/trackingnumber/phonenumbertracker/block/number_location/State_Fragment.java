package callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.adapter.State_Adapter;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.data.STD_Data;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.data.State_Data;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.utils.MySQLiteHelper;

import java.util.ArrayList;

public class State_Fragment extends Fragment {
    State_Adapter adapter;
    ArrayList<STD_Data> alDataCity = new ArrayList();
    ArrayList<State_Data> alDataState = new ArrayList();
    MySQLiteHelper db;
    ListView lvListViewState;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_state, container);
        this.lvListViewState = (ListView) v.findViewById(R.id.listview_state);
        this.db = new MySQLiteHelper(getActivity());
        this.alDataState = this.db.getStateList(getActivity());

        try {
            this.adapter = new State_Adapter(getActivity(), this.alDataState);
            this.lvListViewState.setAdapter(this.adapter);
        } catch (Exception e) {
        }

        this.lvListViewState.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
                try {
                    State_Fragment.this.db = new MySQLiteHelper(State_Fragment.this.getActivity());
                    State_Fragment.this.alDataCity = State_Fragment.this.db.getSTDListForState(State_Fragment.this.getActivity(), ((State_Data) State_Fragment.this.alDataState.get(position)).getId());
                    StdCodeActivity.updateCityView(((State_Data) State_Fragment.this.alDataState.get(position)).getId(), ((State_Data) State_Fragment.this.alDataState.get(position)).getState());
                } catch (Exception e) {
                }
            }
        });
        v.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

}
