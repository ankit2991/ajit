package com.translate.languagetranslator.freetranslation.models;

import androidx.annotation.Keep;

@Keep
public class DictionaryLanguageModel {
    private String langName, langCode;
    boolean isSelected;

    public DictionaryLanguageModel(String langName, String langCode, boolean isSelected) {
        this.langName = langName;
        this.langCode = langCode;
        this.isSelected = isSelected;
    }

    public String getLangName() {
        return langName;
    }

    public void setLangName(String langName) {
        this.langName = langName;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
