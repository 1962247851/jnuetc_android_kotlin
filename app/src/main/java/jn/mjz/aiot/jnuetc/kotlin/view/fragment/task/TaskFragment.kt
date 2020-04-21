package jn.mjz.aiot.jnuetc.kotlin.view.fragment.task

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.animation.Animation
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.youth.xframe.XFrame
import com.youth.xframe.utils.XDensityUtils
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.custom.ContextViewModelFactory
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.*
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DataDao
import jn.mjz.aiot.jnuetc.kotlin.model.util.AnimationUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil.HttpUtilCallBack
import jn.mjz.aiot.jnuetc.kotlin.view.activity.detail.DetailsActivity
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.MyOnScrollListener
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.TaskAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.WrapContentLinearLayoutManager
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.WrapContentStaggeredGridLayoutManager
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractFragment
import jn.mjz.aiot.jnuetc.kotlin.view.custom.LoadingDialog
import jn.mjz.aiot.jnuetc.kotlin.viewmodel.TaskViewModel
import kotlinx.android.synthetic.main.fragment_task.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * TaskFragment
 *
 * @author qq1962247851
 * @date 2020/2/18 17:55
 */
class TaskFragment : AbstractFragment, TaskAdapter.ITaskListener {

    private var state = STATE_DEFAULT_VALUE
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var myOnScrollListener: MyOnScrollListener
    private var fabScrollToTopMarginBottom: Int = 0
    private lateinit var fabScrollToTopLayoutParams: CoordinatorLayout.LayoutParams

    override fun getLayoutId(): Int {
        return R.layout.fragment_task
    }

    override fun initData(savedInstanceState: Bundle?) {
        srl?.isRefreshing = true
        if (savedInstanceState != null) {
            state = savedInstanceState.getInt(STATE_KEY, STATE_DEFAULT_VALUE)
        }
        taskViewModel =
            ContextViewModelFactory.getInstance(activity!!).create(TaskViewModel::class.java)
        taskViewModel.state = state
        taskViewModel.taskAdapter = TaskAdapter(
            context!!,
            App.getUser().haveDeleteAccess(),
            taskViewModel.dataList,
            this
        )
        refresh(false)
        myOnScrollListener = MyOnScrollListener(
            object : MyOnScrollListener.IStateChangeListener {
                override fun OnStateChange(state: MyOnScrollListener.STATE) {
                    if (state == MyOnScrollListener.STATE.SCROLL_DOWN) {
                        if (fab_scroll_to_top.isOrWillBeShown) {
                            fab_scroll_to_top.startAnimation(AnimationUtil.moveToViewBottomQuickly()
                                .also { animation ->
                                    //解决闪烁问题
                                    animation.isFillEnabled = true
                                    animation.setAnimationListener(object :
                                        Animation.AnimationListener {
                                        override fun onAnimationRepeat(animation: Animation?) {
                                        }

                                        override fun onAnimationEnd(animation: Animation?) {
                                            /**由于在执行后view的位置不变，如需要写点击事件就得加上这些重新布置view**/
                                            fabScrollToTopLayoutParams.bottomMargin =
                                                fabScrollToTopMarginBottom - fab_scroll_to_top.height
                                            fab_scroll_to_top.layoutParams =
                                                fabScrollToTopLayoutParams
                                        }

                                        override fun onAnimationStart(animation: Animation?) {
                                        }
                                    })
                                })
                        } else {
                            /**由于在执行后view的位置不变，如需要写点击事件就得加上这些重新布置view**/
                            fabScrollToTopLayoutParams.bottomMargin =
                                fabScrollToTopMarginBottom - fab_scroll_to_top.height
                            fab_scroll_to_top.layoutParams = fabScrollToTopLayoutParams
                        }
                    } else if (state == MyOnScrollListener.STATE.SCROLL_UP) {
                        /**由于在执行后view的位置不变，如需要写点击事件就得加上这些重新布置view**/
                        fabScrollToTopLayoutParams.bottomMargin = fabScrollToTopMarginBottom
                        fab_scroll_to_top.layoutParams = fabScrollToTopLayoutParams
                        fab_scroll_to_top.startAnimation(AnimationUtil.moveToViewLocationFromBottomQuickly()
                            .also { animation ->
                                //解决闪烁问题
                                animation.isFillEnabled = true
                                animation.setAnimationListener(object :
                                    Animation.AnimationListener {
                                    override fun onAnimationRepeat(animation: Animation?) {
                                    }

                                    override fun onAnimationEnd(animation: Animation?) {
                                    }

                                    override fun onAnimationStart(animation: Animation?) {
                                    }
                                })
                            })
                    }
                    if (!taskViewModel.taskAdapter.isSelectMode()) {
                        EventBus.getDefault().post(RecyclerViewScrollStateChange(state))
                    }
                    if (state == MyOnScrollListener.STATE.ARRIVED_TOP || state == MyOnScrollListener.STATE.FULL_ON_SCREEN || state == MyOnScrollListener.STATE.SCROLL_DOWN) {
                        fab_scroll_to_top.hide()
                    } else {
                        //到达底部或者往上滑才显示
                        fab_scroll_to_top.show()
                    }
                    if (state == MyOnScrollListener.STATE.ARRIVED_BOTTOM) {
                        taskViewModel.loadFromDBAndSettings(taskViewModel.dataList.size + PER_PAGE_COUNT)
                    }
                }

                override fun OnStopScroll() {
                }
            }
        )
    }

