package io.github.kaisendonone.JSON

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * JSONをパースするクラス（通知も対応）
 * */
open class TimelineJSON(val jsonString: String, val timeline: String = "home", val instance: String = "") {

    val jsonObject = if (timeline != "notification") {
        JSONObject(jsonString)
    } else {
        if (JSONObject(jsonString).has("status")) {
            JSONObject(jsonString).getJSONObject("status")
        } else {
            null
        }
    }

    val id = jsonObject?.getString("id")
    val createdAt = jsonObject?.getString("created_at")
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.JAPAN)
    val unixTime = if (createdAt != null) {
        simpleDateFormat.parse(createdAt).time
    } else {
        0
    }
    private val formatSimpleDateFormat = SimpleDateFormat("yyyy年MM月dd日 HH時mm分ss秒", Locale.JAPAN)
    val formattedTime = formatSimpleDateFormat.format(unixTime)
    val visibility = jsonObject?.getString("visibility")
    val reblogsCount = jsonObject?.getInt("reblogs_count")
    val favouritesCount = jsonObject?.getInt("favourites_count")
    val content = jsonObject?.getString("content")
    val applicationName = if (jsonObject?.isNull("application") == false) {
        jsonObject.getJSONObject("application").getString("name")
    } else {
        ""
    }
    val accountJSONObject = jsonObject?.getJSONObject("account")
    val displayName = accountJSONObject?.getString("display_name")
    val userName = accountJSONObject?.getString("username")
    val acct = accountJSONObject?.getString("acct")
    val userId = accountJSONObject?.getString("id")
    val avatar = accountJSONObject?.getString("avatar")
    val avatarStatic = accountJSONObject?.getString("avatar_static")
    val followersCount = accountJSONObject?.getInt("followers_count")
    val followingCount = accountJSONObject?.getInt("following_count")

    // インスタンス名。JSONにはそんなものはない。自作
    val instanceName = acct?.replace("$userName", "")

    // 画像配列
    val mediaAttachments = jsonObject?.getJSONArray("media_attachments")
    val mediaUrlList = arrayListOf<String>().apply {
        repeat(mediaAttachments?.length() ?: 0) {
            val mediaObject = mediaAttachments?.getJSONObject(it)
            if (mediaObject != null) {
                this.add(mediaObject?.getString("url"))
            }
        }
    }

    /**
     * 通知。timelineがnotification以外の場合はnull
     * */
    fun getNotification(): NotificationJSON? {
        if (timeline == "notification") {
            return NotificationJSON(jsonString)
        }
        return null
    }

    /**
     * 通知かどうか
     * */
    fun isNotification(): Boolean {
        return timeline == "notification"
    }

}