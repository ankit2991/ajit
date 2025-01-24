package com.translate.languagetranslator.freetranslation.activities.historyBookmark.interfaces;

import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable;

public interface HistoryBookmarkInterface {
    void onLongClick(int position);

    void onSelectItem(TranslationTable translationTable, int position);

    void onDelete(TranslationTable translationTable);

    void onFavorite(TranslationTable translationTable);

    void onSelectionChange(boolean selection);

    void onSelectItem(boolean selection, int selectedItem);
}
