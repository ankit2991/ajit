package com.messaging.textrasms.manager.feature.compose.editing

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.util.extensions.dpToPx
import com.messaging.textrasms.manager.model.Recipient
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.contact_chip.*
import javax.inject.Inject

class ReceipntAdapter @Inject constructor() : QkAdapter<Recipient>() {

    var view: RecyclerView? = null
    val chipDeleted: PublishSubject<Recipient> = PublishSubject.create<Recipient>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.receipnt_chip, parent, false)
        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val chip = getItem(adapterPosition)
                showDetailedChip(view.context, chip)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val recipient = getItem(position)

        holder.avatar.setRecipientgroup(recipient)
        holder.name.text = recipient.contact?.name?.takeIf { it.isNotBlank() } ?: recipient.address
    }

    private fun showDetailedChip(context: Context, recipient: Recipient) {
        val detailedChipView = DetailedChipView(context)
        detailedChipView.setRecipient(recipient)

        val rootView = view?.rootView as ViewGroup

        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        layoutParams.topMargin = 24.dpToPx(context)
        layoutParams.marginStart = 56.dpToPx(context)

        rootView.addView(detailedChipView, layoutParams)
        detailedChipView.show()

        detailedChipView.setOnDeleteListener {
            chipDeleted.onNext(recipient)
            detailedChipView.hide()
        }
    }
}
