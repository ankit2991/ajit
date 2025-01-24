package com.translate.languagetranslator.freetranslation.database.entity;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Keep
@Entity
public class ConversationModel implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String inputWord, translatedWord;
    private String origin;
    private String translatedWordLangCode, translatedWordLangName, translatedWordCountryCode;
    private String inputWordLangCode, inputWordLangName,inputWordCountryCode;
    private int speaking;
    private String groupPair;

    @Ignore
    boolean isConversationSelected;




    public ConversationModel() {
    }


    protected ConversationModel(Parcel in) {
        id = in.readInt();
        inputWord = in.readString();
        translatedWord = in.readString();
        origin = in.readString();
        translatedWordLangCode = in.readString();
        translatedWordLangName = in.readString();
        translatedWordCountryCode = in.readString();
        inputWordLangCode = in.readString();
        inputWordLangName = in.readString();
        inputWordCountryCode = in.readString();
        speaking = in.readInt();

        groupPair= in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(inputWord);
        dest.writeString(translatedWord);
        dest.writeString(origin);
        dest.writeString(translatedWordLangCode);
        dest.writeString(translatedWordLangName);
        dest.writeString(translatedWordCountryCode);
        dest.writeString(inputWordLangCode);
        dest.writeString(inputWordLangName);
        dest.writeString(inputWordCountryCode);
        dest.writeInt(speaking);
        dest.writeString(groupPair);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ConversationModel> CREATOR = new Creator<ConversationModel>() {
        @Override
        public ConversationModel createFromParcel(Parcel in) {
            return new ConversationModel(in);
        }

        @Override
        public ConversationModel[] newArray(int size) {
            return new ConversationModel[size];
        }
    };

    public String getInputWord() {
        return inputWord;
    }

    public void setInputWord(String inputWord) {
        this.inputWord = inputWord;
    }

    public String getTranslatedWord() {
        return translatedWord;
    }

    public void setTranslatedWord(String translatedWord) {
        this.translatedWord = translatedWord;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getTranslatedWordLangCode() {
        return translatedWordLangCode;
    }

    public void setTranslatedWordLangCode(String translatedWordLangCode) {
        this.translatedWordLangCode = translatedWordLangCode;
    }

    public int getSpeaking() {
        return speaking;
    }

    public void setSpeaking(int speaking) {
        this.speaking = speaking;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTranslatedWordLangName() {
        return translatedWordLangName;
    }

    public void setTranslatedWordLangName(String translatedWordLangName) {
        this.translatedWordLangName = translatedWordLangName;
    }

    public String getTranslatedWordCountryCode() {
        return translatedWordCountryCode;
    }

    public void setTranslatedWordCountryCode(String translatedWordCountryCode) {
        this.translatedWordCountryCode = translatedWordCountryCode;
    }

    public String getInputWordLangCode() {
        return inputWordLangCode;
    }

    public void setInputWordLangCode(String inputWordLangCode) {
        this.inputWordLangCode = inputWordLangCode;
    }

    public String getInputWordLangName() {
        return inputWordLangName;
    }

    public void setInputWordLangName(String inputWordLangName) {
        this.inputWordLangName = inputWordLangName;
    }

    public String getInputWordCountryCode() {
        return inputWordCountryCode;
    }

    public void setInputWordCountryCode(String inputWordCountryCode) {
        this.inputWordCountryCode = inputWordCountryCode;
    }

    public String getGroupPair() {
        return groupPair;
    }

    public void setGroupPair(String groupPair) {
        this.groupPair = groupPair;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(this==obj){
            return true;
        }
         if (obj==null){
            return false;
        }
         if (getClass() != obj.getClass())
             return false;
         ConversationModel conversationModel= (ConversationModel) obj;
         return this.inputWord.equals(conversationModel.inputWord);
    }

    public boolean isConversationSelected() {
        return isConversationSelected;
    }

    public void setConversationSelected(boolean conversationSelected) {
        isConversationSelected = conversationSelected;
    }
}
