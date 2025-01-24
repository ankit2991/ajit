package com.translate.languagetranslator.freetranslation.interfaces;

import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable;

public interface TranslationHistoryInterface {
    void onFavorite(TranslationTable translationTable);
    void onSelectHistory(TranslationTable translationTable);
    void onDelete(TranslationTable translationTable);
}
