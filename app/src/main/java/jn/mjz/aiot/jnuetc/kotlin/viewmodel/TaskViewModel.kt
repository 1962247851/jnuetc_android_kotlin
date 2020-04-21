package jn.mjz.aiot.jnuetc.kotlin.viewmodel

import android.content.Context
import com.youth.xframe.utils.log.XLog
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.custom.ContextViewModel
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.Selecting
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.TaskCountChange
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DataDao
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.SharedPreferencesUtil
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.TaskAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.task.TaskFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.greendao.Property
import org.greenrobot.greendao.query.WhereCondition

/**
 * TaskViewModel
 *
 * @author qq1962247851
 * @date 2020/2/18 15:41
 */
class TaskViewModel(context: Context) : ContextViewModel(context) {

    var state = TaskFragment.STATE_DEFAULT_VALUE
    val dataList = ArrayList<Data>()

    lateinit var taskAdapter: TaskAdapter

    fun loadFromDBAndSettings(limit: Int) {
        if (state != TaskFragment.STATE_DEFAULT_VALUE) {
            val oldSize = dataList.size
            val list: List<Data>
            val drawerNorthString = SharedPreferencesUtil.getSettingPreferences()
                .getString("drawer_north_$state", null)
            val listNorth = if (drawerNorthString == null) {
                Data.GET_LOCALS_N()
            } else {
                GsonUtil.parseJsonArray2List(drawerNorthString, String::class.java)
            }
            val drawerSouthString = SharedPreferencesUtil.getSettingPreferences()
                .getString("drawer_south_$state", null)
            val listSouth = if (drawerSouthString == null) {
                Data.GET_LOCALS_S()
            } else {
                GsonUtil.parseJsonArray2List(drawerSouthString, String::class.java)
            }
            val locations = ArrayList<String>().also {
                it.addAll(listNorth)
                it.addAll(listSouth)
            }
            val orderTimeDesc = SharedPreferencesUtil.getSettingPreferences().getBoolean(
                "order_time_desc_$state",
                state != 0
            )
            val timeProperty: Property = when (state) {
                0 -> {
                    DataDao.Properties.Date
                }
                1 -> {
                    DataDao.Properties.OrderDate
                }
                else -> {
                    DataDao.Properties.RepairDate
                }
            }
            list = if (orderTimeDesc) {
                App.daoSession.dataDao.queryBuilder().where(
                    DataDao.Properties.State.eq(
                        state
                    ),
                    DataDao.Properties.Local.`in`(locations),
                    WhereCondition.PropertyCondition(
                        DataDao.Properties.Repairer,
                        " not like '%${App.getUser().userName}%'"
                    )
                ).orderDesc(timeProperty).limit(limit).list()
            } else {
                App.daoSession.dataDao.queryBuilder().where(
                    DataDao.Properties.State.eq(
                        state
                    ),
                    DataDao.Properties.Local.`in`(locations),
                    WhereCondition.PropertyCondition(
                        DataDao.Properties.Repairer,
                        " not like '%${App.getUser().userName}%'"
                    )
                ).orderAsc(timeProperty).limit(limit).list()
            }
            val newSize = list.size
            if (newSize == oldSize) {
                XToast.info("没有更多数据了")
                XLog.d("加载更多，没有更多数据了")
            } else {
                for (i in oldSize until newSize) {
                    dataList.add(list[i])
//                    if (i == 0) {
//                        taskAdapter.notifyItemChanged(0)
//                    } else {
                    taskAdapter.notifyItemInserted(i)
//                    }
                }
                EventBus.getDefault().post(TaskCountChange(state, newSize))
                EventBus.getDefault().post(Selecting(taskAdapter.selectCnt, newSize))
                XLog.d("加载更多，oldSize = $oldSize ,newSize = $newSize")
            }
        }
    }

    fun refresh(callback: HttpUtil.HttpUtilCallBack<ArrayList<Data>>) {
        Data.queryAll(object : HttpUtil.HttpUtilCallBack<ArrayList<Data>> {
            override fun onResponse(result: ArrayList<Data>) {
                taskAdapter.clearAllSelected()
                val oldSize = dataList.size
                dataList.clear()
                taskAdapter.notifyItemRangeRemoved(0, oldSize)
                loadFromDBAndSettings(TaskFragment.PER_PAGE_COUNT)
                callback.onResponse(result)
            }

            override fun onFailure(error: String) {
                callback.onFailure(error)
            }
        })
    }
}