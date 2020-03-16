package io.github.kaisendonone.Fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import io.github.kaisendonone.Activity.LicenseActivity
import io.github.kaisendonone.R
import kotlinx.android.synthetic.main.fragment_timeline_setting.*
import java.io.File

class TimelineSettingFragment : Fragment() {

    lateinit var timeLineFragment: TimeLineFragment
    lateinit var prefSetting: SharedPreferences

    val imageOpenCode = 845

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timeline_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefSetting = PreferenceManager.getDefaultSharedPreferences(context)

        // ストリーミングスイッチ
        fragment_setting_streaming.isChecked = prefSetting.getBoolean("setting_streaming", true)
        setSwitchSetting(fragment_setting_streaming, "isetting_streaming") {
            // ストリーミング接続など
            timeLineFragment.timelineStreaming.destroy()
            if (it) {
                timeLineFragment.initRealTimeTL()
            }
        }

        // 背景画像
        fragment_setting_background.setOnClickListener {
            //ギャラリー出す
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*";
            startActivityForResult(intent, imageOpenCode)
        }
        // 背景画像クリア
        fragment_setting_background_clear.setOnClickListener {
            backgroundDirClear()
        }

        fragment_setting_license.setOnClickListener {
            val intent = Intent(context, LicenseActivity::class.java)
            startActivity(intent)
        }

    }

    private fun backgroundDirClear() {
        val file = File("${context?.getExternalFilesDir(null)?.path}/background")
        if (file.exists()) {
            file.listFiles()?.forEach {
                it.delete()
            }
        }
        // 反映
        timeLineFragment.initBackground()
    }

    // 背景画像受け取る
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            when (requestCode) {
                imageOpenCode -> {
                    // けす
                    backgroundDirClear()
                    // データをScoped Storageへ保存する
                    val fileName = getFileName(data.data!!)
                    // 出力先フォルダ
                    val backgroundDir =
                        File("${context?.getExternalFilesDir(null)?.path}/background/")
                    backgroundDir.mkdir()
                    // 出力先
                    val inputFile =
                        File("${backgroundDir.path}/${fileName}")
                    inputFile.createNewFile()
                    // なぞ。拡張関数を使ってなんとか
                    // 参考：https://stackoverflow.com/questions/35528409/write-a-large-inputstream-to-file-in-kotlin
                    inputFile.outputStream().use { fileOut ->
                        context?.contentResolver?.openInputStream(data.data!!)?.copyTo(fileOut)
                    }
                    // 反映
                    timeLineFragment.initBackground()
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = context?.contentResolver?.query(
            uri,
            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
            null,
            null,
            null
        )
        cursor?.moveToFirst()
        val name = cursor?.getString(0) ?: "test.png"
        cursor?.close()
        return name
    }

    // スイッチの値を設定に保存する
    fun setSwitchSetting(switch: Switch, name: String, code: (Boolean) -> Unit) {
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            prefSetting.edit {
                putBoolean(name, isChecked)
            }
            code(isChecked)
        }
    }

}