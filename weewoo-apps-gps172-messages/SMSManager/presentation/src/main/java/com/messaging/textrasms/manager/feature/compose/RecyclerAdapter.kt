package com.messaging.textrasms.manager.feature.compose

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.messaging.textrasms.manager.R
import kotlinx.android.synthetic.main.recent_images_layout.view.*

class RecyclerAdapter(
    private val photos: ArrayList<Uri>,
    val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerAdapter.PhotoHolder>() {

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.PhotoHolder {
        val inflatedView = parent.inflate(R.layout.recent_images_layout, false)
        return PhotoHolder(inflatedView)
    }


    override fun onBindViewHolder(holder: RecyclerAdapter.PhotoHolder, position: Int) {
        val itemPhoto = photos[position]
        holder.bindPhoto(itemPhoto, itemClickListener)


    }

    private var photo: String? = null
    override fun getItemCount() = photos.size

    class PhotoHolder(v: View) : RecyclerView.ViewHolder(v) {

        private var view: View = v
        var photo: Uri? = null

        companion object {
            //5
            private val PHOTO_KEY = "PHOTO"
        }

        fun bindPhoto(photo: Uri, clickListener: OnItemClickListener) {

            this.photo = photo
            view.itemImage.clipToOutline = true
            logDebug("checkpath" + photo)
            Glide.with(view.context).load(photo).into(view.itemImage)
            itemView.setOnClickListener {
                clickListener.onItemClicked(photo)

            }
        }

        internal var mDebugLog = true
        internal var mDebugTag = "IabHelper"

        internal fun logDebug(msg: String) {
            if (mDebugLog) Log.d(mDebugTag, msg)
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(pic: Uri)
    }


}
