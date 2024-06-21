package de.bypixeltv.cutils.utils.redis.managers

import org.json.JSONObject

object RedisJsonParser {
    fun parseJson(jsonString: String): JSONObject {
        return JSONObject(jsonString)
    }

    fun getTimeFromRedisJson(jsonObject: JSONObject): Long {
        return jsonObject.getLong("Date")
    }

    fun getMessagesFromRedisJson(jsonObject: JSONObject): String {
        return jsonObject.getString("Messages")
    }

    fun getActionFromRedisJson(jsonObject: JSONObject): String {
        return jsonObject.getString("action")
    }
}