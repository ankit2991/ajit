package com.translate.languagetranslator.freetranslation.activities.dictionary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.translate.languagetranslator.freetranslation.R

class SynonymsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var dataList: List<String> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate =
            LayoutInflater.from(parent.context).inflate(R.layout.li_synonyms, parent, false)
        return SynonymsItemHolder(inflate)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val synonym = dataList[position]
        val synonymItemHolder = holder as SynonymsItemHolder
        synonymItemHolder.tvSynonym.text = synonym

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setData(dataList: List<String>) {
        this.dataList = dataList
        notifyDataSetChanged()

    }

    inner class SynonymsItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvSynonym: TextView = itemView.findViewById(R.id.tv_synonyms_item)


    }
}