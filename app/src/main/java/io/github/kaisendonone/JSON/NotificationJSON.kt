package io.github.kaisendonone.JSON

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Favってくれた人のJSONパースなど
 * */
class NotificationJSON(val jsonString: String) {
    val jsonObject = JSONObject(jsonString)
    val type = jsonObject.getString("type")
    val createdAt = jsonObject.getString("created_at")
    val accountJSONObject = jsonObject.getJSONObject("account")
    val displayName = accountJSONObject.getString("display_name")
    val userName = accountJSONObject.getString("username")
    val userId = accountJSONObject.getString("id")
    val avatar = accountJSONObject.getString("avatar")
    val avatarStatic = accountJSONObject.getString("avatar_static")
    val followersCount = accountJSONObject.getInt("followers_count")
    val followingCount = accountJSONObject.getInt("following_count")

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.JAPAN)
    val unixTime = if (createdAt != null) {
        simpleDateFormat.parse(createdAt).time
    } else {
        0
    }
    private val formatSimpleDateFormat = SimpleDateFormat("yyyy年MM月dd日 HH時mm分ss秒", Locale.JAPAN)
    val formattedTime = formatSimpleDateFormat.format(unixTime)

}