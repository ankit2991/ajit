package com.translate.languagetranslator.freetranslation.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.translate.languagetranslator.freetranslation.database.entity.SavedChat;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface SavedChatDao {
    @Transaction
    @Insert(onConflict = REPLACE)
    void insert(SavedChat savedChat);

    @Transaction
    @Update
    void update(SavedChat savedChat);

    @Transaction
    @Delete
    void delete(SavedChat savedChat);

    @Transaction
    @Query("SELECT * FROM savedConversation ")
    LiveData<List<SavedChat>> getAll();

    @Transaction
    @Query("DELETE FROM savedConversation")
    void deleteAll();


    @Transaction
    @Query("DELETE FROM savedConversation WHERE personName LIKE :name")
    void deleteByName(String name);
}
