package jn.mjz.aiot.jnuetc.kotlin.view.activity.detail

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.youth.xframe.XFrame
import com.youth.xframe.utils.log.XLog
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.databinding.FragmentFeedbackBinding
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.User
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.TaskFeedback
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.TaskMakeOver
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.TaskNotExist
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil.HttpUtilCallBack
import jn.mjz.aiot.jnuetc.kotlin.view.activity.gallery.GalleryActivity
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.viewpager.MyFragmentPagerAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractActivity
import jn.mjz.aiot.jnuetc.kotlin.view.custom.LoadingDialog
import jn.mjz.aiot.jnuetc.kotlin.view.custom.ModifyModeBackTipDialog
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.details.DetailsFragment
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.feedback.FeedbackFragment
import jn.mjz.aiot.jnuetc.kotlin.viewmodel.DetailsViewModel
import kotlinx.android.synthetic.main.activity_details.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * @author qq1962247851
 * @date 2020/2/19 11:24
 */
class DetailsActivity : AbstractActivity(),
    DetailsFragment.OnFragmentInteractionListener {

    private var resultCode = RESULT_CODE_DEFAULT_VALUE
    private var id = ID_DEFAULT_VALUE
    var position = POSITION_DEFAULT_VALUE
    var whichState = WHICH_STATE_DEFAULT_VALUE
    private lateinit var detailsViewModel: DetailsViewModel
    private var myFragmentPagerAdapter: MyFragmentPagerAdapter? = null
    private var menu: Menu? = null

    private fun initListener() {
        detailsViewModel.data.observe(
            this,
            Observer { data: Data? ->
                if (data != null) {
                    if (view_pager.adapter == null) {
                        val fragmentList =
                            detailsViewModel.fragmentList
                        val pageTitleList =
                            detailsViewModel.pageTitleList
                        if (data.state.toInt() != 2 && fragmentList.size == 4) {
                            fragmentList.removeAt(3)
                            pageTitleList.removeAt(3)
                        }
                        myFragmentPagerAdapter = MyFragmentPagerAdapter(
                            supportFragmentManager,
                            fragmentList,
                            pageTitleList
                        )
                        view_pager.adapter = myFragmentPagerAdapter
                    }
                    val dataLogSize = data.dataChangeLogs.size
                    val tabAt: TabLayout.Tab? = tab_layout.getTabAt(2)
                    if (tabAt != null) {
                        tabAt.text =
                            if (dataLogSize == 0) getString(R.string.Log) else "${getString(R.string.Log)}（$dataLogSize）"
                    }
                    toolbar.title = String.format(
                        Locale.getDefault(),
                        "%s - %d",
                        data.local,
                        data.id
                    )
                    if (menu != null) {
                        when (data.state.toInt()) {
                            0 -> if (App.getUser().haveModifyAccess()) {
                                menu?.findItem(R.id.menu_details_modify)?.isVisible = true
                            }
                            1, 2 -> if (App.getUser().haveRelationWithData(data) || App.getUser().haveModifyAccess()) {
                                menu?.findItem(R.id.menu_details_modify)?.isVisible = true
                            }
                            else -> {
                            }
                        }
                        if (data.state.toInt() == 1 && App.getUser().haveRelationWithData(
                                data
                            )
                        ) {
                            menu?.findItem(R.id.menu_details_make_over)?.isVisible = true
                            menu?.findItem(R.id.menu_details_input_repair_message)?.isVisible = true
                        } else {
                            menu?.findItem(R.id.menu_details_make_over)?.isVisible = false
                            menu?.findItem(R.id.menu_details_input_repair_message)?.isVisible =
                                false
                        }
                    }
                }
            }
        )
        srl?.setOnRefreshListener { refresh(true) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tool_bar_details, menu)
        this.menu = menu
        detailsViewModel.modifyMode
            .observe(this, Observer { aBoolean: Boolean ->
                val item = menu?.findItem(R.id.menu_details_modify)
                item?.setIcon(if (aBoolean) R.drawable.ic_done_white else R.drawable.ic_modify_white)
                item?.title = if (aBoolean) XFrame.getString(R.string.Finish) else XFrame.getString(
                    R.string.ModifyData
                )
            })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_details_make_over -> {
                LoadingDialog.with(this@DetailsActivity).show()
                User.queryAllUser(object : HttpUtilCallBack<ArrayList<String>> {
                    override fun onResponse(result: ArrayList<String>) {
                        LoadingDialog.with(this@DetailsActivity).cancel()
                        if (result.isEmpty()) {
                            XToast.info("暂无可转让的人")
                        } else {
                            result.remove(App.getUser().userName)
                            val names =
                                arrayOfNulls<String>(result.size)
                            result.toArray<String>(names)
                            val checkItems = BooleanArray(result.size)
                            val builder =
                                AlertDialog.Builder(this@DetailsActivity)
                            builder.setTitle("请选择一个或多个被转让人")
                                .setMultiChoiceItems(
                                    names,
                                    checkItems
                                ) { _: DialogInterface?, _: Int, _: Boolean -> }
                                .setPositiveButton(R.string.Confirm, null)
                                .setNeutralButton(R.string.Cancel, null)
                            val dialog = builder.create()
                            dialog.show()
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                                .setOnClickListener {
                                    val names1 =
                                        StringBuilder()
                                    var j = 0
                                    while (j < checkItems.size) {
                                        if (checkItems[j]) {
                                            names1.append(result[j])
                                            names1.append("，")
                                        }
                                        j++
                                    }
                                    if (names1.toString().isEmpty()) {
                                        XToast.info("请选择一个或多个被转让人")
                                    } else {
                                        names1.deleteCharAt(names1.length - 1)
                                        val repairer = names1.toString()
                                        if (repairer.isNotEmpty() && !repairer.contains(App.getUser().userName)) {
                                            LoadingDialog.with(this@DetailsActivity).show()
                                            val oldJson =
                                                detailsViewModel.data.value.toString()
                                            detailsViewModel.data.value!!.state = 1.toShort()
                                            detailsViewModel.data.value!!.repairer = repairer
                                            detailsViewModel.data.value!!.modify(
                                                oldJson,
                                                object :
                                                    HttpUtilCallBack<Data?> {
                                                    override fun onResponse(result: Data) {
                                                        dialog.cancel()
                                                        LoadingDialog.with(this@DetailsActivity)
                                                            .cancel()
                                                        finish()
                                                        XToast.success(getString(R.string.MakeOverSuccess))
                                                        EventBus.getDefault()
                                                            .post(
                                                                TaskMakeOver(
                                                                    whichState,
                                                                    result,
                                                                    position
                                                                )
                                                            )
                                                        finish()
                                                    }

                                                    override fun onFailure(error: String) {
                                                        LoadingDialog.with(this@DetailsActivity)
                                                            .cancel()
                                                        XToast.error("转让失败\n$error")
                                                    }
                                                }
                                            )
                                        } else {
                                            XToast.info("请检查被转让人")
                                        }
                                    }
                                }
                        }
                    }

                    override fun onFailure(error: String) {
                        LoadingDialog.with(this@DetailsActivity).cancel()
                        XToast.error("转让失败\n$error")
                    }
                })
                true
            }
            R.id.menu_details_input_repair_message -> {
                showFeedbackDialog(detailsViewModel.data.value)
                true
            }
            R.id.menu_details_modify -> {
                modify()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun modify() {
        if (detailsViewModel.modifyMode.value!!) {
            XLog.d("修改" + detailsViewModel.data.value.toString() + "\n备份" + detailsViewModel.dataStringBackup)
            when {
                detailsViewModel.data.value.toString() == detailsViewModel.dataStringBackup -> {
                    XToast.info(getString(R.string.NothingModified))
                    detailsViewModel.modifyMode.value = false
                }
                detailsViewModel.data.value!!.isAllNotEmpty -> {
                    LoadingDialog.with(this@DetailsActivity).show()
                    detailsViewModel.data.value!!.modify(
                        detailsViewModel.dataStringBackup,
                        object :
                            HttpUtilCallBack<Data?> {
                            override fun onResponse(result: Data) {
                                LoadingDialog.with(this@DetailsActivity).cancel()
                                detailsViewModel.dataStringBackup = result.toString()
                                detailsViewModel.modifyMode.value = false
                                detailsViewModel.data.value = result
                                resultCode = Activity.RESULT_OK
                                XToast.success(getString(R.string.ModifySuccess))
                            }

                            override fun onFailure(error: String) {
                                LoadingDialog.with(this@DetailsActivity).cancel()
                                XToast.error("${getString(R.string.ModifyFail)}\n$error")
                            }
                        }
                    )
                }
                else -> {
                    XToast.info(getString(R.string.DataEmpty))
                }
            }
        } else {
            if (detailsViewModel.data.value!!.state.toInt() != 2) {
                if (view_pager.currentItem != 0) {
                    view_pager.setCurrentItem(0, true)
                }
            } else {
                if (!(view_pager.currentItem == 0 || view_pager.currentItem == 3)) {
                    view_pager.setCurrentItem(3, true)
                }
            }
            detailsViewModel.modifyMode.value = true
        }
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this@DetailsActivity, GalleryActivity::class.java)
        intent.putExtra(
            GalleryActivity.URLS,
            GsonUtil.getInstance().toJson(
                detailsViewModel.data.value!!.photoUrlList
            )
        )
        intent.putExtra(
            GalleryActivity.FIRST_INDEX,
            position
        )
        startActivity(intent)
    }

    private fun refresh(showSuccessToast: Boolean) {
        Data.queryById(
            id,
            object : HttpUtilCallBack<Data> {
                override fun onResponse(result: Data) {
                    if (result.toString() != detailsViewModel.dataStringBackup) {
                        detailsViewModel.data.value = result
                        detailsViewModel.dataStringBackup = result.toString()
                    }
                    if (showSuccessToast) {
                        XToast.success(getString(R.string.RefreshSuccess))
                    }
                    srl?.isRefreshing = false
                    detailsViewModel.modifyMode.value = false
                }

                override fun onFailure(error: String) {
                    srl?.isRefreshing = false
                    if (error == "报修单不存在") {
                        EventBus.getDefault().post(TaskNotExist(whichState, id))
                        finish()
                    }
                    XToast.error("${getString(R.string.RefreshFail)}\n$error")
                }
            }
        )
    }

    private fun showFeedbackDialog(data: Data?) {
        val builder =
            AlertDialog.Builder(this@DetailsActivity)
        val fragmentFeedbackBinding: FragmentFeedbackBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(this@DetailsActivity), R.layout.fragment_feedback, null, false
            )
        fragmentFeedbackBinding.data = data
        fragmentFeedbackBinding.modifyMode = true
        fragmentFeedbackBinding.feedbackMode = true
        fragmentFeedbackBinding.onRepairerClick =
            View.OnClickListener { changeRepairer() }
        fragmentFeedbackBinding.repairMessageTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                data?.repairMessage = s.toString()
            }
        }
        fragmentFeedbackBinding.onMarkSelectListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                data?.mark = Data.GET_MARKS()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        fragmentFeedbackBinding.onServiceSelectListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                data?.service = Data.GET_SERVICES()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        builder.setView(fragmentFeedbackBinding.root)
            .setCancelable(false)
            .setPositiveButton("完成", null)
            .setNegativeButton(
                "取消"
            ) { _: DialogInterface?, _: Int ->
                if (detailsViewModel.data.value.toString() != detailsViewModel.dataStringBackup) {
                    detailsViewModel.data.value = GsonUtil.getInstance().fromJson(
                        detailsViewModel.dataStringBackup,
                        Data::class.java
                    )
                }
            }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setOnClickListener {
                data!!.state = 2.toShort()
                if (data.isAllNotEmpty) {
                    if (!data.repairer.contains(App.getUser().userName)) {
                        AlertDialog.Builder(this@DetailsActivity)
                            .setMessage("维修人未含本人，确定提交反馈？")
                            .setTitle(R.string.Attention)
                            .setNegativeButton(R.string.Cancel, null)
                            .setPositiveButton(
                                R.string.Confirm
                            ) { _: DialogInterface?, _: Int ->
                                dialog.cancel()
                                feedback(dialog, data)
                            }.create().show()
                    } else {
                        feedback(dialog, data)
                    }
                } else {
                    XToast.info(getString(R.string.DataEmpty))
                }
            }
    }

    private fun changeRepairer() {
        LoadingDialog.with(this@DetailsActivity).show()
        User.queryAllUser(object :
            HttpUtilCallBack<ArrayList<String>> {
            override fun onResponse(result: ArrayList<String>) {
                LoadingDialog.with(this@DetailsActivity).cancel()
                val names = arrayOfNulls<String>(result.size)
                result.toArray<String>(names)
                val checkItems = BooleanArray(result.size)
                val repairerBuilder =
                    AlertDialog.Builder(this@DetailsActivity)
                repairerBuilder.setTitle("请选择一个或多个维修人")
                    .setMultiChoiceItems(
                        names,
                        checkItems
                    ) { _: DialogInterface?, _: Int, _: Boolean -> }
                    .setPositiveButton(R.string.Confirm, null)
                    .setNeutralButton(R.string.Cancel, null)
                val dialog = repairerBuilder.create()
                dialog.show()
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener {
                        val names1 = StringBuilder()
                        for (j in checkItems.indices) {
                            if (checkItems[j]) {
                                names1.append(result[j])
                                names1.append("，")
                            }
                        }
                        if (names1.toString().isEmpty()) {
                            XToast.info("请选择一个或多个维修人")
                        } else {
                            names1.deleteCharAt(names1.length - 1)
                            val repairer = names1.toString()
                            detailsViewModel.data.value!!.repairer = repairer
                            dialog.cancel()
                        }
                    }
            }

            override fun onFailure(error: String) {
                LoadingDialog.with(this@DetailsActivity).cancel()
                XToast.error("用户列表获取失败\n$error")
            }
        })
    }

    private fun feedback(dialog: AlertDialog, data: Data?) {
        LoadingDialog.with(this@DetailsActivity).show()
        data!!.repairDate = System.currentTimeMillis()
        data.modify(detailsViewModel.dataStringBackup, object : HttpUtilCallBack<Data?> {
            override fun onResponse(result: Data) {
                LoadingDialog.with(this@DetailsActivity).cancel()
                detailsViewModel.data.value = result
                detailsViewModel.dataStringBackup = result.toString()
                dialog.cancel()
                val fragmentList =
                    detailsViewModel.fragmentList
                val pageTitleList =
                    detailsViewModel.pageTitleList
                fragmentList.add(FeedbackFragment())
                pageTitleList.add(getString(R.string.Feedback))
                myFragmentPagerAdapter!!.notifyDataSetChanged()
                view_pager.setCurrentItem(3, true)
                resultCode = Activity.RESULT_OK
                EventBus.getDefault().post(TaskFeedback(position, result))
                XToast.success(getString(R.string.FeedbackSuccess))
            }

            override fun onFailure(error: String) {
                LoadingDialog.with(this@DetailsActivity).cancel()
                XToast.error("${getString(R.string.FeedbackFail)}\n$error")
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(RESULT_CODE_KEY, resultCode)
        outState.putLong(ID_KEY, id)
        outState.putInt(POSITION_KEY, position)
        outState.putInt(WHICH_STATE_KEY, whichState)
        super.onSaveInstanceState(outState)
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return 0
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_details
    }

    override fun initData(savedInstanceState: Bundle?) {
        srl?.isRefreshing = true
        detailsViewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        if (intent != null) {
            id = intent.getLongExtra(ID_KEY, ID_DEFAULT_VALUE)
            position = intent.getIntExtra(POSITION_KEY, POSITION_DEFAULT_VALUE)
            whichState = intent.getIntExtra(WHICH_STATE_KEY, WHICH_STATE_DEFAULT_VALUE)
        } else if (savedInstanceState != null) {
            id = savedInstanceState.getLong(ID_KEY, ID_DEFAULT_VALUE)
            position = savedInstanceState.getInt(POSITION_KEY, POSITION_DEFAULT_VALUE)
            whichState = savedInstanceState.getInt(WHICH_STATE_KEY, WHICH_STATE_DEFAULT_VALUE)
        }
        resultCode = savedInstanceState?.getInt(RESULT_CODE_KEY) ?: RESULT_CODE_DEFAULT_VALUE
        loadFromDB()
        refresh(false)
    }

    private fun loadFromDB() {
        detailsViewModel.data.value = App.daoSession.dataDao.load(id)
    }

    override fun initView() {
        initListener()
        view_pager.setScroll(true)
        view_pager.offscreenPageLimit = detailsViewModel.fragmentList.size
        tab_layout.setupWithViewPager(view_pager)
    }

    override fun preFinish(): Boolean {
        if (detailsViewModel.modifyMode.value!!) {
            ModifyModeBackTipDialog.with(this,
                DialogInterface.OnClickListener { dialog, _ ->
                    if (detailsViewModel.data.value.toString() != detailsViewModel.dataStringBackup) {
                        detailsViewModel.data.value = GsonUtil.getInstance().fromJson(
                            detailsViewModel.dataStringBackup,
                            Data::class.java
                        )
                    }
                    detailsViewModel.modifyMode.value = false
                    dialog.cancel()
                }).show()
            return false
        } else {
            setResult(
                resultCode, Intent().putExtra(ID_KEY, id).putExtra(POSITION_KEY, position).putExtra(
                    WHICH_STATE_KEY, whichState
                ).putExtra(DATA_KEY, detailsViewModel.data.value?.toString())
            )
            return true
        }
    }

    companion object {
        const val DATA_KEY = "data"
        const val ID_KEY = "id"
        const val RESULT_CODE_KEY = "resultCode"
        const val POSITION_KEY = "position"
        const val WHICH_STATE_KEY = "whichState"
        private const val ID_DEFAULT_VALUE = -1L
        private const val RESULT_CODE_DEFAULT_VALUE = Activity.RESULT_CANCELED
        private const val POSITION_DEFAULT_VALUE = -1
        private const val WHICH_STATE_DEFAULT_VALUE = -1
    }

}