package io.github.kaisendonone.Fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.kaisendonone.API.AccessToken
import io.github.kaisendonone.API.Notification
import io.github.kaisendonone.API.Timeline
import io.github.kaisendonone.Activity.LoginActivity
import io.github.kaisendonone.Adapter.TimelineAdapter
import io.github.kaisendonone.JSON.TimelineJSON
import io.github.kaisendonone.R
import io.github.kaisendonone.StreamingAPI.TimelineStreaming
import kotlinx.android.synthetic.main.fragment_timeline.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class TimeLineFragment : Fragment() {

    lateinit var timelineAdapter: TimelineAdapter
    var timelineJSONList = arrayListOf<TimelineJSON>()

    // リアルタイム
    lateinit var timelineStreaming: TimelineStreaming

    lateinit var prefSetting: SharedPreferences
    lateinit var token: AccessToken

    // 背景画像設定時はtrue
    var isUseBackground = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timeline, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefSetting = PreferenceManager.getDefaultSharedPreferences(context)

        val accessToken = prefSetting.getString("token", null)
        val instance = prefSetting.getString("instance", null)
        if (accessToken != null && instance != null) {
            token = AccessToken(instance, accessToken)
            initRecyclerView()
            getTL {
                // リアルタイム
                initRealTimeTL()
            }
            // スワイプ
            fragment_timeline_swipe.setOnRefreshListener {
                getTL {}
            }
            // トゥート画面
            changeFragment(TootFragment())
        } else {
            // ログインActivityへ
            showLoginActivity()
        }

        fragment_timeline_fab_account.setOnClickListener {
            showLoginActivity()
        }
        fragment_timeline_fab_post.setOnClickListener {
            // トゥート画面
            changeFragment(TootFragment())
        }
        fragment_timeline_fab_tl.setOnClickListener {
            // 読み込む設定
            changeFragment(LoadFragment().apply { timeLineFragment = this@TimeLineFragment })
        }
        fragment_timeline_fab_setting.setOnClickListener {
            // タイムライン設定
            changeFragment(TimelineSettingFragment().apply {
                timeLineFragment = this@TimeLineFragment
            })
        }

        // 背景画像など
        initBackground()

    }

    fun initBackground() {
        val file = File("${context?.getExternalFilesDir(null)?.path}/background")
        if (file.exists() && file.listFiles()?.isNotEmpty() ?: false) {
            fragment_timeline_background.setImageURI(file.listFiles()?.get(0)?.toUri())
            isUseBackground = true
        } else {
            fragment_timeline_background.setImageDrawable(null)
            isUseBackground = false
        }
    }

    fun changeFragment(fragment: Fragment) {
        fragmentManager?.beginTransaction()
            ?.replace(fragent_timeline_bottomsheet_fragment_parent.id, fragment)
            ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)?.commit()
    }

    fun showLoginActivity() {
        // ログインActivityへ
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
    }

    fun initRealTimeTL() {
        // ストリーミング使う？
        val isUseStreaming = prefSetting.getBoolean("setting_streaming", true)
        if (!isUseStreaming) {
            return
        }
        // 読み込むTL設定取得
        val isLoadHome = prefSetting.getBoolean("is_load_home", false)
        val isLoadNotification = prefSetting.getBoolean("is_load_notification", false)
        val isLoadLocal = prefSetting.getBoolean("is_load_local", false)
        timelineStreaming = TimelineStreaming(token)
        if (isLoadLocal) {
            timelineStreaming.streamingLocal {
                if (it?.payload != null) {
                    val size = timelineJSONList.size
                    timelineJSONList.add(0, it.payload)
                    streamingRecyclerViewInsert(size)
                }
            }
        }
        if (isLoadHome || isLoadNotification) {
            timelineStreaming.streamingUser {
                if (it?.payload != null) {
                    val size = timelineJSONList.size
                    if (!isLoadHome && isLoadNotification) {
                        // ホームなし通知のみ
                        if (it.event == "notification") {
                            timelineJSONList.add(0, it.payload)
                            streamingRecyclerViewInsert(size)
                        }
                    } else {
                        // 通知なしホームのみ
                        // 同じインスタンスならいらない（ホーム＋ローカル表示時）
                        if (isLoadHome && isLoadLocal) {
                            if (it.event == "update") {
                                if (it.payload.instanceName?.isNotEmpty() == true) {
                                    timelineJSONList.add(0, it.payload)
                                    streamingRecyclerViewInsert(size)
                                }
                            }
                        } else {
                            if (it.event == "update") {
                                timelineJSONList.add(0, it.payload)
                                streamingRecyclerViewInsert(size)
                            }
                        }
                    }

                }
            }
        }
    }

    // ストリーミングで更新
    fun streamingRecyclerViewInsert(size: Int) {
        //RecyclerView更新
        fragment_timeline_recyclerview?.post {
            if (timelineJSONList.size <= size + 1) {
                fragment_timeline_recyclerview?.adapter?.notifyItemInserted(0)
            }
            // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
            val pos =
                (fragment_timeline_recyclerview?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            var top = 0
            if ((fragment_timeline_recyclerview?.layoutManager as LinearLayoutManager).childCount > 0) {
                top =
                    (fragment_timeline_recyclerview?.layoutManager as LinearLayoutManager).getChildAt(0)!!.top
            }
            //一番上なら追いかける
            //System.out.println(pos)
            if (pos == 0 || pos == 1) {
                fragment_timeline_recyclerview?.scrollToPosition(0)
            } else {
                fragment_timeline_recyclerview?.post {
                    (fragment_timeline_recyclerview?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        pos + 1,
                        top
                    )
                }
            }
        }
    }

    /**
     * RecyclerView初期化
     * */
    private fun initRecyclerView() {
        // fragment_timeline_recyclerview.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        fragment_timeline_recyclerview.layoutManager = mLayoutManager
        timelineAdapter = TimelineAdapter(timelineJSONList)
        fragment_timeline_recyclerview.adapter = timelineAdapter
        timelineAdapter.token = token
        timelineAdapter.timeLineFragment = this
    }

    /**
     * TL取得
     * */
    fun getTL(ok: () -> Unit) {

        timelineJSONList.clear()
        notifyRecyclerView()
        fragment_timeline_swipe.isRefreshing = true

        val timeline = Timeline(token)
        val notification = Notification(token)

        // 読み込むTL設定取得
        val isLoadHome = prefSetting.getBoolean("is_load_home", false)
        val isLoadNotification = prefSetting.getBoolean("is_load_notification", false)
        val isLoadLocal = prefSetting.getBoolean("is_load_local", false)

        GlobalScope.launch {
            if (isLoadHome) {
                val homeTL = timeline.getHomeTimeline().await()
                responseParse(homeTL, "home")
            }
            if (isLoadNotification) {
                val notification = notification.getNotification().await()
                responseParse(notification, "notification")
            }
            if (isLoadLocal) {
                val local = timeline.getLocalTimeline().await()
                responseParse(local, "local")
            }
            // ソート
            timelineJSONList.sortByDescending { timelineJSON -> timelineJSON.unixTime }
            activity?.runOnUiThread {
                fragment_timeline_swipe.isRefreshing = false
                notifyRecyclerView()
            }
            ok()
        }
    }

    fun responseParse(response: Response, timeline: String = "home") {
        // 読み込むTL設定取得
        val isLoadHome = prefSetting.getBoolean("is_load_home", false)
        val isLoadNotification = prefSetting.getBoolean("is_load_notification", false)
        val isLoadLocal = prefSetting.getBoolean("is_load_local", false)
        if (response.isSuccessful) {
            val jsonArray = JSONArray(response.body?.string())
            repeat(jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(it)
                // ホームTLでローカルも読み込む設定のとき（かぶらないように）
                if (timeline == "home" && isLoadLocal) {
                    val timelineJSON = TimelineJSON(jsonObject.toString())
                    if (timelineJSON.instanceName?.isNotEmpty() == true) {
                        timelineJSONList.add(TimelineJSON(jsonObject.toString(), timeline, token.instance))
                    }
                } else {
                    timelineJSONList.add(TimelineJSON(jsonObject.toString(), timeline, token.instance))
                }
            }
        }
    }

    private fun notifyRecyclerView() {
        activity?.runOnUiThread {
            timelineAdapter.notifyDataSetChanged()
        }
    }

    private fun insertRecyclerView() {
        activity?.runOnUiThread {
            timelineAdapter.notifyItemInserted(0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this@TimeLineFragment::timelineStreaming.isInitialized) {
            timelineStreaming.destroy()
        }
    }

}