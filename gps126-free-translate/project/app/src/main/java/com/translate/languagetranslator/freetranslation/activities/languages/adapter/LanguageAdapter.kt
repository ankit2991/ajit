package com.translate.languagetranslator.freetranslation.activities.languages.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.appUtils.isSpeakerVisible
import com.translate.languagetranslator.freetranslation.interfaces.LanguageInterface
import com.translate.languagetranslator.freetranslation.models.LanguageModel
import de.hdodenhof.circleimageview.CircleImageView

class LanguageAdapter : RecyclerView.Adapter<LanguageAdapter.ItemHolder>() {

    private var languageModelList: List<LanguageModel> = ArrayList()
    private var languageInterface: LanguageInterface? = null

    fun setAdapterInterface(languageInterface: LanguageInterface) {
        this.languageInterface = languageInterface
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val inflate = LayoutInflater.from(parent.context).inflate(R.layout.li_languages, parent,false)
        return ItemHolder(inflate)
    }

    override fun getItemCount(): Int {
        return languageModelList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val languageModel = languageModelList[position]
        holder.textViewLang.text = languageModel.languageName
        holder.ivFlag.setImageResource(languageModel.flag)
        holder.textViewLang.setBackgroundResource(0)
        holder.textViewLang.setTextColor(Color.DKGRAY)
        if (isSpeakerVisible(languageModel.supportedLangCode)) {
            holder.ivSpeaker.visibility = View.VISIBLE

        } else {
            holder.ivSpeaker.visibility = View.GONE

        }


        holder.ivSpeaker.setOnClickListener {
            languageInterface?.onSpeakerCLicked(languageModel.languageName,languageModel.supportedLangCode)
        }
        holder.rootLayout.setOnClickListener {
            languageInterface?.onLanguageSelect(
                languageModel.languageName,
                languageModel.languageCode,
                languageModel.supportedLangCode,
                languageModel.languageMeaning,
                languageModel.flag
            )

        }


    }

    fun setData(modelList: List<LanguageModel>) {
        this.languageModelList = modelList
        notifyDataSetChanged()
    }


    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var textViewLang: TextView = itemView.findViewById(R.id.tv_lang_title)
        internal var ivSpeaker: ImageView = itemView.findViewById(R.id.iv_speaker)
        internal var ivFlag: CircleImageView = itemView.findViewById(R.id.iv_flag)
        internal var rootLayout: RelativeLayout = itemView.findViewById(R.id.root_layout)


    }

}