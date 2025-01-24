package com.translate.languagetranslator.freetranslation.activities.conversation.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.conversation.interfaces.ConversationListener
import com.translate.languagetranslator.freetranslation.appUtils.isSpeakerVisible
import com.translate.languagetranslator.freetranslation.appUtils.showToast
import com.translate.languagetranslator.freetranslation.database.entity.ConversationModel
import java.util.*

class ConversationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mFrom = 0
    private val mTo = 1
    private val mAD = 2
    private var conversationList: List<ConversationModel> = ArrayList()
    private var conversationListener: ConversationListener? = null
    private var selection = false


    fun setAdapterListener(conversationListener: ConversationListener) {
        this.conversationListener = conversationListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            mFrom -> {
                return LeftConversationHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.li_conversation_left,
                        parent,
                        false
                    )
                )
            }
            mTo -> {
                return RightConversationHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.li_conversation_right,
                        parent,
                        false
                    )
                )

            }
            else -> {
                return AdItemHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.li_conversation_right,
                        parent,
                        false
                    )
                )
            }
        }

    }

    override fun getItemCount(): Int {
        return conversationList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val conversationModel = conversationList[position]
        when {
            mFrom == getItemViewType(position) -> {
                val leftItemHolder = holder as LeftConversationHolder
                leftItemHolder.onBindData(conversationModel, position)
            }
            mTo == getItemViewType(position) -> {
                val rightItemHolder = holder as RightConversationHolder
                rightItemHolder.onBindData(conversationModel, position)

            }
            else -> {
                val adItemHolder = holder as AdItemHolder
                adItemHolder.onBindData(conversationModel, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (conversationList[position].origin) {
            "from" -> {

                mFrom
            }
            "to" -> {
                mTo
            }
            else -> {
                mAD
            }
        }
    }

    fun setConversation(conversationList: List<ConversationModel>) {
        this.conversationList = conversationList
        notifyDataSetChanged()
    }

    inner class LeftConversationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvInputWord: TextView = itemView.findViewById(R.id.tv_input_conv_left)
        var tvTranslatedWord: TextView = itemView.findViewById(R.id.tv_out_conv_left)
        var ivSpeaker: ImageView = itemView.findViewById(R.id.iv_speaker_left_conv)
        var ivCopy: ImageView = itemView.findViewById(R.id.iv_copy_left_conv)

        fun onBindData(conversationModel: ConversationModel, position: Int) {
            tvInputWord.text = conversationModel.inputWord
            tvTranslatedWord.text = conversationModel.translatedWord

            ivSpeaker.setOnClickListener {
                if (isSpeakerVisible(conversationModel.translatedWordLangCode))
                    conversationListener?.onSpeakerClicked(
                        conversationModel.translatedWord,
                        conversationModel.translatedWordLangCode
                    )
                else {
                    showToast(itemView.context, "This language does not suport text to speech")
                }
            }
            ivCopy.setOnClickListener {
                conversationListener?.onCopy(conversationModel.translatedWord)
            }
            if (conversationModel.isConversationSelected) {
                itemView.setBackgroundColor(Color.parseColor("#93C1F4"))
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))

            }

            itemView.setOnLongClickListener {
                conversationListener?.onChatLongClick(position)
                true
            }
            itemView.setOnClickListener {
                conversationListener?.onSingleClick(conversationModel, position)
            }

        }
    }

    inner class RightConversationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvInputWord: TextView = itemView.findViewById(R.id.tv_input_conv_right)
        var tvTranslatedWord: TextView = itemView.findViewById(R.id.tv_out_conv_right)
        var ivSpeaker: ImageView = itemView.findViewById(R.id.iv_speaker_right_conv)
        var ivCopy: ImageView = itemView.findViewById(R.id.iv_copy_right_conv)

        fun onBindData(conversationModel: ConversationModel, position: Int) {
            tvInputWord.text = conversationModel.inputWord
            tvTranslatedWord.text = conversationModel.translatedWord
            ivSpeaker.setOnClickListener {
                if (isSpeakerVisible(conversationModel.translatedWordLangCode))
                    conversationListener?.onSpeakerClicked(
                        conversationModel.translatedWord,
                        conversationModel.translatedWordLangCode
                    )
                else {
                    showToast(itemView.context, "This language does not suport text to speech")
                }
            }
            ivCopy.setOnClickListener {
                conversationListener?.onCopy(conversationModel.translatedWord)
            }
            if (conversationModel.isConversationSelected) {
                itemView.setBackgroundColor(Color.parseColor("#93C1F4"))
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))


            }

            itemView.setOnLongClickListener {
                conversationListener?.onChatLongClick(position)
                true
            }
            itemView.setOnClickListener {
                conversationListener?.onSingleClick(conversationModel, position)
            }

        }
    }

    inner class AdItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {



        var adMobContainer: FrameLayout = itemView.findViewById(R.id.admob_layout_conversation)
        var facebookContainer: FrameLayout = itemView.findViewById(R.id.conversation_fb_native_ad)
        var adContainerConversation: RelativeLayout = itemView.findViewById(R.id.ad_layout_conversation_item)

        fun onBindData(conversationModel: ConversationModel, position: Int) {

        }
    }


    fun setSelection(selection: Boolean) {
        this.selection = selection
        if (conversationListener != null)
            conversationListener!!.onSelectionChange(selection)
        notifyDataSetChanged()
    }

    fun isSelection(): Boolean {
        return selection
    }

    fun getAllData(): List<ConversationModel> {
        return conversationList
    }

    fun getAllSelectedItems(): ArrayList<ConversationModel> {
        val selectedObjects = ArrayList<ConversationModel>()
        if (getAllData().isNotEmpty()) {
            for (item in getAllData()) {
                if (item.isConversationSelected)
                    selectedObjects.add(item)
            }
        }
        return selectedObjects
    }

    private fun isAnyItemSelected(): Boolean {
        for (item in getAllData()) {
            if (item.isConversationSelected)
                return true
        }
        return false
    }

    fun clearSelection() {
        for (item in getAllData()) {
            item.isConversationSelected = false
        }
        setSelection(false)
    }

    fun setChecked(position: Int) {
        getAllData()[position].isConversationSelected =
            !getAllData()[position].isConversationSelected
        if (!isAnyItemSelected()) {
            setSelection(false)
        } else {
            val selectedItem = getAllSelectedItems().size
            conversationListener?.onSelectItem(true, selectedItem)
        }
        notifyItemChanged(position)
    }

    fun setCheckAll(selection: Boolean) {
        for (item in getAllData()) {
            item.isConversationSelected = selection
        }
        setSelection(selection)
    }

}