package com.gpsnavigation.maps.gpsroutefinder.routemap.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.callbacks.OnLanguageClicked
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.LocaleLanguageModel
import org.jetbrains.anko.find

class LocalizationAdapter(
    private val languagesList: ArrayList<LocaleLanguageModel>,
    var lastSelectedPosition: Int
) : RecyclerView.Adapter<LocalizationAdapter.ViewHolder>() {
    //    private var lastSelectedPosition = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_language, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val languageModel = languagesList[position]
        holder.offerName.text = languageModel.name
        holder.selectionState.isChecked = lastSelectedPosition == position
    }

    override fun getItemCount(): Int {
        return languagesList.size
    }

    var onLanguageClicked: OnLanguageClicked? = null
    fun setOnLanguageSelected(onLanguageClicked: OnLanguageClicked) {
        this.onLanguageClicked = onLanguageClicked
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var offerName: TextView = view.findViewById(R.id.tvLanguageName)
        var selectionState: RadioButton = view.findViewById(R.id.radioLangSelecter)

        init {
            view.setOnClickListener {
                if (adapterPosition != lastSelectedPosition) {
                    lastSelectedPosition = adapterPosition
                    onLanguageClicked?.onLanguageSelected(languagesList[lastSelectedPosition])
                    notifyDataSetChanged()
                }
            }
            selectionState.setOnClickListener {
                if (adapterPosition != lastSelectedPosition) {
                    lastSelectedPosition = adapterPosition
                    onLanguageClicked?.onLanguageSelected(languagesList[lastSelectedPosition])
                    notifyDataSetChanged()
                }
            }
        }
    }

}