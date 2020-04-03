package com.example.rssnesapp.data

class NewsItem(
    val title: String,
    val link: String,
    val guid: String,
    val date: String,
    val relatedNews: List<RelatedNews>,
    val source: String
)