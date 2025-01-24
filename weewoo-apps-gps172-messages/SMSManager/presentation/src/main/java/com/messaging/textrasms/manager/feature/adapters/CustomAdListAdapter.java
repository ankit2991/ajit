package com.messaging.textrasms.manager.feature.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.messaging.textrasms.manager.R;
import com.messaging.textrasms.manager.feature.Activities.CustomAdLayoutActivity;
import com.messaging.textrasms.manager.feature.Activities.MainActivity;
import com.messaging.textrasms.manager.feature.models.AdModel;

import java.util.ArrayList;

public class CustomAdListAdapter extends RecyclerView.Adapter<CustomAdListAdapter.ViewHolder> {

    private final ArrayList<AdModel> filesList = new ArrayList<>();
    public CustomAdLayoutActivity notesListActivity;
    public MainActivity mainActivity;

    public CustomAdListAdapter(CustomAdLayoutActivity notesListActivity, ArrayList<AdModel> filesList1) {
        this.notesListActivity = notesListActivity;
        filesList.clear();

        if (filesList1 != null && filesList1.size() > 0) {
            filesList.addAll(filesList1);
        }
    }

    public CustomAdListAdapter(MainActivity notesListActivity, ArrayList<AdModel> filesList1) {
        this.mainActivity = notesListActivity;
        filesList.clear();
        if (filesList1 != null && filesList1.size() > 0) {
            filesList.addAll(filesList1);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdListAdapter.ViewHolder holder, int position) {
        AdModel file = filesList.get(position);
        if (notesListActivity != null) {
            Glide.with(notesListActivity)
                    .load(Uri.parse(file.getIconuri()))
                    .into(holder.app_icon);

            holder.app_text.setText(file.getTitle());
            if (appInstalledOrNot(notesListActivity, file.getApp_id())) {
            } else {

            }
        } else if (mainActivity != null && file.isIsvisible()) {
            holder.ad_txt.setVisibility(View.VISIBLE);
            Glide.with(mainActivity)
                    .load(Uri.parse(file.getIconuri()))
                    .into(holder.app_icon);

            holder.app_text.setText(file.getTitle());
            holder.itemView.setOnClickListener(v -> {
                mainActivity.passDataToCompose(file.getApp_id());
            });
        } else {
            holder.ad_txt.setVisibility(View.GONE);
        }
    }


    @NonNull
    @Override
    public CustomAdListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_cusom_ad_list, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    public void updateList(ArrayList<AdModel> filesList1) {
        filesList.clear();
        if (filesList1 != null && filesList1.size() > 0) {
            filesList.addAll(filesList1);
        }
        notifyDataSetChanged();
    }

    private boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView app_icon;
        TextView app_text, ad_txt;

        ViewHolder(View view) {
            super(view);
            app_icon = view.findViewById(R.id.app_icon);
            app_text = view.findViewById(R.id.app_text);
            ad_txt = view.findViewById(R.id.ad_txt);
        }
    }
}
