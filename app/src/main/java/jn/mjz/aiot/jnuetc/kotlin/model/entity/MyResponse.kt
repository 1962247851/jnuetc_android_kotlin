package jn.mjz.aiot.jnuetc.kotlin.model.entity

import com.google.gson.JsonObject
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil

/**
 * 自定义服务器响应
 *
 * @author qq1962247851
 * @date 2020/1/29 18:00
 */
class MyResponse {
    var error = -1
    var msg: String = ""
    var bodyJson: String? = null

    constructor()

    constructor(jsonObject: JsonObject) {
        error = jsonObject[ERROR_PROPERTY].asInt
        msg = jsonObject[MSG_PROPERTY].asString
        if (jsonObject[BODY_PROPERTY] != null) {
            bodyJson = jsonObject[BODY_PROPERTY].asString
        }
    }

    constructor(result: String) {
        val jsonObject = GsonUtil.getInstance().fromJson(result, JsonObject::class.java)
        error = jsonObject[ERROR_PROPERTY].asInt
        msg = jsonObject[MSG_PROPERTY].asString
        if (jsonObject[BODY_PROPERTY] != null) {
            bodyJson = jsonObject[BODY_PROPERTY].asString
        }
    }

    companion object {
        const val ERROR_PROPERTY = "error"
        const val MSG_PROPERTY = "msg"
        const val BODY_PROPERTY = "body"
        const val FAILED = 0
        const val SUCCESS = 1
        const val ERROR = -1
    }
}