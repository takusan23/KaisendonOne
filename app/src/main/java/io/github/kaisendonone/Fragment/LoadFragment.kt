package io.github.kaisendonone.Fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import io.github.kaisendonone.R
import kotlinx.android.synthetic.main.fragment_load.*

class LoadFragment : Fragment() {

    lateinit var prefSetting: SharedPreferences
    lateinit var timeLineFragment: TimeLineFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_load, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefSetting = PreferenceManager.getDefaultSharedPreferences(context)

        // 読み込むTL設定取得
        val isLoadHome = prefSetting.getBoolean("is_load_home", false)
        val isLoadNotification = prefSetting.getBoolean("is_load_notification", false)
        val isLoadLocal = prefSetting.getBoolean("is_load_local", false)
        fragment_load_home.isChecked = isLoadHome
        fragment_load_notification.isChecked = isLoadNotification
        fragment_load_local.isChecked = isLoadLocal
        // チェックしたら保存変更
        setSwitchSetting(fragment_load_home, "is_load_home")
        setSwitchSetting(fragment_load_notification, "is_load_notification")
        setSwitchSetting(fragment_load_local, "is_load_local")

    }

    // スイッチの値を設定に保存する
    fun setSwitchSetting(switch: Switch, name: String) {
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            prefSetting.edit {
                putBoolean(name, isChecked)
            }
            // TL再接続
            timeLineFragment.timelineStreaming.destroy()
            timeLineFragment.getTL {
                // リアルタイム
                timeLineFragment.initRealTimeTL()
            }
        }
    }

}