package com.translate.languagetranslator.freetranslation.models.dictionaryModel;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


@Keep
public class Dictionary {
    @SerializedName("word")
    @Expose
    private String word;
    @SerializedName("phonetic")
    @Expose
    private String phonetic;
    @SerializedName("origin")
    @Expose
    private String origin;
    @SerializedName("meanings")
    @Expose
    private List<Meaning> meanings = null;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public List<Meaning> getMeanings() {
        return meanings;
    }

    public void setMeanings(List<Meaning> meanings) {
        this.meanings = meanings;
    }
}
