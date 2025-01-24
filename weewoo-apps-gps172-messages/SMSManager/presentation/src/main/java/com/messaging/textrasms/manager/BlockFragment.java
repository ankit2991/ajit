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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

public class BlockFragment extends Fragment {
    BlockAdapter adapter;
    BlockAdapter.OnCallLogItemClickListenerblock onItemClickListener;
    Realm mRealm;
    BlockLayoutFragmentBinding binding;
    BlockMainFragment blockFragment;
    private ArrayList<FilterBlockedNumber> list = new ArrayList<>();
    private ArrayList<AllowNumber> allowlist = new ArrayList<>();

//    public BlockFragment(@NotNull BlockMainFragment blockMainFragment) {
//        blockFragment = blockMainFragment;
//    }


    // Default constructor
    public BlockFragment() {
        // Required empty public constructor
    }

    // Constructor with BlockMainFragment parameter
    public BlockFragment(@NotNull BlockMainFragment blockMainFragment) {
        blockFragment = blockMainFragment;
    }


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
        adapter = new BlockAdapter(getContext());
        binding.recyclerViewblock.setAdapter(adapter);
        loadData();


        new SwipeHelper(getActivity(), binding.recyclerViewblock) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new UnderlayButton(
                        "Delete",
                        0,
                        Color.parseColor("#FF3C30"),
                        new UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                removeblock(list.get(pos).getId(), pos);
                                //    blockFragment.unblock((list.get(pos).getAddress()));
                                list.remove(pos);
                                adapter.addAllCallLog(list);
                                adapter.notifyItemRemoved(pos);
                                adapter.notifyItemRangeChanged(pos, list.size());
                                if (list.size() == 0) {
                                    binding.recyclerViewblock.setVisibility(View.GONE);
                                    binding.noDatablock.setVisibility(View.VISIBLE);
                                }


                            }
                        }
                ));


            }
        };
    }

    public String GetCountryZipCode(String c) {
        String CountryID = "";
        String CountryZipCode = c;

        TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
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

    public void addblock(String toString, String s) {
        String locale = getActivity().getResources().getConfiguration().locale.getCountry();
        GetCountryZipCode(locale);
        Log.d("addblock", "addblock: " + locale + ">>" + GetCountryZipCode(locale));
        try {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    FilterBlockedNumber employee = new FilterBlockedNumber();
                    employee.setAddress("+" + GetCountryZipCode(locale) + toString);
                    //  employee.setName(s);
                    employee.setId(UUID.randomUUID().getMostSignificantBits());
//                    String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    // employee.setDate(currentDateTimeString);
                    realm.insertOrUpdate(employee);
                    getblockdata();
                    adapter.addAllCallLog(list);

                    adapter.notifyDataSetChanged();
                    if (list.size() != 0) {
                        binding.recyclerViewblock.setVisibility(View.VISIBLE);
                        binding.noDatablock.setVisibility(View.GONE);
                    }


                }
            });
            getallowdata();
            if (containsName(allowlist, "+" + GetCountryZipCode(locale) + toString)) {
                removeallow("+" + GetCountryZipCode(locale) + toString);
            }
        } catch (RealmPrimaryKeyConstraintException e) {
            Toast.makeText(
                    getActivity(),
                    "Primary Key exists, Press Update instead",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public RealmResults<AllowNumber> getallowdata() {
        allowlist = new ArrayList<>();
        RealmResults<AllowNumber> results = mRealm.where(AllowNumber.class).findAll();
        allowlist.addAll(mRealm.copyFromRealm(results));
        Log.d("getblockdata", "getblockdata: " + allowlist.size());
        return results;
    }

    public void removeallow(String id) {
        try {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<AllowNumber> rows = realm.where(AllowNumber.class).equalTo("address", id).findAll();
                    rows.deleteAllFromRealm();
                    // MainActivity.Companion.setDatachsnged(true);


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

    public boolean containsName(final List<AllowNumber> list, final String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return list.stream().filter(o -> o.getAddress().equals(name)).findFirst().isPresent();
        } else {
            for (AllowNumber event : list) {
                if (event.getAddress().equals(name)) {
                    return true;
                    //.................
                }
            }


        }
        return false;
    }

    public void loadData() {
        getblockdata();
        if (list.size() == 0) {
            binding.recyclerViewblock.setVisibility(View.GONE);
            binding.noDatablock.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewblock.setVisibility(View.VISIBLE);
            binding.noDatablock.setVisibility(View.GONE);
        }
        adapter.addAllCallLog(list);
        adapter.notifyDataSetChanged();
        getallowdata();
        onItemClickListener = new BlockAdapter.OnCallLogItemClickListenerblock() {
            @Override
            public void onItemClicked(FilterBlockedNumber callLogInfo) {
                Intent intent = new Intent(getActivity(), AddFilterActivity.class);
                intent.putExtra("number", callLogInfo.getAddress());
                intent.putExtra("content", callLogInfo.getContent());
                intent.putExtra("selection", 1);
                intent.putExtra("sender", callLogInfo.getSender());
                intent.putExtra("fromtype", "Block");
                getActivity().startActivityForResult(intent, 35);
            }
        };
        adapter.setOnItemClickListener(onItemClickListener);
    }

    public RealmResults<FilterBlockedNumber> getblockdata() {
        list = new ArrayList<>();
        RealmResults<FilterBlockedNumber> results = mRealm.where(FilterBlockedNumber.class).findAll();
        list.addAll(mRealm.copyFromRealm(results));
        Log.d("getblockdata", "getblockdata: " + list.size());
        return results;
    }

    public void removeblock(Long id, int pos) {
        try {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<FilterBlockedNumber> rows = realm.where(FilterBlockedNumber.class).equalTo("id", id).findAll();
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
