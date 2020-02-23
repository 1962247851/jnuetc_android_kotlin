package jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus

import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data

/**
 * 转让后的时候传递的消息
 * @param whichState 哪个界面 1是MyselfFragment -1是搜索和dataList
 * @param data 转让后的Data
 * @author qq1962247851
 * @date 2020/2/19 12:24
 */
class TaskMakeOver(val whichState: Int, val data: Data, val position: Int)