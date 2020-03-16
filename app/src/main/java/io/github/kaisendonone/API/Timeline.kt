package io.github.kaisendonone.API

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException

class Timeline(val accessToken: AccessToken) {

    val baseUrl = "https://${accessToken.instance}/"

    /**
     * ローカルTL取得
     * */
    fun getLocalTimeline(): Deferred<Response> = GlobalScope.async {
        val url = "${baseUrl}/api/v1/timelines/public".toHttpUrlOrNull()?.newBuilder()
        url?.addQueryParameter("local", "true")
        url?.addQueryParameter("access_token", accessToken.token)
        url?.addQueryParameter("limit", "40")
        val request = Request.Builder().apply {
            url(url?.build()?.toUrl()?.toString()!!)
            get()
        }.build()
        val okHttpClient = OkHttpClient()
        val response = okHttpClient.newCall(request).execute()
        return@async response
    }

    /**
     * ホームTL取得
     * */
    fun getHomeTimeline(): Deferred<Response> = GlobalScope.async {
        val url = "${baseUrl}/api/v1/timelines/home".toHttpUrlOrNull()?.newBuilder()
        url?.addQueryParameter("local", "true")
        url?.addQueryParameter("access_token", accessToken.token)
        url?.addQueryParameter("limit", "40")
        val request = Request.Builder().apply {
            url(url?.build()?.toUrl()?.toString()!!)
            get()
        }.build()
        val okHttpClient = OkHttpClient()
        val response = okHttpClient.newCall(request).execute()
        return@async response
    }

}