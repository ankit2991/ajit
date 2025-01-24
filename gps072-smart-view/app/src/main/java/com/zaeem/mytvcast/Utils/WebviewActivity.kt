package com.zaeem.mytvcast.Utils

import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ironsource.mediationsdk.IronSource
import com.zaeem.mytvcast.databinding.WebviewActivityBinding

class WebviewActivity : AppCompatActivity() {


    companion object {

        const val FACEBOOK = "https://www.facebook.com/"
        const val TED = "https://www.ted.com/"
        const val GOOGLE = "https://www.google.com/search?q="


        fun openWebsite(query: String, context: AppCompatActivity) {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("query", query)
            context.startActivity(intent)

        }

    }




    private lateinit var binding: WebviewActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = WebviewActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.apply {

            webview.webViewClient = WebViewClient()
            webview.webChromeClient = WebChromeClient()
            webview.settings.javaScriptEnabled = true
            intent.getStringExtra("query")?.let {

                when (it) {
                    FACEBOOK -> {
                        webview.loadUrl(FACEBOOK)
                    }
                    TED -> {
                        webview.loadUrl(TED)
                    }
                    else -> webview.loadUrl(GOOGLE + it)
                }
            }

            castIV.setOnClickListener {

                startCast()
            }
            backIV.setOnClickListener {
                onBackPressed()
            }

        }

        AdsManager.showInter(this,{},{})


    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }
    private fun startCast() {

        try {
            startActivity(Intent("android.settings.CAST_SETTINGS"))
        } catch (exception1: Exception) {
            Toast.makeText(applicationContext, "Device not supported", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {

        if (binding.webview.canGoBack()) {
            binding.webview.goBack()
        } else
            finish()
    }
}