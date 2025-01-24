package com.translate.languagetranslator.freetranslation.models;

import androidx.annotation.Keep;

@Keep
public class LanguageModel {


    private String languageName, languageCode, supportedLangCode, languageMeaning;
    private int flag;
    private boolean isSupported;


    public LanguageModel(String supportedLangCode, String languageName, String languageCode, int flag, String languageMeaning) {
        this.supportedLangCode = supportedLangCode;
        this.languageName = languageName;
        this.languageCode = languageCode;
        this.flag = flag;
        this.languageMeaning = languageMeaning;
    }

    public LanguageModel(String supportedLangCode, String languageName, String languageCode, int flag) {
        this.supportedLangCode = supportedLangCode;
        this.languageName = languageName;
        this.languageCode = languageCode;
        this.flag = flag;
    }


    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public boolean isSupported() {
        return isSupported;
    }

    public void setSupported(boolean supported) {
        isSupported = supported;
    }

    public String getSupportedLangCode() {
        return supportedLangCode;
    }

    public void setSupportedLangCode(String supportedLangCode) {
        this.supportedLangCode = supportedLangCode;
    }

    public String getLanguageMeaning() {
        return languageMeaning;
    }

    public void setLanguageMeaning(String languageMeaning) {
        this.languageMeaning = languageMeaning;
    }
}
