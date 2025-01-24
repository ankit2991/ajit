package com.translate.languagetranslator.freetranslation.interfaces;

import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable;

import java.util.ArrayList;

public interface RoomDbTransactionCallback {

    void inflateUI(ArrayList<TranslationTable> translationTables);

}
