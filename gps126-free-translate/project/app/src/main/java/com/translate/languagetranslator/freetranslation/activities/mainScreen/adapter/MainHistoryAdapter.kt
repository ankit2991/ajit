package com.translate.languagetranslator.freetranslation.activities.mainScreen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.appUtils.getCountryFlag
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import com.translate.languagetranslator.freetranslation.interfaces.TranslationHistoryInterface

class MainHistoryAdapter() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val VIEW_TYPE_AD = 0
    private val VIEW_TYPE_ITEM = 1


    inner class HistoryItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tvInput: TextView = itemView.findViewById(R.id.tv_input_word_history)
        internal var tvTranslation: TextView =
            itemView.findViewById(R.id.tv_translated_text_history)
        internal var ivFavorite: ImageView = itemView.findViewById(R.id.iv_fav)
        internal var ivFlagFrom: ImageView = itemView.findViewById(R.id.iv_flag_from)
        internal var ivFlagTo: ImageView = itemView.findViewById(R.id.iv_flag_to)
        internal var ivDeleteItem: ImageView = itemView.findViewById(R.id.iv_delete_item)


        fun onBindData(translationModel: TranslationTable, position: Int) {
            tvInput.text = translationModel.inputStr
            tvTranslation.text = translationModel.outputStr
            if (translationModel.isfav) {
                ivFavorite.setImageResource(R.drawable.ic_star_black_24dp);
            } else {
                ivFavorite.setImageResource(R.drawable.ic_star_gray_24dp)
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


            ivFavorite.setOnClickListener {

                if (translationModel.isfav) {
                    translationModel.isfav = false
                    historyInterface?.onFavorite(translationModel)

                } else {
                    translationModel.isfav = true
                    historyInterface?.onFavorite(translationModel)

                }
            }

            ivDeleteItem.setOnClickListener {
                historyInterface?.onDelete(translationModel)
            }

            itemView.setOnClickListener {
                historyInterface?.onSelectHistory(translationModel)
            }



        }

    }

    private var historyItems: List<TranslationTable> = ArrayList()
    private var historyInterface: TranslationHistoryInterface? = null


    fun setAdapterListener(historyInterface: TranslationHistoryInterface) {
        this.historyInterface = historyInterface
    }

    override fun getItemCount(): Int {
        return historyItems.size
    }

    fun setData(historyItems: List<TranslationTable>) {
        this.historyItems = historyItems
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate =
            LayoutInflater.from(parent.context).inflate(R.layout.li_history_new, parent, false)
        return HistoryItemHolder(inflate)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val translationModel = historyItems[position]
        val historyItemHolder = holder as HistoryItemHolder
        historyItemHolder.onBindData(translationModel, position)
    }
}