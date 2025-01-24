package com.translate.languagetranslator.freetranslation.activities.conversation.interfaces;


import com.translate.languagetranslator.freetranslation.database.entity.SavedChat;

public interface ChatSelectionListener {
    void onSingleClick(SavedChat model, int position);

    void onChatLongClick(int pos);

    void onSelectionChange(boolean selection);

    void onClickDelete(String chatName);
    void onSelectItem(boolean selection, int selectedItem);

}
