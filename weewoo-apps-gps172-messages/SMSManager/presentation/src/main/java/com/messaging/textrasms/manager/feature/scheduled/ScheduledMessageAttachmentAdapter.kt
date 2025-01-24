package com.messaging.textrasms.manager.feature.scheduled

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import kotlinx.android.synthetic.main.attachment_image_list_item.view.*
import kotlinx.android.synthetic.main.scheduled_message_image_list_item.*
import javax.inject.Inject

class ScheduledMessageAttachmentAdapter @Inject constructor(
    private val context: Context
) : QkAdapter<Uri>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.scheduled_message_image_list_item, parent, false)
        view.thumbnail.clipToOutline = true

        return QkViewHolder(view)
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val attachment = getItem(position)

        Glide.with(context).load(attachment).into(holder.thumbnail)
    }

}
