package com.translate.languagetranslator.freetranslation.activities.historyBookmark.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.historyBookmark.interfaces.HistoryBookmarkInterface
import com.translate.languagetranslator.freetranslation.appUtils.getCountryFlag
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable

class HistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var historyItems: List<TranslationTable> = ArrayList()
    private var adapterListener: HistoryBookmarkInterface? = null
    private var selection = false


    fun setListener(adapterListener: HistoryBookmarkInterface) {
        this.adapterListener = adapterListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate =
            LayoutInflater.from(parent.context).inflate(R.layout.li_history, parent, false)
        return HistoryItemHolder(inflate)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val historyModel = historyItems[position]
        val historyItemHolder = holder as HistoryItemHolder
        historyItemHolder.onBindData(historyModel, position)

    }


    fun setData(historyItems: List<TranslationTable>) {
        this.historyItems = historyItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return historyItems.size
    }

    inner class HistoryItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tvInput: TextView = itemView.findViewById(R.id.tv_history_input)
        internal var tvTranslation: TextView =
            itemView.findViewById(R.id.tv_history_output)
        internal var ivFlagFrom: ImageView = itemView.findViewById(R.id.iv_flag_from)
        internal var ivFlagTo: ImageView = itemView.findViewById(R.id.iv_flag_to)
        internal var ivFavorite: ImageView = itemView.findViewById(R.id.iv_favorite_history)
        internal var ivSelected: ImageView = itemView.findViewById(R.id.iv_history_selected)

        fun onBindData(translationModel: TranslationTable, position: Int) {
            tvInput.text = translationModel.inputStr
            tvTranslation.text = translationModel.outputStr
            val isFavorite = translationModel.isIsfav

            if (isFavorite) {
                ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
            } else {
                ivFavorite.setImageResource(R.drawable.ic_favorite_empty)

            }

            ivFlagTo.setImageDrawable(
                getCountryFlag(
                    itemView.context,
                    translationModel.getOutputLanguage()
                )
            )
            ivFlagFrom.setImageDrawable(
                getCountryFlag(
                    itemView.context,
                    translationModel.getInputLanguage()
                )
            )

            itemView.setOnClickListener {
                adapterListener?.onSelectItem(translationModel, position)
            }
            ivFavorite.setOnClickListener {
                if (translationModel.isfav) {
                    translationModel.isfav = false
                    adapterListener?.onFavorite(translationModel)

                } else {
                    translationModel.isfav = true
                    adapterListener?.onFavorite(translationModel)

                }
            }

            if (translationModel.isHistorySelected) {
                itemView.background =
                    itemView.context.resources.getDrawable(R.drawable.bg_history_selected)
                ivSelected.visibility = View.VISIBLE
                ivFavorite.visibility = View.GONE
            } else {
                itemView.background =
                    itemView.context.resources.getDrawable(R.drawable.bg_history_un_selected)
                ivSelected.visibility = View.GONE
                ivFavorite.visibility = View.VISIBLE

            }

            itemView.setOnLongClickListener {

                adapterListener?.onLongClick(position)
                true
            }
        }
    }

    fun setSelection(selection: Boolean) {
        this.selection = selection
        if (adapterListener != null)
            adapterListener!!.onSelectionChange(selection)
        notifyDataSetChanged()
    }

    fun isSelection(): Boolean {
        return selection
    }

    fun getAllData(): List<TranslationTable> {
        return historyItems
    }

    fun getAllSelectedItems(): ArrayList<TranslationTable> {
        val selectedObjects = ArrayList<TranslationTable>()
        if (getAllData().isNotEmpty()) {
            for (item in getAllData()) {
                if (item.isHistorySelected)
                    selectedObjects.add(item)
            }
        }
        return selectedObjects
    }

    private fun isAnyItemSelected(): Boolean {
        for (item in getAllData()) {
            if (item.isHistorySelected)
                return true
        }
        return false
    }

    fun clearSelection() {
        for (item in getAllData()) {
            item.isHistorySelected = false
        }
        setSelection(false)
    }

    fun setChecked(position: Int) {
        getAllData()[position].isHistorySelected = !getAllData()[position].isHistorySelected
        if (!isAnyItemSelected()) {
            setSelection(false)
        } else {
            val selectedItem = getAllSelectedItems().size
            adapterListener?.onSelectItem(true, selectedItem)
        }
        notifyItemChanged(position)
    }

    fun setCheckAll(selection: Boolean) {
        for (item in getAllData()) {
            item.isHistorySelected = selection
        }
        setSelection(selection)
    }
}