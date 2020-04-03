package com.example.rssnesapp.util

import android.annotation.SuppressLint
import java.io.IOException
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

class SSLConnect {
    private val verifier: HostnameVerifier = HostnameVerifier { _, _ ->
        true
    }

    @SuppressLint("TrustAllX509TrustManager")
    private fun trustAllHosts() {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf<java.security.cert.X509Certificate>()
            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) { }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) { }
        })

        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun postHttps(url: String, connTimeout: Int, readTimeout: Int): HttpsURLConnection? {
        trustAllHosts()
        var https: HttpsURLConnection? = null
        try {
            https = URL(url).openConnection() as HttpsURLConnection
            https.hostnameVerifier = verifier
            https.connectTimeout = connTimeout
            https.readTimeout = readTimeout
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return https
    }
}