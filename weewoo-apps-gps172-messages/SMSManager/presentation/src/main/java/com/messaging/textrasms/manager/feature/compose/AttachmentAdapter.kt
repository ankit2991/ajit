package com.messaging.textrasms.manager.feature.compose

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.model.Attachment
import ezvcard.Ezvcard
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.attachment_contact_list_item.*
import kotlinx.android.synthetic.main.attachment_image_list_item.*
import kotlinx.android.synthetic.main.attachment_image_list_item.view.*
import javax.inject.Inject

class AttachmentAdapter @Inject constructor(
    private val context: Context
) : QkAdapter<Attachment>() {

    companion object {
        private const val VIEW_TYPE_IMAGE = 0
        private const val VIEW_TYPE_CONTACT = 1
    }

    val attachmentDeleted: Subject<Attachment> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            VIEW_TYPE_IMAGE -> inflater.inflate(R.layout.attachment_image_list_item, parent, false)
                .apply { thumbnailBounds.clipToOutline = true }

            VIEW_TYPE_CONTACT -> inflater.inflate(
                R.layout.attachment_contact_list_item,
                parent,
                false
            )

            else -> null!! // Impossible
        }

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                if (adapterPosition != -1) {
                    val attachment = getItem(adapterPosition)
                    attachmentDeleted.onNext(attachment)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val attachment = getItem(position)

        when (attachment) {
            is Attachment.Image -> {
                Glide.with(context)
                    .load(attachment.getUri())
                    .into(holder.thumbnail)
            }


            is Attachment.Contact -> Observable.just(attachment.vCard)
                .mapNotNull { vCard -> Ezvcard.parse(vCard).first() }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { vcard -> holder.name?.text = vcard.formattedName.value }

        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is Attachment.Image -> VIEW_TYPE_IMAGE
        is Attachment.Contact -> VIEW_TYPE_CONTACT
    }

}