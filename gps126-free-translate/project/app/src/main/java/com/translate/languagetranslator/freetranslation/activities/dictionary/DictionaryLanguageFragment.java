package com.translate.languagetranslator.freetranslation.activities.dictionary;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.translate.languagetranslator.freetranslation.R;
import com.translate.languagetranslator.freetranslation.activities.dictionary.adapter.DictionaryLanguageAdapter;
import com.translate.languagetranslator.freetranslation.appUtils.Constants;
import com.translate.languagetranslator.freetranslation.appUtils.UtilsMethodsKt;
import com.translate.languagetranslator.freetranslation.interfaces.DictionaryLanguageSelectionInterface;
import com.translate.languagetranslator.freetranslation.interfaces.LanguageSelectedInterface;
import com.translate.languagetranslator.freetranslation.models.DictionaryLanguageModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DictionaryLanguageFragment extends DialogFragment implements DictionaryLanguageSelectionInterface {

    private static DictionaryLanguageFragment instance;
    private static final float DIM_AMOUNT = 0.4f;


    Context context;
    RecyclerView recyclerView;
    LanguageSelectedInterface langInterface;
    ImageView ivCross;
    //    EditText etSearch;
    List<DictionaryLanguageModel> langList = new ArrayList<>();

    public void setInterface(LanguageSelectedInterface langInterface) {
        this.langInterface = langInterface;
    }

    DictionaryLanguageAdapter languageAdapter;
    int languagePosition;


    public static DictionaryLanguageFragment newInstance() {
        return instance == null ? new DictionaryLanguageFragment() : instance;

    }


    public DictionaryLanguageFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        final Window window = dialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowLayoutParams = window.getAttributes();
            windowLayoutParams.dimAmount = DIM_AMOUNT;
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dictionary_language_fragment, container, false);
        createView(view);
        return view;
    }

    private void createView(View view) {


        languagePosition = UtilsMethodsKt.getDefaultLang(view.getContext(), Constants.DICTIONARY_LANG_POSITION);


        langList = getLanguages();
        for (int i = 0; i <= langList.size() - 1; i++) {
            if (i == languagePosition) {
                langList.get(i).setSelected(true);
            } else {
                langList.get(i).setSelected(false);
            }
        }
        recyclerView = view.findViewById(R.id.recycler_lang_dictionary);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        languageAdapter = new DictionaryLanguageAdapter();
        recyclerView.setAdapter(languageAdapter);
        languageAdapter.setData(langList);
        languageAdapter.setAdapterInterface(this);

        ivCross = view.findViewById(R.id.iv_cross_lang);
//        icClearInput= view.findViewById(R.id.iv_cross_lang_input);
        ivCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
//        etSearch= view.findViewById(R.id.et_search_lang);
//        setEtChangeListener();


    }


    private void changeWindowSizes(int width, int height) {
        final Window window = getDialog().getWindow();
        if (window != null) {
            window.setLayout(width, height);
        }
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    @Override
    public void onResume() {
        super.onResume();
        int height_fourth = getScreenHeight() / 4;
        int height = height_fourth * 3;
        changeWindowSizes(getScreenWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public int getTheme() {
        return R.style.CustomDialog;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public List<DictionaryLanguageModel> getLanguages() {
        List<DictionaryLanguageModel> languageModels = new ArrayList<>();
        languageModels.add(new DictionaryLanguageModel("English", "en", false));
        languageModels.add(new DictionaryLanguageModel("Hindi", "hi", false));
        languageModels.add(new DictionaryLanguageModel("Spanish", "es", false));
        languageModels.add(new DictionaryLanguageModel("French", "fr", false));
        languageModels.add(new DictionaryLanguageModel("Japanese", "ja", false));
        languageModels.add(new DictionaryLanguageModel("Russian", "ru", false));
        languageModels.add(new DictionaryLanguageModel("German", "de", false));
        languageModels.add(new DictionaryLanguageModel("Italian", "it", false));
        languageModels.add(new DictionaryLanguageModel("Korean", "ko", false));
        languageModels.add(new DictionaryLanguageModel("Brazilian Portuguese", "pt-BR", false));
        languageModels.add(new DictionaryLanguageModel("Chinese (Simplified)", "zh-CN", false));
        languageModels.add(new DictionaryLanguageModel("Arabic", "ar", false));
        languageModels.add(new DictionaryLanguageModel("Turkish", "tr", false));
        Collections.sort(languageModels, (o1, o2) -> o1.getLangName().compareTo(o2.getLangName()));


        return languageModels;
    }

    @Override
    public void onLanguageSelect(int position, String languageName, String languageCode) {
        UtilsMethodsKt.putPrefInt(context, Constants.DICTIONARY_LANG_POSITION, position);
        langInterface.selectedLanguage(languageName, languageCode);
        dismiss();


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
