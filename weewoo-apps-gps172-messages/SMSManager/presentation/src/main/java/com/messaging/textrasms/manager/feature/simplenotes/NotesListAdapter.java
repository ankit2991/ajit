package com.messaging.textrasms.manager.feature.simplenotes;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.messaging.textrasms.manager.R;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.ViewHolder> {

    private final int colourText;
    private final int colourBackground;
    private final NotesListActivity notesListActivity;
    private List<File> fullList, filesList;

    NotesListAdapter(NotesListActivity notesListActivity, int colourText, int colourBackground) {
        this.notesListActivity = notesListActivity;
        filesList = new ArrayList<>();
        fullList = new ArrayList<>();
        this.colourText = colourText;
        this.colourBackground = colourBackground;
    }

    @Override
    public void onBindViewHolder(@NonNull NotesListAdapter.ViewHolder holder, int position) {
        File file = filesList.get(position);
        String fileName = file.getName().substring(0, file.getName().length() - 4);
        holder.noteTitle.setText(fileName);
        holder.edit.setOnClickListener(v -> {
            notesListActivity.startActivity(NoteActivity.getStartIntent(notesListActivity, fileName));
        });
        holder.btnLayout.setOnClickListener(v -> {
            showPopup(holder.btnLayout, fileName, position);
        });
        holder.delete.setOnClickListener(v -> {
            deleteDialog(position);
        });
        holder.noteTitle.setOnClickListener(v -> {
            notesListActivity.passDataToCompose(HelperUtils.readFile(notesListActivity, fileName));
        });
        holder.mainLayout.setOnClickListener(v -> {
            notesListActivity.passDataToCompose(HelperUtils.readFile(notesListActivity, fileName));
        });
    }


    @NonNull
    @Override
    public NotesListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_list_item, parent, false);
        return new ViewHolder(inflatedView, colourText, colourBackground);
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    void updateList(List<File> files) {
        filesList = files;
        fullList = new ArrayList<>(filesList);
        notifyDataSetChanged();
    }

    void filterList(String query) {
        if (TextUtils.isEmpty(query)) {
            DiffUtil.calculateDiff(new NotesDiffCallback(filesList, fullList)).dispatchUpdatesTo(this);
            filesList = new ArrayList<>(fullList);
        } else {
            filesList.clear();
            for (int i = 0; i < fullList.size(); i++) {
                final File file = fullList.get(i);
                final String fileName = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
                if (fileName.contains(query)) {
                    filesList.add(fullList.get(i));
                }
            }
            DiffUtil.calculateDiff(new NotesDiffCallback(fullList, filesList)).dispatchUpdatesTo(this);
        }
    }

    private boolean deleteFile(int position) {
        File file = filesList.get(position);
        fullList.remove(file);
        filesList.remove(file);
        notifyItemRemoved(position);
        return file.delete();
    }

    private void deleteDialog(int pos) {
        new AlertDialog.Builder(notesListActivity)
                .setTitle(notesListActivity.getString(R.string.confirm_delete))
                .setMessage(notesListActivity.getString(R.string.confirm_delete_text))
                .setPositiveButton(notesListActivity.getString(android.R.string.yes), (dialog, which) -> {
                    if (deleteFile(pos)) {
                        notesListActivity.showEmptyListMessage();
                    } else {
                        Toast.makeText(notesListActivity, "Failed to delete note !", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(notesListActivity.getString(android.R.string.no), (dialog, which) -> {
                })
                .show();
    }

    public void showPopup(View v, String fileName, int position) {
        Context wrapper = new ContextThemeWrapper(notesListActivity, R.style.MyPopupOtherStyle);
        PopupMenu popup = new PopupMenu(wrapper, v);
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popup.getMenuInflater().inflate(R.menu.pop_up, popup.getMenu());
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);

                    break;
                }
            }
        } catch (Exception e) {

        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit:
                        notesListActivity.startActivity(NoteActivity.getStartIntent(notesListActivity, fileName));
                        break;
                    case R.id.delete:
                        deleteDialog(position);
                        break;

                }
                return true;
            }


        });
        popup.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView noteTitle;
        ImageView edit, delete, menu;
        RelativeLayout mainLayout;
        LinearLayout btnLayout;

        ViewHolder(View view, int colourText, int colourBackground) {
            super(view);
            mainLayout = view.findViewById(R.id.mainLayout);
            noteTitle = view.findViewById(R.id.tv_title);
            edit = view.findViewById(R.id.edit);
            delete = view.findViewById(R.id.delete);
            btnLayout = view.findViewById(R.id.btnLayout);
        }
    }

}
