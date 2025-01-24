package com.translate.languagetranslator.freetranslation.models.dictionaryModel;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
@Keep
public class Meaning {

    @SerializedName("partOfSpeech")
    @Expose
    private String partOfSpeech;
    @SerializedName("definitions")
    @Expose
    private List<Definition> definitions = null;

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }
}
