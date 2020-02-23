package jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus

import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data

/**
 * 接单后的时候传递的消息
 * @param whichState 哪个界面
 *
 * @author qq1962247851
 * @date 2020/2/19 12:24
 */
class TaskOrdered(val whichState: Int, val data: Data, val position: Int?)