    private fun refresh(showSuccessToast: Boolean) {
        taskViewModel.refresh(object : HttpUtilCallBack<ArrayList<Data>> {
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

    override fun initView() {
        fabScrollToTopLayoutParams =
            fab_scroll_to_top.layoutParams as CoordinatorLayout.LayoutParams
        fabScrollToTopMarginBottom = fabScrollToTopLayoutParams.bottomMargin
        recycler_view.layoutManager = if (XDensityUtils.getScreenHeight() <= XFrame.screenWidth) {
            WrapContentStaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        } else {
            WrapContentLinearLayoutManager(context)
        }
        recycler_view.adapter = taskViewModel.taskAdapter
        srl?.setOnRefreshListener { refresh(true) }
//        taskViewModel.mldDataList.observe(this, Observer {
//            //TODO 加载更多的时候更新UI，从当前list的size遍历到新的list的size，add后通知adapter更新
//            val oldSize = taskViewModel.dataList.size
//            val size = it.size
//            EventBus.getDefault().post(
//                TaskCountChange(
//                    state,
//                    size
//                )
//            )
//            if (size > oldSize) {
//                //加载更多
//                if (size != 0) {
//                    for (i in oldSize until size) {
//                        taskViewModel.dataList.add(it[i])
//                    }
//                    taskViewModel.taskAdapter.notifyItemRangeInserted(oldSize, size - oldSize)
//                } else {
//                    taskViewModel.taskAdapter.notifyItemChanged(0)
//                }
//            } else if (size < oldSize) {
//                //刷新
//                taskViewModel.dataList.clear()
//                taskViewModel.taskAdapter.notifyItemRangeRemoved(0, oldSize)
//                taskViewModel.dataList.addAll(it)
//                if (size != 0) {
//                    taskViewModel.taskAdapter.notifyItemRangeInserted(0, size)
//                } else {
//                    taskViewModel.taskAdapter.notifyItemChanged(0)
//                }
//            }
////            taskViewModel.dataList.clear()
////            taskViewModel.taskAdapter.notifyItemRangeRemoved(0, oldSize)
////            taskViewModel.dataList.addAll(it)
////            if (size != 0) {
////                taskViewModel.taskAdapter.notifyItemRangeInserted(0, size)
////            } else {
////                taskViewModel.taskAdapter.notifyItemChanged(0)
////            }
//        })
        recycler_view.addOnScrollListener(myOnScrollListener)
        fab_scroll_to_top.setOnClickListener {
            recycler_view.smoothScrollToPosition(0)
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onItemClick(
        position: Int,
        data: Data,
        cardView: CardView
    ) {
        startActivityForResult(
            Intent(context, DetailsActivity::class.java).also {
                it.putExtra(DetailsActivity.ID_KEY, data.id)
                it.putExtra(DetailsActivity.POSITION_KEY, position)
                it.putExtra(DetailsActivity.WHICH_STATE_KEY, state)
            },
            VIEW_TASK
//            ActivityOptions.makeSceneTransitionAnimation(
//                activity!!,
//                cardView as View,
//                "taskCard"
//            ).toBundle()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == VIEW_TASK) {
                if (taskViewModel.dataList.isNotEmpty()) {
                    if (data.getIntExtra(DetailsActivity.WHICH_STATE_KEY, -1) == state) {
                        val position = data.getIntExtra(DetailsActivity.POSITION_KEY, -1)
                        if (position != -1) {
                            val fromJson = GsonUtil.getInstance().fromJson(
                                data.getStringExtra(DetailsActivity.DATA_KEY),
                                Data::class.java
                            )
                            taskViewModel.dataList.removeAt(position)
                            taskViewModel.dataList.add(position, fromJson)
                            taskViewModel.taskAdapter.notifyItemChanged(position)
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

    override fun onStartSelect(count: Int) {
        EventBus.getDefault().post(
            StartSelect(
                count,
                taskViewModel.dataList.size
            )
        )
    }

    override fun onSelect(count: Int) {
        EventBus.getDefault().post(
            Selecting(
                count,
                taskViewModel.dataList.size
            )
        )
    }

    override fun onConfirmSelect(count: Int, sparseBooleanArray: SparseBooleanArray) {
        val builder = StringBuilder()
        val ids: ArrayList<Long> = ArrayList()
        val needToDelete: ArrayList<Data> = ArrayList()
        for (i in 0 until sparseBooleanArray.size()) {
            val key = sparseBooleanArray.keyAt(i)
            if (sparseBooleanArray[key]) {
                val data = taskViewModel.dataList[key]
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
        AlertDialog.Builder(context!!).setTitle(R.string.Attention)
            .setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作。确认删除以下报修单？\n$builder")
            .setPositiveButton(
                R.string.Cancel
            ) { _: DialogInterface?, _: Int -> }
            .setNegativeButton(
                R.string.Delete
            ) { _: DialogInterface?, _: Int ->
                LoadingDialog.with(context!!).show()
                Data.deleteMany(ids, object : HttpUtilCallBack<Boolean?> {
                    override fun onResponse(result1: Boolean) {
                        LoadingDialog.with(context!!).cancel()

//用DiffUtil会有点小问题
//                        val deleted = ArrayList<Data>().also {
//                            it.addAll(taskViewModel.dataList)
//                            it.removeAll(needToDelete)
//                        }

//                        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
//                            override fun areItemsTheSame(
//                                oldItemPosition: Int,
//                                newItemPosition: Int
//                            ): Boolean {
//                                return taskViewModel.dataList[oldItemPosition].id == deleted[newItemPosition].id
//                            }
//
//                            override fun getOldListSize(): Int {
//                                return taskViewModel.dataList.size
//                            }
//
//                            override fun getNewListSize(): Int {
//                                return deleted.size
//                            }
//
//                            override fun areContentsTheSame(
//                                oldItemPosition: Int,
//                                newItemPosition: Int
//                            ): Boolean {
//                                return true
//                            }
//                        }).dispatchUpdatesTo(taskViewModel.taskAdapter)

                        for (data in needToDelete) {
                            val indexOf = taskViewModel.dataList.indexOf(data)
                            taskViewModel.dataList.removeAt(indexOf)
                            taskViewModel.taskAdapter.notifyItemRemoved(indexOf)
                            taskViewModel.taskAdapter.notifyItemRangeChanged(
                                indexOf,
                                taskViewModel.taskAdapter.itemCount - indexOf
                            )
                        }
//                        val oldSize = taskViewModel.dataList.size
//                        taskViewModel.taskAdapter.notifyItemRangeRemoved(0, oldSize)
//                        taskViewModel.dataList.removeAll(needToDelete)
//                        taskViewModel.taskAdapter.notifyItemRangeInserted(
//                            0,
//                            taskViewModel.dataList.size
//                        )
                        taskViewModel.taskAdapter.clearAllSelected()
                        XToast.success(getString(R.string.DeleteSuccess))
                    }

                    override fun onFailure(error: String) {
                        LoadingDialog.with(context!!).cancel()
                        XToast.error("${getString(R.string.DeleteFail)}\n$error")
                    }
                })
            }.show()
    }

    override fun onQuitSelectMode() {
        //退出的时候reset一下滑动状态，防止往下滑动不隐藏bottomNavigationView
        myOnScrollListener.resetState()
        recycler_view.stopScroll()
        EventBus.getDefault().post(
            QuitSelectMode(
                state,
                false
            )
        )
    }

    override fun onConfirmClick(position: Int, data: Data) {
        order(data, position)
    }

    private fun order(data: Data, position: Int) {
        LoadingDialog.with(context!!).show()
        Data.queryById(data.id, object : HttpUtilCallBack<Data> {
            override fun onResponse(result: Data) {
                LoadingDialog.with(context!!).cancel()
                if (result.state.toInt() == 0) {
                    val booleans = booleanArrayOf(false)
                    val dataBackUpString = result.toString()
                    AlertDialog.Builder(context!!).setCancelable(false)
                        .setTitle("${result.local} - ${result.id}")
                        .setNegativeButton(R.string.Cancel, null)
                        .setPositiveButton(
                            R.string.Order
                        ) { _, _ ->
                            LoadingDialog.with(context!!).show()
                            result.orderDate = System.currentTimeMillis()
                            result.repairer = App.getUser().userName
                            result.state = 1.toShort()
                            result.modify(
                                dataBackUpString,
                                object : HttpUtilCallBack<Data> {

                                    override fun onResponse(result: Data) {
                                        LoadingDialog.with(context!!).cancel()
                                        if (booleans[0]) {
                                            Data.openQq(result.qq)
                                        }
                                        XToast.success(getString(R.string.OrderSuccess))
                                        EventBus.getDefault()
                                            .post(
                                                TaskOrdered(
                                                    state,
                                                    result,
                                                    position
                                                )
                                            )
                                    }

                                    override fun onFailure(error: String) {
                                        LoadingDialog.with(context!!).cancel()
                                        XToast.error("${getString(R.string.OrderFail)}\n$error")
                                    }
                                })
                        }
                        .setMultiChoiceItems(
                            R.array.open_qq_after_order_dialog,
                            booleans
                        ) { _, _, _ -> }
                        .show()
                } else {
                    LoadingDialog.with(context!!).cancel()
                    XToast.warning("唉呀，有人抢先了...\n该报修单已被 " + result.repairer + " 处理")
                    EventBus.getDefault().post(
                        TaskOrdered(
                            state,
                            result,
                            position
                        )
                    )
                }
            }

            override fun onFailure(error: String) {
                LoadingDialog.with(context!!).cancel()
                XToast.error("${getString(R.string.OrderFail)}\n$error")
            }
        })
    }

    companion object {
        const val STATE_KEY = "state"
        const val STATE_DEFAULT_VALUE = 3
        const val VIEW_TASK = 0
        const val PER_PAGE_COUNT = 10
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_KEY, state)
        super.onSaveInstanceState(outState)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskMakeOver(taskMakeOver: TaskMakeOver) {
        if (state == 1) {
            taskViewModel.dataList.add(0, taskMakeOver.data)
            taskViewModel.taskAdapter.notifyItemInserted(0)
            taskViewModel.taskAdapter.notifyItemRangeChanged(0, taskViewModel.taskAdapter.itemCount)
            recycler_view.scrollToPosition(0)
            EventBus.getDefault().post(
                TaskCountChange(
                    state,
                    taskViewModel.dataList.size
                )
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskOrdered(taskOrder: TaskOrdered) {
        if (taskOrder.whichState == state) {
            var position = taskOrder.position
            if (position == null) {
                position = taskViewModel.dataList.indexOf(taskOrder.data)
            }
            if (position != -1) {
                taskViewModel.dataList.removeAt(position)
                taskViewModel.taskAdapter.notifyItemRemoved(position)
                taskViewModel.taskAdapter.notifyItemRangeChanged(
                    position,
                    taskViewModel.taskAdapter.itemCount - position
                )
                EventBus.getDefault().post(
                    TaskCountChange(
                        state,
                        taskViewModel.dataList.size
                    )
                )
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onQuitSelect(quitSelectMode: QuitSelectMode) {
        if (quitSelectMode.whichState == state) {
            if (quitSelectMode.fromActivity) {
                taskViewModel.taskAdapter.quitSelect()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onConfirmSelect(confirmSelect: ConfirmSelect) {
        if (confirmSelect.whichState == state) {
            taskViewModel.taskAdapter.confirmSelect()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSelectOrClearAll(toggleSelectOrClearAll: ToggleSelectOrClearAll) {
        if (toggleSelectOrClearAll.whichState == state) {
            taskViewModel.taskAdapter.toggleSelectAOrClearAll()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskNotExist(taskNotExist: TaskNotExist) {
        if (taskNotExist.whichState == state) {
            if (taskViewModel.dataList.isNotEmpty()) {
                val position = taskViewModel.dataList.indexOf(Data().also {
                    it.id = taskNotExist.dataId
                })
                if (position != -1) {
                    taskViewModel.dataList.removeAt(position)
                    taskViewModel.taskAdapter.notifyItemRemoved(position)
                    taskViewModel.taskAdapter.notifyItemRangeChanged(
                        position,
                        taskViewModel.taskAdapter.itemCount
                    )
                }
                EventBus.getDefault().post(TaskCountChange(state, taskViewModel.dataList.size))
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDrawerClose(drawerClose: DrawerClose) {
        if (drawerClose.whichState == state) {
            val oldSize = taskViewModel.dataList.size
            taskViewModel.dataList.clear()
            taskViewModel.taskAdapter.notifyItemRangeRemoved(0, oldSize)
            taskViewModel.loadFromDBAndSettings(PER_PAGE_COUNT)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskFeedback(taskFeedback: TaskFeedback) {
        if (state == 2) {
            if (!taskFeedback.data.repairer.contains(App.getUser().userName)) {
                taskViewModel.dataList.add(0, taskFeedback.data)
                taskViewModel.taskAdapter.notifyItemInserted(0)
                taskViewModel.taskAdapter.notifyItemRangeChanged(
                    0,
                    taskViewModel.taskAdapter.itemCount
                )
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewDataFromCloud(newDataFromCloud: NewDataFromCloud) {
        if (state == 0) {
            taskViewModel.dataList.add(0, newDataFromCloud.data)
            taskViewModel.taskAdapter.notifyItemInserted(0)
            recycler_view.scrollToPosition(0)
        }
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDestroy()
    }

    constructor()
    constructor(state: Int) {
        this.state = state
    }
}