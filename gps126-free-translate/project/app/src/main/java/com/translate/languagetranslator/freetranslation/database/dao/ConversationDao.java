package com.translate.languagetranslator.freetranslation.database.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.translate.languagetranslator.freetranslation.database.entity.ConversationModel;

import java.util.List;

@Dao
public interface ConversationDao {

    @Transaction
    @Insert
    void insertAll(List<ConversationModel> conversationList);

    @Transaction
    @Insert
    void insert(ConversationModel conversationModel);

    @Transaction
    @Update
    void update(ConversationModel conversationModel);

    @Transaction
    @Delete
    void delete(ConversationModel conversationModel);

    @Transaction
    @Query("SELECT * FROM ConversationModel ")
    LiveData<List<ConversationModel>> getAll();

    @Transaction
    @Query("DELETE FROM ConversationModel")
    void deleteAll();

    @Transaction
    @Query("DELETE FROM ConversationModel WHERE id LIKE :id")
    void deleteById(int id);

}
