package io.github.kaisendonone.Fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import io.github.kaisendonone.API.AccessToken
import io.github.kaisendonone.API.Account
import io.github.kaisendonone.JSON.AccountJSON
import io.github.kaisendonone.R
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    lateinit var prefSetting: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getString("id")

        prefSetting = PreferenceManager.getDefaultSharedPreferences(context)
        val accessToken = prefSetting.getString("token", null)
        val instance = prefSetting.getString("instance", null)
        if (accessToken != null && instance != null) {
            val token = AccessToken(instance, accessToken)
            // API叩く
            val account = Account(token)
            account.getUserAccount(id) {
                if (it.isSuccessful) {
                    val accountJSON = AccountJSON(it.body?.string()!!)
                    activity?.runOnUiThread {
                        fragment_profile_name.text =
                            "${accountJSON.displayName} @${accountJSON.userName}"
                        fragment_profile_follow.text =
                            "フォロー中：${accountJSON.followingCount} / フォロワー:${accountJSON.followersCount}"
                        Glide.with(fragment_profile_avatar)
                            .load(accountJSON.avatarStatic)
                            .into(fragment_profile_avatar)
                        Glide.with(fragment_profile_header)
                            .load(accountJSON.headerStatic)
                            .into(fragment_profile_header)
                        fragment_profile_note.text =
                            HtmlCompat.fromHtml(accountJSON.note, HtmlCompat.FROM_HTML_MODE_COMPACT)
                    }
                }
            }
        }


    }
}