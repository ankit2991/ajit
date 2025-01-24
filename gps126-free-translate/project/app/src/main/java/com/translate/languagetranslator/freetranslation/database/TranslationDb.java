package com.translate.languagetranslator.freetranslation.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.translate.languagetranslator.freetranslation.database.dao.ConversationDao;
import com.translate.languagetranslator.freetranslation.database.dao.SavedChatDao;
import com.translate.languagetranslator.freetranslation.database.dao.TranslationTblDao;
import com.translate.languagetranslator.freetranslation.database.entity.ConversationModel;
import com.translate.languagetranslator.freetranslation.database.entity.SavedChat;
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable;

@Database(entities = {TranslationTable.class, /*ChatMessage.class,*/ ConversationModel.class, SavedChat.class}, version = 7)
public abstract class TranslationDb extends RoomDatabase {

    public static TranslationDb dataBase;

    public abstract TranslationTblDao translationTblDao();

    public abstract ConversationDao conversationDao();

    public abstract SavedChatDao savedChatDao();

    public static TranslationDb getInstance(Context context) {
        if (null == dataBase) {
            dataBase = buildDatabaseInstance(context);
        }
        return dataBase;
    }

    private static TranslationDb buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context,
                TranslationDb.class,
                roomConstants.roomDbName)
//                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_3_6, MIGRATION_4_6, MIGRATION_5_6, MIGRATION_6_7)
                .allowMainThreadQueries().build();
    }


    static final Migration MIGRATION_3_6 = new Migration(3, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ConversationModel` (`speaking` INTEGER NOT NULL," +
                    "`inputWord` TEXT, "
                    + "`translatedWord` TEXT," +
                    "`origin` TEXT," +
                    "`translatedWordLangCode` TEXT," +
                    "`translatedWordLangName` TEXT," +
                    "`translatedWordCountryCode` TEXT," +
                    "`inputWordLangCode` TEXT," +
                    "`inputWordLangName` TEXT," +
                    "`id` INTEGER NOT NULL," +
                    "`inputWordCountryCode` TEXT," +
                    "`groupPair` TEXT," +
                    "PRIMARY KEY(`id`))");

            database.execSQL("ALTER TABLE `TranslationTable` ADD COLUMN `unique_iden` TEXT");
        }
    };

    static final Migration MIGRATION_4_6 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

            database.execSQL("DROP TABLE ChatMessage");
            database.execSQL("ALTER TABLE `TranslationTable` ADD COLUMN `unique_iden` TEXT");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

            database.execSQL("ALTER TABLE `TranslationTable` ADD COLUMN `unique_iden` TEXT");
        }
    };
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {


            database.execSQL("CREATE TABLE IF NOT EXISTS `savedConversation` (`personName` TEXT NOT NULL, "
                    + "`conversationList` TEXT,`totalChat` INTEGER NOT NULL," +
                    " PRIMARY KEY(`personName`))");
        }
    };


}
