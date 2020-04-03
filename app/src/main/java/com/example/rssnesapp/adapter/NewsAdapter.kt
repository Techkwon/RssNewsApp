package com.example.rssnesapp.adapter

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rssnesapp.R
import com.example.rssnesapp.data.NewsDescription
import com.example.rssnesapp.data.NewsItem
import com.example.rssnesapp.ui.NewsInfoActivity
import com.example.rssnesapp.ui.NewsInfoActivity.Companion.EXTRA_NEWS_KEYWORDS
import com.example.rssnesapp.ui.NewsInfoActivity.Companion.EXTRA_NEWS_URL
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_news_list.view.*
import kotlin.collections.ArrayList

class NewsAdapter(private val activity: Activity): RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    private val newsList = ArrayList<NewsItem>()
    private val urls = HashMap<String, String>()
    private val descriptions = HashMap<String, NewsDescription?>()
    private var lastClickTime: Long = 0

    companion object {
        private const val CLICK_INTERVAL = 600
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_news_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = newsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateNewsItem(newsList[position], holder)
        updateDescription(holder, newsList[position].guid)
        updateThumbnails(urls, holder, newsList[position].guid)

        val keywords = descriptions[newsList[position].guid]?.keywords
        holder.constraintNews.setOnClickListener { moveToNewsView(newsList[position].link, keywords) }
    }

    private fun updateNewsItem(newsItem: NewsItem, holder: ViewHolder) {
        with(holder) {
            val source = newsItem.source
            val title = newsItem.title
                .replaceAfterLast("-", "")
                .substringBeforeLast("-")

            tvNewsSource.text = source
            tvNewsTitle.text = title
        }
    }

    private fun updateThumbnails(urls: HashMap<String, String>, holder: ViewHolder, guid: String) {
        if (urls.isNotEmpty()) {
            Glide.with(activity).load(urls[guid]).error(R.drawable.ic_error_thumbnail).into(holder.ivThumbnail)
        } else {
            Glide.with(activity).load(R.drawable.ic_error_thumbnail).into(holder.ivThumbnail)
        }
    }

    private fun updateDescription(holder: ViewHolder, guid: String) {
        val description = descriptions[guid]

        with(holder) {
            if (description != null) {
                tvAbstract.text = description.abstract
                tvKeyword1.text = description.keywords[0]; tvKeyword1.visibility = View.VISIBLE
                tvKeyword2.text = description.keywords[1]; tvKeyword2.visibility = View.VISIBLE
                tvKeyword3.text = description.keywords[2]; tvKeyword3.visibility = View.VISIBLE
            } else {
                tvKeyword1.visibility = View.GONE
                tvKeyword2.visibility = View.GONE
                tvKeyword3.visibility = View.GONE
            }
        }
    }

    private fun moveToNewsView(url: String, keywords: Array<String>?) {
        val currentTime = SystemClock.uptimeMillis()
        val elapsedTime = currentTime - lastClickTime
        lastClickTime = currentTime

        if (elapsedTime <= CLICK_INTERVAL) return // block double-click

        val intent = Intent(activity, NewsInfoActivity::class.java)
        intent.putExtra(EXTRA_NEWS_URL, url)
        intent.putExtra(EXTRA_NEWS_KEYWORDS, keywords)
        activity.startActivity(intent)
    }

    internal fun addNews(items: List<NewsItem>) {
        if (newsList != items) {
            newsList.clear()
            newsList.addAll(items)
            notifyDataSetChanged()
        }
    }

    internal fun addUrls(data: HashMap<String, String>) {
        if (urls != data) {
            urls.clear()
            urls.putAll(data)
            notifyDataSetChanged()
        }
    }

    internal fun addDescriptions(data: HashMap<String, NewsDescription?>) {
        if (descriptions != data) {
            descriptions.clear()
            descriptions.putAll(data)
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        val constraintNews: ConstraintLayout = containerView.constraint_news_holder
        val tvNewsSource: AppCompatTextView = containerView.tv_item_source
        val tvNewsTitle: AppCompatTextView = containerView.tv_item_title
        val ivThumbnail: AppCompatImageView = containerView.iv_news_thumbnail
        val tvKeyword1: AppCompatTextView = containerView.tv_keyword1
        val tvKeyword2: AppCompatTextView = containerView.tv_keyword2
        val tvKeyword3: AppCompatTextView = containerView.tv_keyword3
        val tvAbstract: AppCompatTextView = containerView.tv_abstract
    }
}