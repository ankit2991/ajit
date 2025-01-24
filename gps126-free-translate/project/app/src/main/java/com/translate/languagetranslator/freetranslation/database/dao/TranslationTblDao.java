package com.translate.languagetranslator.freetranslation.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable;

import java.util.List;

@Dao
public interface TranslationTblDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TranslationTable translationTable);

    @Transaction
    @Delete
    void delete(TranslationTable translationTable);

    @Query("select * from TranslationTable")
    List<TranslationTable> getAllTranslations();

    @Query("update TranslationTable set isfav=:isFav where id=:id")
    void updateFAv(boolean isFav, int id);


    @Query("select * from TranslationTable where isfav like:tru")
    List<TranslationTable> getAllFavItems(boolean tru);

    @Query("delete  from TranslationTable")
    void deleteAllHistory();

    @Query("delete  from TranslationTable where isfav like:isFav")
    void deleteAllFav(boolean isFav);

    @Delete
    void delete(List<TranslationTable> translationTables);


    @Query("select * from TranslationTable")
    LiveData<List<TranslationTable>> getAll();


    @Transaction
    @Query("SELECT * FROM TranslationTable ORDER BY id DESC LIMIT :limit  ")
    LiveData<List<TranslationTable>> getLimitList(int limit);

    @Query("select * from TranslationTable where isfav like:tru")
    LiveData<List<TranslationTable>> getFavorite(boolean tru);

    @Query("DELETE FROM TranslationTable")
    void deleteAll();

    @Transaction
    @Query("SELECT isfav FROM TranslationTable WHERE unique_iden LIKE :id ")
    boolean isFavorite(String id);

    @Query("update TranslationTable set isfav=:isFav where unique_iden=:unique_iden")
    void setFavorite(boolean isFav, String unique_iden);

}
