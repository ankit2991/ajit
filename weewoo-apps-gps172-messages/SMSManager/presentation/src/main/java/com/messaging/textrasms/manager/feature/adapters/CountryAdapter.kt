package com.messaging.textrasms.manager.feature.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.feature.models.Country


class CountryAdapter(val context: Context,private val countries: List<Country>, private val onItemClick:(item: String)->Unit) :
    RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_languages_home, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = countries[position]
        holder.bind(country,position)


    }

    override fun getItemCount(): Int {
        return countries.size
    }

    inner class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvLanguage)


        fun bind(country: Country,position: Int) {
//            flagImageView.setImageResource(country.flagResourceId)
            nameTextView.text = country.name

            if (position == 0) {
                // Make the text bold for the first item
                val boldTypeface = ResourcesCompat.getFont(context, R.font.gilroy_bold)
                nameTextView.typeface = boldTypeface
            } else {
                // Reset the text style for other items
                val boldTypeface = ResourcesCompat.getFont(context, R.font.gilroy_medium)
                nameTextView.typeface = boldTypeface
            }
            itemView.setOnClickListener {

                onItemClick(country.code)
            }
        }
    }
}
