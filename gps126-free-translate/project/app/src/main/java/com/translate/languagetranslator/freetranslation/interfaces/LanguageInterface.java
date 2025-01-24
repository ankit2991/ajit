package com.translate.languagetranslator.freetranslation.interfaces;

public interface LanguageInterface {


    void onSpeakerCLicked(String text,String langCode);
    void onLanguageSelect(String languageName, String languageCode, String languageSupportCode, String languageMeaning, int flag);

}
