package com.gpsnavigation.maps.gpsroutefinder.routemap.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ItemMainScreenBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.MainScreenItemModel

class MainScreenAdapter(
    private val items: List<MainScreenItemModel>,
    private var clickListener: (MainScreenItemModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    inner class ItemsViewTwoColumnsHolder(var binding: ItemMainScreenBinding) :
        RecyclerView.ViewHolder(binding.root)
    inner class ItemsViewThreeColumnsHolder(var binding: ItemMainScreenBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemMainScreenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return when (viewType) {
            TWO_COLUMNS -> {
                ItemsViewTwoColumnsHolder(binding)
            }
            else -> {
                ItemsViewThreeColumnsHolder(binding)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MainScreenAdapter.ItemsViewTwoColumnsHolder -> {
                val context =  holder.binding.root.context
                getItem(position).also { item ->
                    holder.binding.apply {
                        title.text = item.title
                        icon.setImageResource(item.icon)
                        card.background = item.colorDrawable

                        title.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.resources.getDimension(R.dimen.big_card_text_size))
                        title.layoutParams.height = getItemWidth(context, 2) / 5
                        card.layoutParams.height = getItemWidth(context, 2) * 3 / 4

                        root.setOnClickListener {
                            clickListener(item)
                        }
                    }
                }
            }
            is MainScreenAdapter.ItemsViewThreeColumnsHolder -> {
                val context =  holder.binding.root.context
                getItem(position).also { item ->
                    holder.binding.apply {
                        title.text = item.title
                        icon.setImageResource(item.icon)
                        card.background = item.colorDrawable

                        title.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.resources.getDimension(R.dimen.small_card_text_size))
                        title.layoutParams.height = getItemWidth(context, 3) * 4 / 21
                        card.layoutParams.height = getItemWidth(context, 3) * 19 / 21

                        root.setOnClickListener {
                            clickListener(item)
                        }
                    }
                }
            }
        }
    }

    private fun getItemWidth(context: Context, columnsCount: Int) =
        with(context) {
            (resources.displayMetrics.widthPixels - (resources.getDimensionPixelSize(R.dimen._10sdp) * (columnsCount + 1))) / columnsCount
        }
    private fun getItem(position: Int): MainScreenItemModel = items[position]

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < 2) TWO_COLUMNS
        else THREE_COLUMNS
    }
    override fun getItemId(position: Int): Long {
        return items[position].id
    }

    companion object {
        private const val TWO_COLUMNS = 0
        private const val THREE_COLUMNS = 1
    }
}

