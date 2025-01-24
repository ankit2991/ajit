package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityTermsConditionsBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.UrlUtils


class TermsAndConditionsActivity : BaseActivity() {


    lateinit var binding:ActivityTermsConditionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsConditionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_terms_conditions)

        if (TinyDB.getInstance(this).getBoolean("isPolicyAccepted")) {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        } else {
            setTextViewHTML(binding.tvPrivacyPolicy, getString(R.string.privacy_text))
            binding.btnGrantPermission.setOnClickListener {
                TinyDB.getInstance(this).putBoolean("isPolicyAccepted", true)
                startActivity(Intent(this, SplashActivity::class.java))
                finish()
            }
        }
    }


   /* fun showDiloage() {
        if (isFinishing)
            return
        val builder = AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        builder.setPositiveButton("OK") { dialog, which ->
            dialog.cancel()
            showPermissionDialog()
        }
        builder.setTitle("")
        builder.setMessage("This app may not work correctly without the requested permission.")
        val alertDialog = builder.create()
        if (alertDialog != null) {
            alertDialog.show()
            val buttonbackground = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            buttonbackground.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            val buttonbackground1 = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            buttonbackground1.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        }
    }*/



    protected fun setTextViewHTML(text: TextView, html: String) {
        var sequence: Spanned? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sequence = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            sequence = Html.fromHtml(html)
        }

        val strBuilder = SpannableStringBuilder(sequence)
        val urls = strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
        for (span in urls) {
            makeLinkClickable(strBuilder, span)
        }
        text.text = strBuilder
        text.movementMethod = LinkMovementMethod.getInstance()
    }


    private fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        val clickable = object : ClickableSpan() {
            override fun onClick(view: View) {
                UrlUtils.openUrl(
                    this@TermsAndConditionsActivity,
                    "https://zedlatino.info/privacy-policy-apps.html"
                )
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)
    }


}
