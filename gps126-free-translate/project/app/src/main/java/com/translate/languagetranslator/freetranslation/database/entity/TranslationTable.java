package com.translate.languagetranslator.freetranslation.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.translate.languagetranslator.freetranslation.interfaces.History;


@Entity
public class TranslationTable implements Parcelable, History {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int id;

    @ColumnInfo
    public String inputLanguage;
    @ColumnInfo
    public String outputLanguage;

    @ColumnInfo
    public String inputStr;
    @ColumnInfo
    public String outputStr;

    @ColumnInfo
    public boolean isfav;

    @ColumnInfo
    private String sourceLanCode;

    @ColumnInfo(name = "unique_iden")
    private String unique_iden;

    @ColumnInfo
    private String destLanCode;

    public boolean isChek;

    @Ignore
    public boolean historySelected;



    public TranslationTable(String inputLanguage, String outputLanguage, String inputStr, String outputStr, String sourceLanCode, String destLanCode, String unique_iden, boolean isfav) {
        this.inputLanguage = inputLanguage;
        this.outputLanguage = outputLanguage;
        this.inputStr = inputStr;
        this.outputStr = outputStr;
        this.sourceLanCode = sourceLanCode;
        this.destLanCode = destLanCode;
        this.unique_iden = unique_iden;
        this.isfav = isfav;
    }


    protected TranslationTable(Parcel in) {
        id = in.readInt();
        inputLanguage = in.readString();
        outputLanguage = in.readString();
        inputStr = in.readString();
        outputStr = in.readString();
        unique_iden = in.readString();
        sourceLanCode=in.readString();
        destLanCode=in.readString();
        isfav = in.readByte() != 0;
    }

    public TranslationTable() {
        super();
    }

    public static final Creator<TranslationTable> CREATOR = new Creator<TranslationTable>() {
        @Override
        public TranslationTable createFromParcel(Parcel in) {
            return new TranslationTable(in);
        }

        @Override
        public TranslationTable[] newArray(int size) {
            return new TranslationTable[size];
        }
    };

    public String getInputStr() {
        return inputStr;
    }

    public void setInputStr(String inputStr) {
        this.inputStr = inputStr;
    }

    public String getOutputStr() {
        return outputStr;
    }

    public void setOutputStr(String outputStr) {
        this.outputStr = outputStr;
    }

    public String getInputLanguage() {
        return inputLanguage;
    }

    public void setInputLanguage(String inputLanguage) {
        this.inputLanguage = inputLanguage;
    }

    public String getOutputLanguage() {
        return outputLanguage;
    }

    public void setOutputLanguage(String outputLanguage) {
        this.outputLanguage = outputLanguage;
    }

    public String getUnique_iden() {
        return unique_iden;
    }

    public void setUnique_iden(String unique_iden) {
        this.unique_iden = unique_iden;
    }

    public String getSourceLanCode() {
        return sourceLanCode;
    }

    public void setSourceLanCode(String sourceLanCode) {
        this.sourceLanCode = sourceLanCode;
    }

    public String getDestLanCode() {
        return destLanCode;
    }

    public void setDestLanCode(String destLanCode) {
        this.destLanCode = destLanCode;
    }


    public boolean isIsfav() {
        return isfav;
    }

    public void setIsfav(boolean isfav) {
        this.isfav = isfav;
    }

    public boolean isHistorySelected() {
        return historySelected;
    }

    public void setHistorySelected(boolean historySelected) {
        this.historySelected = historySelected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(inputLanguage);
        dest.writeString(outputLanguage);
        dest.writeString(inputStr);
        dest.writeString(outputStr);
        dest.writeString(unique_iden);
        dest.writeString(destLanCode);
        dest.writeString(sourceLanCode);
        dest.writeByte((byte) (isfav ? 1 : 0));
    }

    @Override
    public void history() {

    }
}
