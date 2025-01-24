package com.translate.languagetranslator.freetranslation.database.entity;


import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.translate.languagetranslator.freetranslation.appUtils.Constants;

@Keep
@Entity(tableName = Constants.TABLE_NAME_SAVED_CONVERSATION)
public class SavedChat {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "personName")
    private String personName;
    @ColumnInfo(name = "conversationList")
    private String conversationList;
    @ColumnInfo(name = "totalChat")
    private int totalChat;

    @Ignore
    public boolean chatSelected;

    public SavedChat() {
    }

    public SavedChat(@NonNull String personName, String conversationList, int totalChat) {
        this.personName = personName;
        this.conversationList = conversationList;
        this.totalChat = totalChat;
    }

    @NonNull
    public String getPersonName() {
        return personName;
    }

    public void setPersonName(@NonNull String personName) {
        this.personName = personName;
    }

    public String getConversationList() {
        return conversationList;
    }

    public void setConversationList(String conversationList) {
        this.conversationList = conversationList;
    }

    public int getTotalChat() {
        return totalChat;
    }

    public void setTotalChat(int totalChat) {
        this.totalChat = totalChat;
    }

    public boolean isChatSelected() {
        return chatSelected;
    }

    public void setChatSelected(boolean chatSelected) {
        this.chatSelected = chatSelected;
    }
}
