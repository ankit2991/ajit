package com.messaging.textrasms.manager.feature.fragments


import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.messgeData.TabletOptimizedBottomSheetDialogFragment
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import java.util.*


class LinkLongClickFragment : TabletOptimizedBottomSheetDialogFragment() {

    private var link: String? = null
    private var mainColor: Int = 0
    var from: Boolean = false
    private var accentColor: Int = 0
    fun setvalue(from1: Boolean) {
        from = from1
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //No call for super().
    }

    companion object {


        fun newInstance(): LinkLongClickFragment {
            return LinkLongClickFragment()
        }

        fun newInstance(from: Boolean): LinkLongClickFragment {
            return LinkLongClickFragment()

        }
    }


    override fun createLayout(inflater: LayoutInflater): View {
        val contentView = View.inflate(context, R.layout.bottom_tap, null)

        val copy = contentView.findViewById<View>(R.id.copy)
        val cancel = contentView.findViewById<View>(R.id.cancel)
        var open_external = contentView.findViewById<View>(R.id.open_external) as LinearLayout
        open_external.setVisible(from)
        open_external.setOnClickListener {
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(mainColor)
            builder.setShowTitle(true)
            builder.setActionButton(
                BitmapFactory.decodeResource(resources, R.drawable.ic_share),
                getString(R.string.Share), getShareIntent(link), true
            )
            val customTabsIntent = builder.build()

            try {
                customTabsIntent.launchUrl(activity!!, Uri.parse(link))
            } catch (e: Exception) {
            }

            dismiss()
        }

        cancel.setOnClickListener {


            dismiss()
        }



        copy.setOnClickListener {
            val activity = activity ?: return@setOnClickListener

            val clipboard = activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("msg", link)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                activity, com.messaging.textrasms.manager.data.R.string.text_copy,
                Toast.LENGTH_SHORT
            ).show()

            dismiss()
        }

        return contentView
    }

    fun setLink(from: Boolean, link: String) {
        this.link = link


    }

    private fun getShareIntent(url: String?): PendingIntent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_TEXT, url)
        shareIntent.type = "text/plain"
        return PendingIntent.getActivity(
            activity,
            Random().nextInt(Integer.MAX_VALUE),
            shareIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }


}
