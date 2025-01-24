package com.translate.languagetranslator.freetranslation.activities.conversation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.conversation.adapter.SavedChatAdapter
import com.translate.languagetranslator.freetranslation.activities.conversation.interfaces.ChatSelectionListener
import com.translate.languagetranslator.freetranslation.activities.conversation.viewModel.SavedChatViewModel
import com.translate.languagetranslator.freetranslation.appUtils.Constants.CONVERSATION_ORIGIN_LIST
import com.translate.languagetranslator.freetranslation.database.entity.SavedChat
import kotlinx.android.synthetic.main.activity_saved_chat.*
import kotlinx.android.synthetic.main.layout_toolbar_saved_chat.*

class SavedChatActivity : AppCompatActivity(), ChatSelectionListener, View.OnClickListener {

    val viewModel: SavedChatViewModel by lazy {
        ViewModelProviders.of(this).get(SavedChatViewModel::class.java)
    }

    private var personalChatAdapter: SavedChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_chat)
        getSavedChat()
        setClickListeners()
    }

    private fun getSavedChat() {
        personalChatAdapter = SavedChatAdapter()
        personalChatAdapter?.setAdapterListener(this)
        val mLayoutManager = LinearLayoutManager(this)
        rv_saved_chat.layoutManager = mLayoutManager
        rv_saved_chat.adapter = personalChatAdapter


        viewModel.getSavedPersonalChat().observe(this, { data ->
            if (!data.isNullOrEmpty()) {
                personalChatAdapter?.setData(data)
                rv_saved_chat.visibility = View.VISIBLE
                tv_no_saved_chat.visibility = View.GONE
            } else {
                rv_saved_chat.visibility = View.GONE
                tv_no_saved_chat.visibility = View.VISIBLE
            }

        })
    }

    private fun setClickListeners() {
        iv_back_toolbar_chat.setOnClickListener(this)
        iv_clear_selected_chats.setOnClickListener(this)
        iv_delete_all_saved_chat.setOnClickListener(this)
        iv_delete_selected_chats.setOnClickListener(this)
    }

    override fun onSingleClick(model: SavedChat?, position: Int) {
        personalChatAdapter?.let { adapter ->
            if (!adapter.isSelection()) {
                // launch conversation screen
                val chatName = model!!.personName
                val chatList = model.conversationList

                viewModel.starConversationActivity(
                    this,
                    CONVERSATION_ORIGIN_LIST,
                    chatName,
                    chatList
                )
            } else {
                adapter.setChecked(position)
            }
        }
    }

    override fun onChatLongClick(pos: Int) {
        personalChatAdapter?.let { adapter ->
            if (!adapter.isSelection())
                adapter.setSelection(true)
            adapter.setChecked(pos)

        }

    }

    override fun onSelectionChange(selection: Boolean) {
        if (selection) {
            toolbar_saved_first.visibility = View.GONE
            toolbar_saved_second.visibility = View.VISIBLE
        } else {
            toolbar_saved_first.visibility = View.VISIBLE
            toolbar_saved_second.visibility = View.GONE
        }

    }

    override fun onClickDelete(chatName: String?) {
        viewModel.deleteChatByName(chatName!!)
    }

    override fun onSelectItem(selection: Boolean, selectedItem: Int) {
        if (selection) {
            toolbar_saved_first.visibility = View.GONE
            toolbar_saved_second.visibility = View.VISIBLE
            val count = personalChatAdapter!!.getAllSelectedItems().size
            tv_label_selected_chat_count.text = "$count ${getString(R.string.label_selected_total)}"
        } else {
            toolbar_saved_first.visibility = View.VISIBLE
            toolbar_saved_second.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        v?.let { view ->
            when (view.id) {
                R.id.iv_back_toolbar_chat -> {
                    onBackPressed()
                }
                R.id.iv_clear_selected_chats -> {
                    personalChatAdapter?.clearSelection()
                }
                R.id.iv_delete_all_saved_chat -> {
                    deleteAllChat()
                }
                R.id.iv_delete_selected_chats -> {
                    deleteSelectedChats()
                }
                else -> {

                }
            }
        }
    }

    private fun deleteSelectedChats() {
        personalChatAdapter?.let { adapter ->
            val selectedChat = adapter.getAllSelectedItems()
            for (chat in selectedChat) {
                val name = chat.personName
                viewModel.deleteChatByName(name)
            }
            adapter.clearSelection()
        }

    }

    private fun deleteAllChat() {
        personalChatAdapter?.let { adapter ->
            if (adapter.getAllData().size > 0) {
                val savedChat = adapter.getAllData()
                for (chat in savedChat) {
                    val name = chat.personName
                    viewModel.deleteChatByName(name)
                }
            }
        }

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.transit_none, R.anim.transit_top_bottom)

    }




}