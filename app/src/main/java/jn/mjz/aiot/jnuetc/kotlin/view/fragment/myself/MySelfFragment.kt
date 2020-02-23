package jn.mjz.aiot.jnuetc.kotlin.view.fragment.myself

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.*
import jn.mjz.aiot.jnuetc.kotlin.model.util.DateUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.SharedPreferencesUtil
import jn.mjz.aiot.jnuetc.kotlin.view.activity.admin.AdminActivity
import jn.mjz.aiot.jnuetc.kotlin.view.activity.datalist.DataListActivity
import jn.mjz.aiot.jnuetc.kotlin.view.activity.ranking.RankingActivity
import jn.mjz.aiot.jnuetc.kotlin.view.activity.settins.SettingsActivity
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractFragment
import jn.mjz.aiot.jnuetc.kotlin.viewmodel.MySelfViewModel
import kotlinx.android.synthetic.main.fragment_myself.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * MySelfFragment
 *
 * @author qq1962247851
 * @date 2020/2/18 17:34
 */
class MySelfFragment : AbstractFragment() {
    private lateinit var mySelfViewModel: MySelfViewModel

    override fun getLayoutId(): Int {
        return R.layout.fragment_myself
    }

    override fun initData(savedInstanceState: Bundle?) {
        mySelfViewModel = ViewModelProvider(activity!!).get(MySelfViewModel::class.java)
        if (SharedPreferencesUtil.getSettingPreferences().getBoolean("show_reg_time", true)) {
            mySelfViewModel.timer =
                DateUtil.diffTime(App.getUser().regDate, System.currentTimeMillis())
            mySelfViewModel.timer!!.startTiming {
                if (it.day != 0) {
                    tickerView_timer_day.text = it.day.toString()
                    linearLayout_timer_day.visibility = View.VISIBLE
                } else {
                    linearLayout_timer_day.visibility = View.GONE
                }
                if (it.hour != 0) {
                    tickerView_timer_hour.text = it.hour.toString()
                    linearLayout_timer_hour.visibility = View.VISIBLE
                } else {
                    linearLayout_timer_hour.visibility = View.GONE
                }
                if (it.minute != 0) {
                    tickerView_timer_minute.text = it.minute.toString()
                    linearLayout_timer_minute.visibility = View.VISIBLE
                } else {
                    linearLayout_timer_minute.visibility = View.GONE
                }
                tickerView_timer_second.text = it.second.toString()
            }
        } else {
            mcv_myself_timer.visibility = View.GONE
        }
        srl?.isRefreshing = true
        refresh(false)
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        if (App.getUser().haveDeleteAccess()) {
            tv_fragment_myself_admin.visibility = View.VISIBLE
            tv_fragment_myself_admin.setOnClickListener {
                startActivity(
                    Intent(
                        context!!,
                        AdminActivity::class.java
                    )
                )
            }
        } else {
            tv_fragment_myself_admin.visibility = View.GONE
            tv_fragment_myself_admin.setOnClickListener(null)
        }
        tv_fragment_myself_setting.setOnClickListener {
            startActivity(Intent(context!!, SettingsActivity::class.java))
        }
        tv_fragment_myself_ranking.setOnClickListener {
            startActivity(
                Intent(
                    context!!,
                    RankingActivity::class.java
                )
            )
        }
        tv_fragment_myself_processing.setOnClickListener {
            if (mySelfViewModel.mldDataListProcessing.value!!.size == 0) {
                XToast.info(getString(R.string.NoData))
            } else {
                startActivity(
                    Intent(context!!, DataListActivity::class.java)
                        .putExtra(DataListActivity.STATE_KEY, 1)
                        .putExtra(
                            DataListActivity.DATA_LIST_JSON_KEY,
                            GsonUtil.getInstance().toJson(mySelfViewModel.mldDataListProcessing.value)
                        )
                        .putExtra(DataListActivity.TITLE_KEY, getString(R.string.Processing))
                )
            }
        }
        tv_fragment_myself_done.setOnClickListener {
            if (mySelfViewModel.mldDataListDone.value!!.size == 0) {
                XToast.info(getString(R.string.NoData))
            } else {
                startActivity(
                    Intent(context!!, DataListActivity::class.java)
                        .putExtra(DataListActivity.STATE_KEY, 2)
                        .putExtra(
                            DataListActivity.DATA_LIST_JSON_KEY,
                            GsonUtil.getInstance().toJson(mySelfViewModel.mldDataListDone.value)
                        )
                        .putExtra(DataListActivity.TITLE_KEY, getString(R.string.Done))
                )
            }
        }
        mySelfViewModel.mldDataListProcessing.observe(this, Observer {
            tv_fragment_myself_processing.text = "${getString(R.string.Processing)} ${it.size}"
        })
        mySelfViewModel.mldDataListDone.observe(this, Observer {
            tv_fragment_myself_done.text = "${getString(R.string.Done)} ${it.size}"
        })
        srl?.setOnRefreshListener { refresh(true) }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun refresh(showSuccessToast: Boolean) {
        mySelfViewModel.refresh(object : HttpUtil.HttpUtilCallBack<ArrayList<Data>> {
            override fun onResponse(result: ArrayList<Data>) {
                srl?.isRefreshing = false
                if (showSuccessToast) {
                    XToast.success(getString(R.string.RefreshSuccess))
                }
            }

            override fun onFailure(error: String) {
                srl?.isRefreshing = false
                XToast.error("${getString(R.string.RefreshFail)}$error")
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMySelfTaskModified(mySelfTaskModified: MySelfTaskModified) {
        if (mySelfTaskModified.whichState == 1) {
            mySelfViewModel.loadProcessingFromDB()
        } else if (mySelfTaskModified.whichState == 2) {
            mySelfViewModel.loadDoneFromDB()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskOrdered(taskOrdered: TaskOrdered) {
        mySelfViewModel.loadProcessingFromDB()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMySelfTaskDeleted(mySelfTaskDeleted: MySelfTaskDeleted) {
        if (mySelfTaskDeleted.whichState == 1) {
            mySelfViewModel.mldDataListProcessing.value = mySelfTaskDeleted.deletedDataList
        } else if (mySelfTaskDeleted.whichState == 2) {
            mySelfViewModel.mldDataListDone.value = mySelfTaskDeleted.deletedDataList
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskMakeOver(taskMakeOver: TaskMakeOver) {
        if (mySelfViewModel.mldDataListProcessing.value!!.remove(taskMakeOver.data)) {
            mySelfViewModel.mldDataListProcessing.value =
                mySelfViewModel.mldDataListProcessing.value
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskFeedback(taskFeedback: TaskFeedback) {
        mySelfViewModel.loadProcessingFromDB()
        mySelfViewModel.loadDoneFromDB()
    }

    override fun onDestroy() {
        mySelfViewModel.timer?.stopTiming()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDestroy()
    }
}