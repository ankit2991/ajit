package com.translate.languagetranslator.freetranslation.activities.conversation.interfaces;

import com.translate.languagetranslator.freetranslation.database.entity.ConversationModel;

public interface ConversationListener {
    void onSpeakerClicked(String word, String langCode);
    void onCopy(String word);
    void onChatLongClick(int pos);
    void onSingleClick(ConversationModel model, int position);
    void onSelectionChange(boolean selection);
    void onSelectItem(boolean selection, int selectedItem);
}
