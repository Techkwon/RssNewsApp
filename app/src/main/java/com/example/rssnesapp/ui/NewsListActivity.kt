package com.example.rssnesapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rssnesapp.R
import com.example.rssnesapp.adapter.NewsAdapter
import com.example.rssnesapp.util.CommonUtils.isNetworkAvailable
import com.example.rssnesapp.util.RssParser
import com.example.rssnesapp.viewmodel.RssViewModel
import com.example.rssnesapp.viewmodel.RssViewModel.Companion.ERROR_IO_EXCEPTION
import com.example.rssnesapp.viewmodel.RssViewModel.Companion.ERROR_NO_UPDATES
import com.example.rssnesapp.viewmodel.RssViewModel.Companion.ERROR_TIME_OUT_EXCEPTION
import kotlinx.android.synthetic.main.activity_news_feed.*

class NewsListActivity : AppCompatActivity() {
    private lateinit var viewModel: RssViewModel

    private lateinit var adapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_feed)
        title = getString(R.string.news_list_title)

        if (isNetworkAvailable(this)) {
            viewModel = ViewModelProviders.of(this).get(RssViewModel::class.java)
            initAdapter()
            getNewsItems()
            setRefreshLayoutListener()
        } else {
            showNetworkError()
        }
    }

    private fun getNewsItems() {
        this.swipe_layout.isRefreshing = true
        viewModel.getNews(RssParser())

        viewModel.newsItems.observe(this, Observer {
            adapter.addNews(it)
        })

        viewModel.thumbnailUrls.observe(this, Observer {
            adapter.addUrls(it)
            this.swipe_layout.isRefreshing = false
        })

        viewModel.descriptions.observe(this, Observer {
            adapter.addDescriptions(it)
        })

        viewModel.errorMessage.observe(this, Observer {
            showErrorMessage(it)
            this.swipe_layout.isRefreshing = false
        })
    }

    private fun showErrorMessage(code: Int) {
        val msg = when (code) {
            ERROR_TIME_OUT_EXCEPTION -> getString(R.string.time_out_error)
            ERROR_IO_EXCEPTION -> getString(R.string.io_error)
            ERROR_NO_UPDATES -> getString(R.string.no_update_error)
            else -> getString(R.string.unknown_error)
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun setRefreshLayoutListener() {
        this.swipe_layout.setOnRefreshListener {
            viewModel.getNews(RssParser())
        }
    }

    private fun initAdapter() {
        adapter = NewsAdapter(this)
        this.rv_feed.layoutManager = LinearLayoutManager(this)
        this.rv_feed.adapter = adapter
        this.rv_feed.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun showNetworkError() {
        this.swipe_layout.visibility = View.GONE
        this.constraint_network_disconnected.visibility = View.VISIBLE
    }
}