package com.example.rssnesapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rssnesapp.data.NewsDescription
import com.example.rssnesapp.data.NewsItem
import com.example.rssnesapp.util.CommonUtils.getAbstract
import com.example.rssnesapp.util.CommonUtils.getImageUrlFromTags
import com.example.rssnesapp.util.CommonUtils.getKeywordEntries
import com.example.rssnesapp.util.RssParser
import com.example.rssnesapp.util.SSLConnect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.collections.HashMap

class RssViewModel: ViewModel() {
    internal val newsItems = MutableLiveData<List<NewsItem>>()
    internal val thumbnailUrls = MutableLiveData<HashMap<String, String>>()
    internal val descriptions = MutableLiveData<HashMap<String, NewsDescription?>>()
    internal val errorMessage = MutableLiveData<Int>()

    private val mapUrls = HashMap<String, String>()
    private val mapDescriptions = HashMap<String, NewsDescription?>()

    companion object {
        const val ERROR_TIME_OUT_EXCEPTION = 1
        const val ERROR_IO_EXCEPTION = 2
        const val ERROR_NO_UPDATES = 3

        const val TIME_OUT_DURATION = 5000
    }

    internal fun getNews(rssParser: RssParser) = viewModelScope.launch(Dispatchers.IO) {
        setNews(rssParser.getNewsItems())
    }

    private fun setNews(news: List<NewsItem>) {
        if (newsItems.value == news) {
            viewModelScope.launch(Dispatchers.Main) { errorMessage.value = ERROR_NO_UPDATES }
        } else {
            parseHtml(news)
            viewModelScope.launch(Dispatchers.Main) { newsItems.value = news }
        }
    }

    private fun parseHtml(newsItems: List<NewsItem>) {
        mapUrls.clear()
        mapDescriptions.clear()

        for (news in newsItems) {
            viewModelScope.launch(Dispatchers.IO) {
                var metaTags = Elements()
                try {
                    SSLConnect().postHttps(news.link, 2000, 1000)

                    val doc = Jsoup
                        .connect(news.link)
                        .timeout(TIME_OUT_DURATION)
                        .get()

                    metaTags = doc.getElementsByTag("meta")

                } catch (e: SocketTimeoutException) {
                    setErrorMsg(ERROR_TIME_OUT_EXCEPTION)
                } catch (e: IOException) {
                    setErrorMsg(ERROR_IO_EXCEPTION)
                } finally {
                    extractThumbnailUrl(metaTags, news, newsItems.size)
                    extractDescription(metaTags, news, newsItems.size) // abstract & 3 keywords
                }
            }
        }
    }

    private fun extractThumbnailUrl(metaTags: Elements, news: NewsItem, itemSize: Int) {
        mapUrls[news.guid] = getImageUrlFromTags(metaTags)
        if (mapUrls.size == itemSize) setThumbnails(mapUrls)
    }

    private fun setThumbnails(thumbnails: HashMap<String, String>) = viewModelScope.launch(Dispatchers.Main) {
        thumbnailUrls.value = thumbnails
    }

    private fun extractDescription(metaTags: Elements, news: NewsItem, itemSize: Int) {
        val abstract = getAbstract(metaTags)
        val keywordsEntries: List<Map.Entry<String, Int>> = getKeywordEntries(abstract)

        if (keywordsEntries.isNotEmpty()) {
            mapDescriptions[news.guid] =
                NewsDescription(abstract, arrayOf(keywordsEntries[0].key, keywordsEntries[1].key, keywordsEntries[2].key))
        } else {
            mapDescriptions[news.guid] = null
        }

        if (mapDescriptions.size == itemSize) setDescriptions(mapDescriptions)
    }

    private fun setDescriptions(descriptions: HashMap<String, NewsDescription?>) = viewModelScope.launch(Dispatchers.Main) {
        this@RssViewModel.descriptions.value = descriptions
    }

    private fun setErrorMsg(code: Int) = viewModelScope.launch(Dispatchers.Main) {
        errorMessage.value = code
    }
}