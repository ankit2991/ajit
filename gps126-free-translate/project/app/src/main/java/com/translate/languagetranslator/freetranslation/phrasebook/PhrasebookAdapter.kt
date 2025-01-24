package com.translate.languagetranslator.freetranslation.phrasebook

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.interfaces.PhraseitemCallback


class PhrasebookAdapter(
    var context: Context,
    var list: ArrayList<PhareseModel>,
    var phraseitemCallback: PhraseitemCallback
) :
    RecyclerView.Adapter<PhrasebookAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.phrasebook_item, parent, false)
        return ViewHolder(v)
    }


    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var textView: TextView

        init {
            imageView = itemView.findViewById(R.id.imageView)
            textView = itemView.findViewById(R.id.textView)

        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item: PhareseModel = list.get(position)
        holder.imageView.setImageResource(item.icon)
        holder.textView.setText(item.name)


        holder.itemView.setOnClickListener {
            phraseitemCallback.onItemClick(item.name)
        }
    }
}