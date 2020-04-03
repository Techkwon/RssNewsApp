package com.example.rssnesapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rssnesapp.R
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_TIME = 1300L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        waitDisplay()
    }

    private fun waitDisplay() = launchWork {
        delay(SPLASH_TIME)
        goToNewsFeed()
        return@launchWork
    }

    private fun goToNewsFeed() {
        val intent = Intent(this, NewsListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private inline fun launchWork(crossinline block: suspend () -> Unit): Job {
        return CoroutineScope(Dispatchers.Default + Job()).launch {
            block()
        }
    }

}
