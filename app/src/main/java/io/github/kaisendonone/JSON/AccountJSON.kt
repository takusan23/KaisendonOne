package io.github.kaisendonone.JSON

import org.json.JSONObject

class AccountJSON(var jsonString: String) {
    val jsonObject = JSONObject(jsonString)
    val id = jsonObject.getString("id")
    val userName = jsonObject.getString("username")
    val displayName = jsonObject.getString("display_name")
    val createdAt = jsonObject.getString("created_at")
    val note = jsonObject.getString("note")
    val avatar = jsonObject.getString("avatar")
    val avatarStatic = jsonObject.getString("avatar_static")
    val headerStatic = jsonObject.getString("header_static")
    val followersCount = jsonObject.getInt("followers_count")
    val followingCount = jsonObject.getInt("following_count")
    val lastStatusAt = jsonObject.getString("last_status_at")
}