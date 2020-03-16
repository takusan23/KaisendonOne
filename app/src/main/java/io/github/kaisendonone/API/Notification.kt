package io.github.kaisendonone.API

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException

class Notification(val accessToken: AccessToken) {

    val baseUrl = "https://${accessToken.instance}/"

    /**
     * 通知を取得する
     * */
    fun getNotification(): Deferred<Response> = GlobalScope.async {
        val url = "${baseUrl}/api/v1/notifications".toHttpUrlOrNull()?.newBuilder()
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