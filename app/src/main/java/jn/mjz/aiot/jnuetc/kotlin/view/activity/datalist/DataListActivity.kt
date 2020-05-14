package jn.mjz.aiot.jnuetc.kotlin.view.activity.datalist

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.Menu
import android.view.View
import android.view.animation.Animation
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.youth.xframe.utils.log.XLog
import com.youth.xframe.utils.statusbar.XStatusBar
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.MySelfTaskDeleted
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.MySelfTaskModified
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.TaskMakeOver
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.TaskNotExist
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DataDao
import jn.mjz.aiot.jnuetc.kotlin.model.util.AnimationUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil
import jn.mjz.aiot.jnuetc.kotlin.view.activity.detail.DetailsActivity
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.MyOnScrollListener
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.TaskAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.WrapContentLinearLayoutManager
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractActivity
import jn.mjz.aiot.jnuetc.kotlin.view.custom.LoadingDialog
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.task.TaskFragment
import kotlinx.android.synthetic.main.activity_data_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DataListActivity : AbstractActivity(), TaskAdapter.ITaskListener {

    var dataList = ArrayList<Data>()
    var title = TITLE_DEFAULT_VALUE

    //如果是从我的界面进入则会用到
    var state = STATE_DEFAULT_VALUE
    private lateinit var myOnScrollListener: MyOnScrollListener
    private lateinit var taskAdapter: TaskAdapter

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskNotExist(taskNotExist: TaskNotExist) {
        XLog.d("taskNotExist.whichState = ${taskNotExist.whichState}")
        if (taskNotExist.whichState != STATE_DEFAULT_VALUE) {
            EventBus.getDefault().post(MySelfTaskModified(taskNotExist.whichState))
        }
        if (dataList.isNotEmpty()) {
            val position = dataList.indexOf(Data().also {
                it.id = taskNotExist.dataId
            })
            if (position != -1) {
                dataList.removeAt(position)
                taskAdapter.notifyItemRemoved(position)
                taskAdapter.notifyItemRangeChanged(
                    position,
                    taskAdapter.itemCount
                )
                updateTitle()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskMakeOver(taskMakeOver: TaskMakeOver) {
        if (taskMakeOver.whichState == -1) {
            //更新
            dataList.remove(taskMakeOver.data)
            dataList.add(taskMakeOver.position, taskMakeOver.data)
            taskAdapter.notifyItemChanged(taskMakeOver.position)
        } else {
            //移除
            dataList.remove(taskMakeOver.data)
            taskAdapter.notifyItemRemoved(taskMakeOver.position)
            taskAdapter.notifyItemRangeChanged(
                taskMakeOver.position,
                taskAdapter.itemCount - taskMakeOver.position
            )
            updateTitle()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == TaskFragment.VIEW_TASK) {
                if (dataList.isNotEmpty()) {
                    //从MySelfFragment进入DetailsActivity的，state=1或2
                    if (data.getIntExtra(DetailsActivity.WHICH_STATE_KEY, -1) == state) {
                        val position = data.getIntExtra(DetailsActivity.POSITION_KEY, -1)
                        if (position != -1) {
                            val fromJson = GsonUtil.getInstance().fromJson(
                                data.getStringExtra(DetailsActivity.DATA_KEY),
                                Data::class.java
                            )
                            if (fromJson.state.toInt() != 2) {
                                //处理中的修改
                                dataList.removeAt(position)
                                dataList.add(position, fromJson)
                                taskAdapter.notifyItemChanged(position)
                                EventBus.getDefault().post(MySelfTaskModified(state))
                            } else {
                                if (state == 1) {
                                    //处理中的反馈
                                    dataList.removeAt(position)
                                    taskAdapter.notifyItemRemoved(position)
                                    taskAdapter.notifyItemRangeChanged(
                                        position,
                                        taskAdapter.itemCount - position
                                    )
                                    updateTitle()
                                } else {
                                    //更新
                                    dataList.removeAt(position)
                                    dataList.add(position, fromJson)
                                    taskAdapter.notifyItemChanged(position)
                                    EventBus.getDefault().post(MySelfTaskModified(state))
                                }
                            }
                        }
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun preFinish(): Boolean {
        return if (appbar_layout_delete.visibility == View.VISIBLE) {
            taskAdapter.quitSelect()
            hideDeleteAppBar()
            false
        } else {
            true
        }
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return 0
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_data_list
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (intent != null) {
            state = intent.getIntExtra(STATE_KEY, STATE_DEFAULT_VALUE)
            title = intent.getStringExtra(TITLE_KEY) ?: TITLE_DEFAULT_VALUE
            GsonUtil.parseJsonArrayAdd2List(
                intent.getStringExtra(DATA_LIST_JSON_KEY),
                dataList,
                Data::class.java, true
            )
        } else if (savedInstanceState != null) {
            state = savedInstanceState.getInt(STATE_KEY, STATE_DEFAULT_VALUE)
            title = savedInstanceState.getString(TITLE_KEY, TITLE_DEFAULT_VALUE)
            GsonUtil.parseJsonArrayAdd2List(
                savedInstanceState.getString(DATA_LIST_JSON_KEY),
                dataList,
                Data::class.java, true
            )
        }
        taskAdapter = TaskAdapter(this, App.getUser().haveDeleteAccess(), dataList, this)
        myOnScrollListener = MyOnScrollListener(
            object : MyOnScrollListener.IStateChangeListener {
                override fun OnStateChange(state: MyOnScrollListener.STATE) {
                    if (state == MyOnScrollListener.STATE.ARRIVED_TOP || state == MyOnScrollListener.STATE.FULL_ON_SCREEN || state == MyOnScrollListener.STATE.SCROLL_DOWN) {
                        fab_scroll_to_top.hide()
                    } else {
                        //到达底部或者往上滑才显示
                        fab_scroll_to_top.show()
                    }
                }

                override fun OnStopScroll() {
                }
            }
        )
    }

    override fun initView() {
        updateTitle()
        recycler_view.layoutManager = WrapContentLinearLayoutManager(this)
        recycler_view.adapter = taskAdapter
        recycler_view.addOnScrollListener(myOnScrollListener)
        fab_scroll_to_top.setOnClickListener {
            recycler_view.smoothScrollToPosition(0)
        }
        initDeleteAppBar()
//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        toolbar.setPadding(0, App.getStatusHeight(this), 0, 0)
    }

    private fun updateTitle() {
        toolbar.title = title + "（" + dataList.size + "单）"
        if (dataList.isEmpty()) {
            finish()
        }
    }

    private fun initDeleteAppBar() {
//        appbar_layout_delete.setPadding(
//            0,
//            App.getStatusHeight(this),
//            0,
//            0
//        )
        toolbar_delete.setNavigationIcon(R.drawable.ic_close)
        toolbar_delete.setNavigationOnClickListener {
            taskAdapter.quitSelect()
        }
        toolbar_delete.menu.findItem(R.id.menu_main_delete).setOnMenuItemClickListener {
            taskAdapter.confirmSelect()
            return@setOnMenuItemClickListener true
        }
        toolbar_delete.menu.findItem(R.id.menu_main_select_all).setOnMenuItemClickListener {
            if (it.title == getString(R.string.SelectAll)) {
                it.setTitle(R.string.ClearAll)
            } else {
                it.setTitle(R.string.SelectAll)
            }
            taskAdapter.toggleSelectAOrClearAll()
            return@setOnMenuItemClickListener true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_KEY, state)
        outState.putString(TITLE_KEY, title)
        outState.putString(DATA_LIST_JSON_KEY, GsonUtil.getInstance().toJson(dataList))
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val STATE_KEY = "state"
        const val STATE_DEFAULT_VALUE = -1
        const val TITLE_KEY = "title"
        const val TITLE_DEFAULT_VALUE = "报修单列表"
        const val DATA_LIST_JSON_KEY = "dataListJson"
    }

    override fun onItemClick(
        position: Int,
        data: Data,
        cardView: CardView
    ) {
        startActivityForResult(Intent(this, DetailsActivity::class.java).also {
            it.putExtra(DetailsActivity.ID_KEY, data.id)
            it.putExtra(DetailsActivity.WHICH_STATE_KEY, state)
            it.putExtra(DetailsActivity.POSITION_KEY, position)
        }, TaskFragment.VIEW_TASK)
    }

    override fun onStartSelect(count: Int) {
        updateDeleteAppBar(count, dataList.size)
        showDeleteAppBar()
    }

    override fun onSelect(count: Int) {
        updateDeleteAppBar(count, dataList.size)
    }

    override fun onConfirmSelect(count: Int, sparseBooleanArray: SparseBooleanArray) {
        val builder = StringBuilder()
        val ids: ArrayList<Long> = ArrayList()
        val needToDelete: ArrayList<Data> = ArrayList()
        for (i in 0 until sparseBooleanArray.size()) {
            val key = sparseBooleanArray.keyAt(i)
            if (sparseBooleanArray[key]) {
                val data = dataList[key]
                val id = data.id
                ids.add(id)
                builder.append(data.local)
                builder.append(" - ")
                builder.append(id)
                builder.append("\n")
                needToDelete.add(
                    App.daoSession.dataDao.queryBuilder().where(
                        DataDao.Properties.Id.eq(
                            id
                        )
                    ).build().unique()
                )
            }
        }
        AlertDialog.Builder(this).setTitle(R.string.Attention)
            .setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作。确认删除以下报修单？\n$builder")
            .setPositiveButton(
                R.string.Cancel
            ) { _: DialogInterface?, _: Int -> }
            .setNegativeButton(
                R.string.Delete
            ) { _: DialogInterface?, _: Int ->
                LoadingDialog.with(this).show()
                Data.deleteMany(ids, object : HttpUtil.HttpUtilCallBack<Boolean?> {
                    override fun onResponse(result1: Boolean) {
                        LoadingDialog.with(this@DataListActivity).cancel()
                        val deleted = ArrayList<Data>().also {
                            it.addAll(dataList)
                            it.removeAll(needToDelete)
                        }
                        val oldSize = dataList.size
                        dataList.clear()
                        taskAdapter.notifyItemRangeRemoved(0, oldSize)
                        dataList.addAll(deleted)
                        taskAdapter.notifyItemRangeInserted(0, dataList.size)
                        taskAdapter.clearAllSelected()
                        if (state != STATE_DEFAULT_VALUE) {
                            EventBus.getDefault().post(MySelfTaskDeleted(state, dataList))
                        }
                        updateTitle()
                        XToast.success(getString(R.string.DeleteSuccess))
                    }

                    override fun onFailure(error: String) {
                        LoadingDialog.with(this@DataListActivity).cancel()
                        XToast.error("${getString(R.string.DeleteFail)}\n$error")
                    }
                })
            }.show()
    }

    override fun onQuitSelectMode() {
        recycler_view.stopScroll()
        hideDeleteAppBar()
    }

    override fun onConfirmClick(position: Int, data: Data) {
        XToast.success("onConfirmClick")
    }

    private fun updateDeleteAppBar(count: Int, totalCount: Int) {
        toolbar_delete.menu.findItem(R.id.menu_main_delete).isEnabled = count != 0
        if (count == totalCount) {
            toolbar_delete.menu.findItem(R.id.menu_main_select_all)
                .setTitle(R.string.ClearAll)
        } else {
            toolbar_delete.menu.findItem(R.id.menu_main_select_all)
                .setTitle(R.string.SelectAll)
        }
        toolbar_delete.title =
            "${getString(R.string.HaveSelect)}（$count/$totalCount）"
        toolbar_delete.title =
            "${getString(R.string.HaveSelect)}（$count/$totalCount）"
    }

    private fun hideDeleteAppBar() {
        if (appbar_layout_delete.visibility != View.GONE) {
            appbar_layout_delete.visibility = View.GONE
            val moveToViewTop = AnimationUtil.moveToViewTop()
            moveToViewTop.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    XStatusBar.setColorNoTranslucent(
                        this@DataListActivity,
                        ContextCompat.getColor(this@DataListActivity, R.color.colorPrimary)
                    )
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
            appbar_layout_delete.startAnimation(moveToViewTop)
        }
    }

    private fun showDeleteAppBar() {
        if (appbar_layout_delete.visibility != View.VISIBLE) {
            XStatusBar.setColorNoTranslucent(
                this,
                ContextCompat.getColor(this, R.color.colorAccent)
            )
            appbar_layout_delete.visibility = View.VISIBLE
            appbar_layout_delete.startAnimation(AnimationUtil.moveToViewLocationFromTop())
        }
    }

}