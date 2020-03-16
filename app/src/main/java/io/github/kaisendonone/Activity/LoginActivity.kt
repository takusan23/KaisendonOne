package io.github.kaisendonone.Activity

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager
import io.github.kaisendonone.MainActivity
import io.github.kaisendonone.R
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.Normalizer

class LoginActivity : AppCompatActivity() {

    var client_id = ""
    var client_secret = ""
    var redirect_url = ""
    lateinit var prefSetting: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefSetting = PreferenceManager.getDefaultSharedPreferences(this)

        // ClientID / ClientSecret 取得
        main_activity_login_button.setOnClickListener {
            getClientId()
        }
    }

    // 認証に使うやつ取得
    fun getClientId() {
        val instance = main_activity_login_instance.text.toString()
        val url = "https://$instance/api/v1/apps"
        val clientName = main_activity_login_client.text.toString()
        //ぱらめーたー
        val requestBody = FormBody.Builder()
            .add("client_name", clientName)
            .add("redirect_uris", "https://takusan23.github.io/Kaisendon-Callback-Website/")
            .add("scopes", "read write follow")
            .add(
                "website",
                "https://play.google.com/store/apps/details?id=io.github.takusan23.kaisendon"
            )
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val response_string = response.body?.string()
                    try {
                        val jsonObject = JSONObject(response_string)
                        //ぱーす
                        client_id = jsonObject.getString("client_id")
                        client_secret = jsonObject.getString("client_secret")
                        redirect_url = jsonObject.getString("redirect_uri")
                        //アクセストークン取得のときにActivity再起動されるので保存しておく
                        val editor = prefSetting.edit()
                        editor.putString("client_id", client_id)
                        editor.putString("client_secret", client_secret)
                        editor.putString("redirect_uri", redirect_url)
                        //リダイレクト時にインスタンス名飛ぶので保存
                        editor.putString("register_instance", instance)
                        editor.apply()
                        //Step2:認証画面を表示させる
                        showApplicationRequest()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    errorToast()
                }
            }
        })
    }

    private fun errorToast() {
        runOnUiThread {
            Toast.makeText(this@LoginActivity, "問題が発生しました", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 認証画面表示
     */
    fun showApplicationRequest() {
        //PINを生成する
        val instance = main_activity_login_instance.text.toString()
        val url =
            "https://$instance/oauth/authorize?client_id=$client_id&redirect_uri=$redirect_url&response_type=code&scope=read%20write%20follow"
        val uri = Uri.parse(url)
        //移動
        val intent = Intent(Intent.ACTION_VIEW, uri)
        this@LoginActivity.startActivity(intent)
    }


    /**
     * アクセストークン取得
     */
    override fun onResume() {
        super.onResume()
        //認証最後の仕事、アクセストークン取得
        //URLスキーマからの起動のときの処理
        if (intent.data != null) {
            //code URLパース
            val code = intent.data!!.getQueryParameter("code") ?: ""
            //Step3:アクセストークン取得
            getAccessToken(code)
        }
    }

    fun getAccessToken(code: String) {
        val url = "https://" + prefSetting.getString("register_instance", "") + "/oauth/token"
        //OkHttp
        //ぱらめーたー
        val requestBody = FormBody.Builder()
            .add("client_id", prefSetting.getString("client_id", "")!!)
            .add("client_secret", prefSetting.getString("client_secret", "")!!)
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", prefSetting.getString("redirect_uri", "")!!)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {

                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    //エラー
                    //失敗
                    errorToast()
                } else {
                    //成功
                    val response_string = response.body?.string()
                    try {
                        val jsonObject = JSONObject(response_string)
                        val access_token = jsonObject.getString("access_token")
                        //保存
                        val editor = prefSetting.edit()
                        editor.putString("token", access_token)
                        editor.putString("instance", prefSetting.getString("register_instance", ""))
                        editor.apply()
                        //Activity移動
                        val mainActivity = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(mainActivity);
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }


}
