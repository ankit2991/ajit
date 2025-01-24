package com.translate.languagetranslator.freetranslation.activities.dictionary.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.translate.languagetranslator.freetranslation.R;
import com.translate.languagetranslator.freetranslation.interfaces.DictionaryLanguageSelectionInterface;
import com.translate.languagetranslator.freetranslation.models.DictionaryLanguageModel;

import java.util.ArrayList;
import java.util.List;

public class DictionaryLanguageAdapter extends RecyclerView.Adapter<DictionaryLanguageAdapter.DictionaryLangItemHolder> {

    List<DictionaryLanguageModel> languageModels = new ArrayList<>();
    private DictionaryLanguageSelectionInterface languageSelectionInterface;

    public void setAdapterInterface(DictionaryLanguageSelectionInterface languageSelectionInterface) {
        this.languageSelectionInterface = languageSelectionInterface;
    }

    @NonNull
    @Override
    public DictionaryLangItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.li_language_dictionary, parent, false);

        return new DictionaryLangItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DictionaryLangItemHolder holder, int position) {
        DictionaryLanguageModel languageModel = languageModels.get(position);
        if (position == languageModels.size() - 1) {
            holder.dividerView.setVisibility(View.GONE);
        } else {
            holder.dividerView.setVisibility(View.VISIBLE);
        }

        if (languageModel.isSelected()) {
            holder.ivSelectedLanguage.setVisibility(View.VISIBLE);
        } else {
            holder.ivSelectedLanguage.setVisibility(View.GONE);
        }
        holder.tvLanguageName.setText(languageModel.getLangName());
        holder.itemView.setOnClickListener(v -> languageSelectionInterface.onLanguageSelect(position, languageModel.getLangName(), languageModel.getLangCode()));

    }

    @Override
    public int getItemCount() {
        return languageModels.size();
    }

    public void setData(List<DictionaryLanguageModel> languageModels) {
        this.languageModels = languageModels;
        notifyDataSetChanged();
    }

    public static class DictionaryLangItemHolder extends RecyclerView.ViewHolder {

        TextView tvLanguageName;
        RadioButton ivSelectedLanguage;
        View dividerView;

        public DictionaryLangItemHolder(@NonNull View itemView) {
            super(itemView);
            tvLanguageName = itemView.findViewById(R.id.tv_lang_dictionary_adapter);
            ivSelectedLanguage = itemView.findViewById(R.id.iv_selected_lang);
            dividerView = itemView.findViewById(R.id.view_divider);
        }

    }
}
