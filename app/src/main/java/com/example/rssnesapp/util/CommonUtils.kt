@file:Suppress("DEPRECATION")

package com.example.rssnesapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import org.jsoup.select.Elements
import java.util.*
import kotlin.collections.HashMap

object CommonUtils {

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun isNetworkAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // VERSION_CODES.M = API 23
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            capabilities ?: return false

            result = when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }

        return result
    }

    fun getImageUrlFromTags(metaTags: Elements): String {
        val imageTags = metaTags
            .filter { it.attr("property") == "og:image" }
            .filter { it.attr("content").startsWith("http") }

        return if (imageTags.isNotEmpty()) imageTags[0].attr("content") else ""
    }

    fun getAbstract(metaTags: Elements): String {
        val descTags = getDescTags(metaTags)
        return if (descTags.isNotEmpty()) descTags[0].attr("content") else ""
    }

    private fun getDescTags(metaTags: Elements) = metaTags.filter { it.attr("property") == "og:description" }

    fun getKeywordEntries(abstract: String): List<Map.Entry<String, Int>> {
        val extractedWords = splitWords(abstract)
        val wordsWithCounts: HashMap<String, Int> = countDuplicateWords(extractedWords)

        return sortKeywords(wordsWithCounts)
    }

    private fun splitWords(abstract: String): List<String> {
        var words = abstract.replace("-", " ")

        words = Regex(pattern = "[^\\uAC00-\\uD7A3xfe0-9a-zA-Z\\\\s]")
            .replace(words, " ")

        return words
            .split(" ")
            .filter { it.length > 1 }
    }

    private fun countDuplicateWords(extractedWords: List<String>): HashMap<String, Int> {
        val map = HashMap<String, Int>()

        for (i in extractedWords.indices) {
            val comp1 = extractedWords[i]
            var count = 1

            for (j in i + 1 until extractedWords.size) {
                val comp2 = extractedWords[j]
                if (comp1.contains(comp2) || comp2.contains(comp1)) count ++ // count duplicate words
            }
            map[comp1] = count // ex) ["kotlin", 3]
        }

        return map
    }

    private fun sortKeywords(wordsWithCounts: HashMap<String, Int>): List<Map.Entry<String, Int>> {
        val keywordsEntries: List<Map.Entry<String, Int>> = LinkedList(wordsWithCounts.entries) // sorted keywords

        Collections.sort(keywordsEntries) { o1, o2 ->
            val comparison = (o1.value - o2.value) * -1
            if (comparison == 0) o1.key.compareTo(o2.key) else comparison
        }

        return keywordsEntries
    }
}