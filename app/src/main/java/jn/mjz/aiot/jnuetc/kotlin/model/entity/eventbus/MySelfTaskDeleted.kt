package jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus

import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data

/**
 * 报修单被删除
 * @param whichState 处理中还是已维修
 * @param deletedDataList 删除后的报修单列表，用于更新mld
 * @author qq1962247851
 * @date 2020/2/19 17:21
 */
class MySelfTaskDeleted(val whichState: Int, val deletedDataList: ArrayList<Data>)