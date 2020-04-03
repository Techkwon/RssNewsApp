package com.example.rssnesapp.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Toast
import com.example.rssnesapp.R
import kotlinx.android.synthetic.main.activity_news_view.*

class NewsInfoActivity : AppCompatActivity() {
    private var url: String? = null
    private var keywords: Array<String>? = null

    companion object {
        const val EXTRA_NEWS_URL = "extra_news_url"
        const val EXTRA_NEWS_KEYWORDS = "extra_news_keywords"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_view)

        getIntendData()
        showKeywords()
        setViewClickListener()

        if (savedInstanceState == null) initWebView()
        else this.web_view.restoreState(savedInstanceState)
    }

    override fun onBackPressed() {
        if (this.web_view.canGoBack()) this.web_view.goBack() else super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        this.web_view.saveState(outState)
    }

    private fun getIntendData() {
        url = intent.getStringExtra(EXTRA_NEWS_URL)
        keywords = intent.getStringArrayExtra(EXTRA_NEWS_KEYWORDS)
    }

    private fun showKeywords() {
        if (keywords != null) {
            keywords?.let {
                this.tv_keyword1.text = it[0]
                this.tv_keyword2.text = it[1]
                this.tv_keyword3.text = it[2]
                this.constraint_holder_webview_keywords.visibility = View.VISIBLE
            }
        } else {
            this.constraint_holder_webview_keywords.visibility = View.GONE
        }
    }

    private fun setViewClickListener() {
        this.iv_close_webview.setOnClickListener { finish() }
        this.iv_icon_copy_url.setOnClickListener { copyUrl() }
    }

    private fun copyUrl() {
        val url = this.tv_url_status.text
        val clipboardManager = getSystemService((Context.CLIPBOARD_SERVICE)) as ClipboardManager
        val clipData = ClipData.newPlainText(getString(R.string.label_news_url), url)
        clipboardManager.setPrimaryClip(clipData)

        Toast.makeText(this, getString(R.string.text_copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        this.web_view.webViewClient = WebViewClient()
        val webViewSettings = web_view.settings

        webViewSettings.run {
            javaScriptEnabled = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = false
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            builtInZoomControls = false
            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
        }

        setProgressStatus()
        url?.let { web_view.loadUrl(it) }
    }

    private fun setProgressStatus() {
        this.progressbar_webview.max = 100

        this.web_view.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                this@NewsInfoActivity.progressbar_webview.progress = newProgress
                this@NewsInfoActivity.progressbar_webview.visibility = if (newProgress == 100) View.INVISIBLE else View.VISIBLE

                val url = view?.url
                this@NewsInfoActivity.tv_url_status.text = url
            }
        }

        this.web_view.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
            }
        }
    }
}
