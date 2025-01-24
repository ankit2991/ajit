
package com.translate.languagetranslator.freetranslation.models.translationModel;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
@Keep
public class Translation {

    @SerializedName("translatedText")
    @Expose
    private String translatedText;
    @SerializedName("detectedSourceLanguage")
    @Expose
    private String detectedSourceLanguage;

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getDetectedSourceLanguage() {
        return detectedSourceLanguage;
    }

    public void setDetectedSourceLanguage(String detectedSourceLanguage) {
        this.detectedSourceLanguage = detectedSourceLanguage;
    }

}
