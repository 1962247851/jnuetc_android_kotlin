package jn.mjz.aiot.jnuetc.kotlin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.custom.Timer
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DataDao
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil

/**
 * MySelfViewModel
 *
 * @author qq1962247851
 * @date 2020/2/19 22:43
 */
class MySelfViewModel : ViewModel() {
    val mldDataListProcessing = MutableLiveData<ArrayList<Data>>().apply {
        value = ArrayList()
    }
    val mldDataListDone = MutableLiveData<ArrayList<Data>>().apply {
        value = ArrayList()
    }
    var timer: Timer? = null

    fun loadProcessingFromDB() {
        mldDataListProcessing.value!!.clear()
        mldDataListProcessing.value!!.addAll(
            App.daoSession.dataDao.queryBuilder().where(
                DataDao.Properties.State.eq(1),
                DataDao.Properties.Repairer.like("%${App.getUser().userName}%")
            ).list()
        )
        mldDataListProcessing.value = mldDataListProcessing.value
    }

    fun loadDoneFromDB() {
        mldDataListDone.value!!.clear()
        mldDataListDone.value!!.addAll(
            App.daoSession.dataDao.queryBuilder().where(
                DataDao.Properties.State.eq(2),
                DataDao.Properties.Repairer.like("%${App.getUser().userName}%")
            ).list()
        )
        mldDataListDone.value = mldDataListDone.value
    }

    fun refresh(callback: HttpUtil.HttpUtilCallBack<ArrayList<Data>>) {
        Data.queryAll(object : HttpUtil.HttpUtilCallBack<ArrayList<Data>> {
            override fun onResponse(result: ArrayList<Data>) {
                loadProcessingFromDB()
                loadDoneFromDB()
                callback.onResponse(result)
            }

            override fun onFailure(error: String) {
                loadProcessingFromDB()
                loadDoneFromDB()
                callback.onFailure(error)
            }
        })
    }

}