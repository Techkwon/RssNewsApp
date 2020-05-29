package com.example.rssnesapp.util

import com.example.rssnesapp.data.NewsItem
import com.example.rssnesapp.data.RelatedNews
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class RssParser {
    companion object {
        private const val RSS_URL = "https://news.google.com/rss?hl=ko&gl=KR&ceid=KR:ko"

        private const val CHANNEL_ITEM = "item"

        private const val ITEM_NAME_TITLE = "title"
        private const val ITEM_NAME_LINK = "link"
        private const val ITEM_NAME_GUID = "guid"
        private const val ITEM_NAME_DATE = "pubDate"
        private const val ITEM_NAME_DESC = "description"
        private const val ITEM_NAME_SOURCE = "source"

        private lateinit var itemTitle: String
        private lateinit var itemLink: String
        private lateinit var itemGuid: String
        private lateinit var itemDate: String
        private lateinit var itemDesc: String
        private lateinit var itemSource: String
    }

    enum class Tag{
        ITEM_TITLE,
        ITEM_LINK,
        ITEM_GUID,
        ITEM_DATE,
        ITEM_DESC,
        ITEM_SOURCE,
        ITEM_UNKNOWN
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun getNewsItems(): List<NewsItem> {
        val url = URL(RSS_URL)
        val conn = url.openConnection() as HttpURLConnection

        conn.readTimeout = 5000
        conn.connectTimeout = 5500
        conn.requestMethod = "GET"
        conn.doInput = true
        conn.connect()

        val stream = conn.inputStream
        val parserFactory = XmlPullParserFactory.newInstance()
        parserFactory.isNamespaceAware = true
        val parser = parserFactory.newPullParser()

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(stream, null)

        val items = parseRss(parser)
        stream.close()

        return items
    }

    @Throws(XmlPullParserException::class)
    private fun parseRss(parser: XmlPullParser): List<NewsItem> {
        var eventType = parser.eventType
        var tagType = -1
        var isInItemTag = false
        val items = ArrayList<NewsItem>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == CHANNEL_ITEM) isInItemTag = true
                    if (isInItemTag) tagType = getItemTagNumber(parser.name).ordinal
                }

                XmlPullParser.TEXT -> {
                    if (isInItemTag) setItemValues(tagType, parser.text)
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == CHANNEL_ITEM) {
                        val relatedNews = parseHtml(itemDesc)
                        items.add(NewsItem(itemTitle, itemLink, itemGuid, itemDate, relatedNews, itemSource))
                        isInItemTag = false
                    }
                }
            }

            eventType = parser.next()
        }
        return items
    }

    private fun getItemTagNumber(tagName: String): Tag {
        return when (tagName) {
            ITEM_NAME_TITLE -> Tag.ITEM_TITLE
            ITEM_NAME_LINK -> Tag.ITEM_LINK
            ITEM_NAME_GUID -> Tag.ITEM_GUID
            ITEM_NAME_DATE -> Tag.ITEM_DATE
            ITEM_NAME_DESC -> Tag.ITEM_DESC
            ITEM_NAME_SOURCE -> Tag.ITEM_SOURCE
            else -> Tag.ITEM_UNKNOWN
        }
    }

    private fun setItemValues(tagType: Int, value: String) {
        when (tagType) {
            Tag.ITEM_TITLE.ordinal -> itemTitle = value
            Tag.ITEM_LINK.ordinal -> itemLink = value
            Tag.ITEM_GUID.ordinal -> itemGuid = value
            Tag.ITEM_DATE.ordinal -> itemDate = value
            Tag.ITEM_DESC.ordinal -> itemDesc = value
            Tag.ITEM_SOURCE.ordinal -> itemSource = value
        }
    }

    private fun parseHtml(html: String): List<RelatedNews> {
        val doc = Jsoup.parse(html)
        val tags = doc.body().getElementsByTag("li")
        val news = ArrayList<RelatedNews>()

        for (tag in tags) {
            val aTag = tag.getElementsByTag("a")
            val title = aTag.text()
            val link = aTag.attr("href")
            val source = tag.getElementsByTag("font").text()

            news.add(RelatedNews(link, title, source))
        }

        return news
    }
}