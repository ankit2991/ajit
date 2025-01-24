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
import com.messaging.textrasms.manager.model.Sticker
import kotlinx.android.synthetic.main.gif_layout.view.*

class GifAdapter(
    var photos: ArrayList<Sticker.stickerurl>,
    val itemClickListener: OnItemClickListenergif
) : RecyclerView.Adapter<GifAdapter.PhotoHolder>() {

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifAdapter.PhotoHolder {
        val inflatedView = parent.inflate(R.layout.gif_layout, false)
        return PhotoHolder(inflatedView)

    }


    override fun onBindViewHolder(holder: GifAdapter.PhotoHolder, position: Int) {
        val itemPhoto = photos[position]
        holder.bindPhoto(itemPhoto.getUrl(), itemPhoto.name, itemClickListener)


    }

    private var photo: Uri? = null
    override fun getItemCount() = photos.size

    //1
    class PhotoHolder(v: View) : RecyclerView.ViewHolder(v) {
        //2
        private var view: View = v
        var photo: Uri? = null


        fun bindPhoto(photo: Uri, name: String, clickListener: OnItemClickListenergif) {

            this.photo = photo
            Glide.with(view.context).load(photo)
                .into(view.love)
            itemView.setOnClickListener {
                clickListener.onItemClickedgif(photo, name)
                logDebug("checkpath" + photo)

            }
        }

        internal var mDebugLog = true
        internal var mDebugTag = "data"

        internal fun logDebug(msg: String) {
            if (mDebugLog) Log.d(mDebugTag, msg)
        }
    }

    interface OnItemClickListenergif {
        fun onItemClickedgif(pic: Uri, name: String)
    }

}
