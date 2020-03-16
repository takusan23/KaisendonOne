package io.github.kaisendonone.API

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * トゥート関係
 * */
class Status(val token: AccessToken) {

    val url = "https://${token.instance}"

    fun statusFav(id: String?, response: (Response) -> Unit) {
        val postData = JSONObject().apply {
        }.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("${url}/api/v1/${id}/favourite?access_token=${token.token}")
            .post(postData)
            .build()
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                response(response)
            }
        })
    }

    fun statusBoost(id: String?, response: (Response) -> Unit) {
        val postData = JSONObject().apply {
            put("access_token", token.token)
        }.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("${url}/api/v1/${id}/reblog")
            .post(postData)
            .build()
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                response(response)
            }
        })
    }

    fun postStatus(statusText: String, visibility: String = "public", response: (Response) -> Unit) {
        //特にWebAPIが思いつかなかったのでMastodonへ投稿するAPIをたたく。POST
        val url = "$url/api/v1/statuses"
        //POSTするときに送るJSON
        val postJSON = JSONObject().apply {
            put("access_token", token.token)
            put("status", statusText)
            put("visibility", visibility)
        }
        //JSONをRequestBodyにする
        val requestBody =
            postJSON.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
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