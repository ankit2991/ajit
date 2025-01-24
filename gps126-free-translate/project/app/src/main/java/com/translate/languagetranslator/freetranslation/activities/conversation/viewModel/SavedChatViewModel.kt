package com.translate.languagetranslator.freetranslation.activities.conversation.viewModel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.conversation.ConversationActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.database.entity.SavedChat

class SavedChatViewModel(application: Application) : AndroidViewModel(application) {
    private var appDataBase: TranslationDb? = null
    private var context: Context? = null

    init {
        this.context = application.applicationContext
        this.appDataBase = TranslationDb.getInstance(context)
    }

    fun getSavedPersonalChat(): LiveData<List<SavedChat>> {
        return appDataBase?.savedChatDao()?.all!!
    }

    fun deleteChatByName(name: String) {
        appDataBase?.savedChatDao()?.deleteByName(name)
    }

    fun starConversationActivity(
        activity: Activity,
        origin: String,
        chatName: String,
        conversationList: String
    ) {
        val intent = Intent(activity, ConversationActivity::class.java)
        intent.putExtra(Constants.INTENT_KEY_CONVERSATION_ORIGIN, origin)
        intent.putExtra(Constants.INTENT_PARAM_CONVERSATION_NAME, chatName)
        intent.putExtra(Constants.INTENT_PARAM_CONVERSATION_LIST, conversationList)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.transit_right_left, R.anim.transit_none)


    }
}