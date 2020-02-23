package jn.mjz.aiot.jnuetc.kotlin.model.entity

import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil

/**
 * @author 19622
 */
class Version {
    var id: Long? = null
    var message: String = ""
    var date = System.currentTimeMillis()
    var version: String = ""
    var url: String = ""
    override fun toString(): String {
        return GsonUtil.getInstance().toJson(this)
    }

}