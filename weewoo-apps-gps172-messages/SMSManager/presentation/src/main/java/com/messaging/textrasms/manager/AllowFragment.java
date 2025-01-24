package com.messaging.textrasms.manager;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.messaging.textrasms.manager.databinding.BlockLayoutFragmentBinding;
import com.messaging.textrasms.manager.feature.AddFilterActivity;
import com.messaging.textrasms.manager.model.AllowNumber;
import com.messaging.textrasms.manager.model.FilterBlockedNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

public class AllowFragment extends Fragment {
    AllowAdapter adapter;
    AllowAdapter.OnCallLogItemClickListenerblock onItemClickListener;
    Realm mRealm;
    BlockLayoutFragmentBinding binding;
    private TextView no_data;
    private ArrayList<AllowNumber> list = new ArrayList<>();
    private ArrayList<FilterBlockedNumber> blocklist = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.block_layout_fragment, container, false);

        initComponents();
        return binding.getRoot();
    }

    public void initComponents() {
        mRealm = Realm.getDefaultInstance();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewblock.setLayoutManager(mLayoutManager);
        binding.recyclerViewblock.setItemAnimator(new DefaultItemAnimator());
        adapter = new AllowAdapter(getContext());
        binding.recyclerViewblock.setAdapter(adapter);
        loadData();
        new SwipeHelper(getActivity(), binding.recyclerViewblock) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new UnderlayButton(
                        "Delete",
                        0,
                        Color.parseColor("#FF3C30"), pos -> {
                    removeAllow(list.get(pos).getId(), pos);
                    list.remove(pos);
                    adapter.addAllCallLog(list);
                    adapter.notifyItemRemoved(pos);
                    adapter.notifyItemRangeChanged(pos, list.size());
                    if (list.size() == 0) {
                        binding.recyclerViewblock.setVisibility(View.GONE);
                        binding.noDatablock.setVisibility(View.VISIBLE);
                    }

                }
                ));
            }
        };
    }

    public void removeAllow(Long id, int pos) {
        try {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<AllowNumber> rows = realm.where(AllowNumber.class).equalTo("id", id).findAll();
                    rows.deleteAllFromRealm();


                }
            });
        } catch (RealmPrimaryKeyConstraintException e) {
            Toast.makeText(
                    getActivity(),
                    "Primary Key exists, Press Update instead",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public void loadData() {
        getAllowdata();
        if (list.size() == 0) {
            binding.recyclerViewblock.setVisibility(View.GONE);
            binding.noDatablock.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewblock.setVisibility(View.VISIBLE);
            binding.noDatablock.setVisibility(View.GONE);
        }
        adapter.addAllCallLog(list);
        getblockdata();
        adapter.notifyDataSetChanged();
        onItemClickListener = new AllowAdapter.OnCallLogItemClickListenerblock() {
            @Override
            public void onItemClicked(AllowNumber callLogInfo) {
                Intent intent = new Intent(getActivity(), AddFilterActivity.class);
                intent.putExtra("number", callLogInfo.getAddress());
                intent.putExtra("content", callLogInfo.getContent());
                intent.putExtra("selection", 2);
                intent.putExtra("sender", callLogInfo.getSender());
                intent.putExtra("fromtype", "Allow");

                getActivity().startActivityForResult(intent, 35);
            }
        };
        adapter.setOnItemClickListener(onItemClickListener);
    }

    public String GetCountryZipCode(String c) {
        String CountryID = "";
        String CountryZipCode = c;

        TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    public void addAllow(String toString, String s) {
        String locale = getActivity().getResources().getConfiguration().locale.getCountry();
        GetCountryZipCode(locale);
        try {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    AllowNumber employee = new AllowNumber();
                    employee.setAddress("+" + GetCountryZipCode(locale) + toString);

                    employee.setId(UUID.randomUUID().getMostSignificantBits());

                    realm.insertOrUpdate(employee);
                    getAllowdata();
                    adapter.addAllCallLog(list);
                    adapter.notifyDataSetChanged();
                    if (list.size() != 0) {
                        binding.recyclerViewblock.setVisibility(View.VISIBLE);
                        binding.noDatablock.setVisibility(View.GONE);
                    }
                }
            });
            getblockdata();
            if (containsName(blocklist, "+" + GetCountryZipCode(locale) + toString)) {
                removeblock("+" + GetCountryZipCode(locale) + toString);
            }
            binding.noDatablock.setVisibility(View.GONE);
        } catch (RealmPrimaryKeyConstraintException e) {
            Toast.makeText(
                    getActivity(),
                    "Primary Key exists, Press Update instead",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public boolean containsName(final List<FilterBlockedNumber> list, final String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return list.stream().filter(o -> o.getAddress().equals(name)).findFirst().isPresent();
        } else {
            for (FilterBlockedNumber event : list) {
                if (event.getAddress().equals(name)) {
                    return true;
                    //.................
                }
            }


        }
        return false;
    }

    public RealmResults<AllowNumber> getAllowdata() {
        list = new ArrayList<>();

        RealmResults<AllowNumber> results = mRealm.where(AllowNumber.class).findAll();
        list.addAll(mRealm.copyFromRealm(results));
        return results;
    }

    public RealmResults<FilterBlockedNumber> getblockdata() {
        blocklist = new ArrayList<>();
        RealmResults<FilterBlockedNumber> results = mRealm.where(FilterBlockedNumber.class).findAll();
        blocklist.addAll(mRealm.copyFromRealm(results));
        Log.d("getblockdata", "getblockdata: " + list.size());
        return results;
    }

    public void removeblock(String id) {
        try {
            Log.d("removeblock", "removeblock: " + id);
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<FilterBlockedNumber> rows = realm.where(FilterBlockedNumber.class).equalTo("address", id).findAll();
                    rows.deleteAllFromRealm();

                }
            });
        } catch (RealmPrimaryKeyConstraintException e) {
            Toast.makeText(
                    getActivity(),
                    "Primary Key exists, Press Update instead",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

}
