package com.translate.languagetranslator.freetranslation.activities.conversation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.conversation.interfaces.ChatSelectionListener
import com.translate.languagetranslator.freetranslation.database.entity.SavedChat


class SavedChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataList: List<SavedChat> = ArrayList()
    private var chatListener: ChatSelectionListener? = null
    private var selection = false

    fun setAdapterListener(chatSelectionListener: ChatSelectionListener) {
        this.chatListener = chatSelectionListener
    }

    inner class PersonalChatItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tvChatName: AppCompatTextView = itemView.findViewById(R.id.tv_label_chat_name)
        internal var tvTotalChats: AppCompatTextView =
            itemView.findViewById(R.id.tv_label_total_saved)
        internal var ivDelete: AppCompatImageView = itemView.findViewById(R.id.iv_delete_chat)
        internal var ivSelected: AppCompatImageView = itemView.findViewById(R.id.iv_selected_chat)
        internal var cardItem: ConstraintLayout =
            itemView.findViewById(R.id.card_container_personal)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.li_saved_chat, parent, false)
        return PersonalChatItemHolder(inflate)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatModel = dataList[position]
        val itemHolder = holder as PersonalChatItemHolder
        itemHolder.tvChatName.text = chatModel.personName
        itemHolder.tvTotalChats.text =
            "${itemHolder.itemView.context.getString(R.string.label_total_chats)}: ${chatModel.totalChat}"

        itemHolder.ivDelete.setOnClickListener {
            chatListener?.onClickDelete(chatModel.personName)
        }
        itemHolder.itemView.setOnClickListener {
            chatListener?.onSingleClick(chatModel, position)
        }
        itemHolder.itemView.setOnLongClickListener {
            chatListener?.onChatLongClick(position)
            true
        }
        if (chatModel.isChatSelected) {
            itemHolder.ivDelete.visibility = View.GONE
            itemHolder.ivSelected.visibility = View.VISIBLE
            itemHolder.cardItem.background =
                itemHolder.itemView.context.getDrawable(R.drawable.chat_card_selected)
        } else {
            if (isAnyItemSelected()) {
                itemHolder.ivDelete.visibility = View.GONE

            } else {
                itemHolder.ivDelete.visibility = View.VISIBLE

            }
            itemHolder.ivSelected.visibility = View.GONE
            itemHolder.cardItem.background =
                itemHolder.itemView.context.getDrawable(R.drawable.chat_card_unselected)

        }


    }

    fun setData(data: List<SavedChat>) {
        this.dataList = data
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setSelection(selection: Boolean) {
        this.selection = selection
        if (chatListener != null)
            chatListener!!.onSelectionChange(selection)
        notifyDataSetChanged()
    }

    fun isSelection(): Boolean {
        return selection
    }

    fun getAllData(): List<SavedChat> {
        return dataList
    }

    fun getAllSelectedItems(): ArrayList<SavedChat> {
        val selectedObjects = ArrayList<SavedChat>()
        if (getAllData().isNotEmpty()) {
            for (item in getAllData()) {
                if (item.isChatSelected)
                    selectedObjects.add(item)
            }
        }
        return selectedObjects
    }

    private fun isAnyItemSelected(): Boolean {
        for (item in getAllData()) {
            if (item.isChatSelected)
                return true
        }
        return false
    }

    fun clearSelection() {
        for (item in getAllData()) {
            item.isChatSelected = false
        }
        setSelection(false)
    }

    fun setChecked(position: Int) {
        getAllData()[position].isChatSelected = !getAllData()[position].isChatSelected
        if (!isAnyItemSelected()) {
            setSelection(false)
        } else {
            val selectedItem = getAllSelectedItems().size
            chatListener?.onSelectItem(true, selectedItem)
        }
        notifyItemChanged(position)
    }

    fun setCheckAll(selection: Boolean) {
        for (item in getAllData()) {
            item.isChatSelected = selection
        }
        setSelection(selection)
    }

}