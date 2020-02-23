package jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus

import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data

/**
 * 报修单被删除
 *
 * @author qq1962247851
 * @date 2020/2/19 17:21
 */
class TaskDeleted(val whichState: Int, val dataList: ArrayList<Data>)