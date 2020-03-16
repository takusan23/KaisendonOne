package io.github.kaisendonone.JSON

import io.github.kaisendonone.API.Timeline
import org.json.JSONObject

class StreamingJSON(jsonString: String?, timeline: String, instance: String) {
    val jsonObject = JSONObject(jsonString)
    val event = jsonObject.getString("event")

    // updateのときはTimelineJSONを返す
    val payload = if (event == "update") {
        TimelineJSON(jsonObject.getString("payload"), timeline, instance)
    } else if (event == "notification") {
        TimelineJSON(jsonObject.getString("payload"), "notification", instance)
    } else {
        null
    }
}