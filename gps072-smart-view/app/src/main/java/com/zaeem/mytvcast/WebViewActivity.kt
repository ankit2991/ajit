package com.zaeem.mytvcast;

import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import android.view.View
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar


class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_webview)
        initToolbar()
        loadHtmlContent()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = intent.getStringExtra(TITLE_ARG)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    private fun loadHtmlContent() {
        val privacyPolicyWV = findViewById<View>(R.id.webview) as WebView
        privacyPolicyWV.loadUrl(intent.getStringExtra(URI_TAG)!!)
    }

    companion object {
        private const val TITLE_ARG = "title"
        private const val URI_TAG = "uri"
        @JvmStatic
        fun getIntent(context: Context?, title: String?, url: String?): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(TITLE_ARG, title)
            intent.putExtra(URI_TAG, url)
            return intent
        }
    }
}