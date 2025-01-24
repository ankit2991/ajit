package com.messaging.textrasms.manager.feature.compose.editing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.model.Recipient
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.contact_chip.*
import kotlinx.android.synthetic.main.contact_chip.view.*
import javax.inject.Inject

class ChipsAdapter @Inject constructor() : QkAdapter<Recipient>() {

    var view: RecyclerView? = null
    val chipDeleted: PublishSubject<Recipient> = PublishSubject.create<Recipient>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.contact_chip, parent, false)
        return QkViewHolder(view).apply {

            view.cancel.setOnClickListener(View.OnClickListener {
                if (adapterPosition != -1) {
                    val chip = getItem(adapterPosition)
                    chipDeleted.onNext(chip)
                }
            })

        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val recipient = getItem(position)

        holder.avatar.setRecipientgroup(recipient)
        holder.name.text = recipient.contact?.name?.takeIf { it.isNotBlank() } ?: recipient.address
    }
}
