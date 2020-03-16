package io.github.kaisendonone.API

import okhttp3.*
import java.io.IOException

class Account(val token: AccessToken) {

    val url = "https://${token.instance}"

    /**
     * 自分のアカウント情報取得
     * */
    fun getMyAccount(response: (Response) -> Unit) {
        val request = Request.Builder().apply {
            url("$url/api/v1/accounts/verify_credentials?access_token=${token.token}")
            get()
        }.build()
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                response(response)
            }
        })
    }

    /**
     * 指定したユーザーのアカウント取得
     * */
    fun getUserAccount(id: String?, response: (Response) -> Unit) {
        val request = Request.Builder().apply {
            url("$url/api/v1/accounts/$id")
            get()
        }.build()
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                response(response)
            }
        })
    }

}