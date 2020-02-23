package jn.mjz.aiot.jnuetc.kotlin.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.custom.ContextViewModel
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DataDao
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.SharedPreferencesUtil
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.TaskAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.task.TaskFragment
import org.greenrobot.greendao.Property

/**
 * TaskViewModel
 *
 * @author qq1962247851
 * @date 2020/2/18 15:41
 */
class TaskViewModel(context: Context) : ContextViewModel(context) {

    var state = TaskFragment.STATE_DEFAULT_VALUE
    val dataList = ArrayList<Data>()
    val mldDataList = MutableLiveData<ArrayList<Data>>().apply {
        value = ArrayList()
    }

    lateinit var taskAdapter: TaskAdapter

    fun loadFromDBAndSettings() {
        if (state != TaskFragment.STATE_DEFAULT_VALUE) {
            mldDataList.value!!.clear()
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
                    DataDao.Properties.Local.`in`(locations)
                ).orderDesc(timeProperty).list()
            } else {
                App.daoSession.dataDao.queryBuilder().where(
                    DataDao.Properties.State.eq(
                        state
                    ),
                    DataDao.Properties.Local.`in`(locations)
                ).orderAsc(timeProperty).list()
            }
            if (state != 0) {
                val needToDelete = ArrayList<Data>()
                list.forEach {
                    if (it.repairer.contains(App.getUser().userName)) {
                        needToDelete.add(it)
                    }
                }
                list.removeAll(needToDelete)
            }
            mldDataList.value!!.addAll(
                list
            )
            mldDataList.value = mldDataList.value
        }
        taskAdapter.clearAllSelected()
    }

    fun refresh(callback: HttpUtil.HttpUtilCallBack<ArrayList<Data>>) {
        Data.queryAll(object : HttpUtil.HttpUtilCallBack<ArrayList<Data>> {
            override fun onResponse(result: ArrayList<Data>) {
                loadFromDBAndSettings()
                callback.onResponse(result)
            }

            override fun onFailure(error: String) {
                loadFromDBAndSettings()
                callback.onFailure(error)
            }
        })
    }
}