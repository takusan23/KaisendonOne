package io.github.kaisendonone.Fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import io.github.kaisendonone.API.AccessToken
import io.github.kaisendonone.API.Account
import io.github.kaisendonone.API.Status
import io.github.kaisendonone.JSON.AccountJSON
import io.github.kaisendonone.R
import kotlinx.android.synthetic.main.fragment_toot.*
import org.json.JSONObject

class TootFragment : Fragment() {

    lateinit var prefSetting: SharedPreferences
    lateinit var token: AccessToken

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_toot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefSetting = PreferenceManager.getDefaultSharedPreferences(context)

        val accessToken = prefSetting.getString("token", null)
        val instance = prefSetting.getString("instance", null)
        if (accessToken != null && instance != null) {
            token = AccessToken(instance, accessToken)
        }

        val status = Status(token)
        // 投稿する
        fragment_toot_post.setOnClickListener {
            status.postStatus(fragment_toot_input.text.toString()) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "投稿しました", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // アカウント情報取得
        val account = Account(token)
        account.getMyAccount {
            if (it.isSuccessful) {
                val accountJSON = AccountJSON(it.body?.string()!!)
                activity?.runOnUiThread {
                    fragment_toot_display_name.text =
                        "${accountJSON.displayName} @${accountJSON.userName}"
                    Glide.with(fragment_toot_avatar)
                        .load(accountJSON.avatarStatic)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(30))) //←この一行追加
                        .into(fragment_toot_avatar)
                }
            }
        }

    }

}