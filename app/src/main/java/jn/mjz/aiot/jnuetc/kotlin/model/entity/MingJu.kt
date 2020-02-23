package jn.mjz.aiot.jnuetc.kotlin.model.entity

import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil

/**
 * @author qq1962247851
 * @date 2020/1/15 13:31
 */
class MingJu {
    var id: Long? = null
    var author: String? = null
    var shiName: String? = null
    var content: String? = null
    var topic: String? = null
    var type: String? = null
    override fun toString(): String {
        return GsonUtil.getInstance().toJson(this)
    }

}