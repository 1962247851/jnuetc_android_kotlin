package jn.mjz.aiot.jnuetc.kotlin.view.activity.main

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Process
import android.util.SparseBooleanArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.xiaomi.mipush.sdk.MiPushClient
import com.youth.xframe.XFrame
import com.youth.xframe.utils.XDateUtils
import com.youth.xframe.utils.log.XLog
import com.youth.xframe.utils.permission.XPermission
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.*
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DataDao
import jn.mjz.aiot.jnuetc.kotlin.model.util.AnimationUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.SharedPreferencesUtil
import jn.mjz.aiot.jnuetc.kotlin.view.activity.about.AboutActivity
import jn.mjz.aiot.jnuetc.kotlin.view.activity.detail.DetailsActivity
import jn.mjz.aiot.jnuetc.kotlin.view.activity.login.LoginActivity
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.CheckableAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.MyOnScrollListener
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.TaskAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.WrapContentLinearLayoutManager
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.viewpager2.MainPagerAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractActivity
import jn.mjz.aiot.jnuetc.kotlin.view.custom.LoadingDialog
import jn.mjz.aiot.jnuetc.kotlin.view.custom.ModifyPasswordDialog
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.greendao.query.QueryBuilder

/**
 * MainActivity
 *
 * @author qq1962247851
 * @date 2020/2/17 22:58
 */
class MainActivity : AbstractActivity(), SearchView.OnQueryTextListener, TaskAdapter.ITaskListener {


    private val animation = AlphaAnimation(1f, 0.1f).apply {
        fillAfter = false
        duration = 1000
    }
    private var cnt =
        (SharedPreferencesUtil.getSettingPreferences().getString("show_time", "5") ?: "5").toInt()
    private lateinit var searchView: SearchView
    private var backTime = 0L
    private var inputString = ""
    private var isSearchMode = false
    private var searchDataList = ArrayList<Data>()
    private lateinit var searchTaskAdapter: TaskAdapter
    private var currentState = 0
    private lateinit var mainPagerAdapter: MainPagerAdapter
    private lateinit var checkableAdapterSouth: CheckableAdapter
    private lateinit var checkableAdapterNorth: CheckableAdapter
    private val selectedTitleNorth = ArrayList<String>()
    private val selectedTitleSouth = ArrayList<String>()

    override fun preFinish(): Boolean {
        return when {
            isSearchMode -> {
                if (searchTaskAdapter.isSelectMode()) {
                    searchTaskAdapter.quitSelect()
                } else {
                    quitSearchMode()
                }
                false
            }
            appbar_layout_delete.visibility == View.VISIBLE -> {
                EventBus.getDefault().post(
                    QuitSelectMode(
                        currentState,
                        true
                    )
                )
                hideDeleteAppBar()
                false
            }
            System.currentTimeMillis() - backTime < BACK_TIME -> {
                true
            }
            else -> {
                backTime = System.currentTimeMillis()
                XToast.info(getString(R.string.BackAgainToExit))
                false
            }
        }
    }

    private fun quitSearchMode() {
        //清空输入，并隐藏输入框
        searchView.setQuery("", false)
        searchView.isIconified = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onModifyPassword(modifyPassword: ModifyPassword) {
        finish()
        startActivity(
            Intent(
                this@MainActivity,
                LoginActivity::class.java
            )
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecyclerViewScrollStateChange(recyclerViewScrollStateChange: RecyclerViewScrollStateChange) {
        if (recyclerViewScrollStateChange.state == MyOnScrollListener.STATE.SCROLL_UP) {
            showBottomNavigationView()
        } else if (recyclerViewScrollStateChange.state == MyOnScrollListener.STATE.SCROLL_DOWN) {
            hideBottomNavigationView()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSelecting(selecting: Selecting) {
        updateDeleteAppBar(selecting.count, selecting.totalCount)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStartSelect(startSelect: StartSelect) {
        updateDeleteAppBar(startSelect.count, startSelect.totalCount)
        hideBottomNavigationView()
        showDeleteAppBar()
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
    }

    private fun hideDeleteAppBar() {
        if (appbar_layout_delete.visibility != View.GONE) {
            appbar_layout_delete.visibility = View.GONE
            appbar_layout_delete.startAnimation(AnimationUtil.moveToViewTop())
        }
    }

    private fun showDeleteAppBar() {
        if (appbar_layout_delete.visibility != View.VISIBLE) {
            appbar_layout_delete.visibility = View.VISIBLE
            appbar_layout_delete.startAnimation(AnimationUtil.moveToViewLocationFromTop())
        }
    }

    private fun showBottomNavigationView() {
        if (!isSearchMode) {
            if (bottomNavigationView.visibility != View.VISIBLE) {
                bottomNavigationView.visibility = View.VISIBLE
                bottomNavigationView.startAnimation(
                    AnimationUtil.moveToViewLocationFromBottomQuickly()
                )
            }
        }
    }

    private fun hideBottomNavigationView() {
        if (bottomNavigationView.visibility != View.GONE) {
            bottomNavigationView.visibility = View.GONE
            bottomNavigationView.startAnimation(
                AnimationUtil.moveToViewBottomQuickly()
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onQuitSelectMode(quitSelectMode: QuitSelectMode) {
        //防止死循环
        if (!quitSelectMode.fromActivity) {
            showBottomNavigationView()
            hideDeleteAppBar()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCurrentStateChange(currentStateChange: CurrentStateChange) {
        currentState = currentStateChange.currentState
        textView_main_state.text = when (currentState) {
            0 -> getString(R.string.NewData)
            1 -> getString(R.string.Processing)
            2 -> getString(R.string.Done)
            3 -> SharedPreferencesUtil.getSettingPreferences().getString(
                "show_text",
                getString(R.string.DrawerShowText)
            )
            else -> {
                ""
            }
        }
        if (currentState != 3) {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            textView_main_order_time_title.text = when (currentState) {
                0 -> getString(R.string.OrderByTime)
                1 -> getString(R.string.OrderByOrderTime)
                else -> getString(R.string.OrderByRepairTime)
            }
            linearLayout_main_settings.visibility = View.VISIBLE
            updateDrawerSelectedTitles()
        } else {
            if (SharedPreferencesUtil.getSettingPreferences().getBoolean("unlock", true)) {
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            linearLayout_main_settings.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskMakeOver(taskMakeOver: TaskMakeOver) {
        if (isSearchMode) {
            if (taskMakeOver.whichState == -1) {
                //更新
                searchDataList.remove(taskMakeOver.data)
                searchDataList.add(taskMakeOver.position, taskMakeOver.data)
                searchTaskAdapter.notifyItemChanged(taskMakeOver.position)
            }
        }
    }

    private fun updateDrawerSelectedTitles() {
        updateRadioGroup()
        val drawerNorthString = SharedPreferencesUtil.getSettingPreferences()
            .getString("drawer_north_$currentState", null)
        val localSelectedTitlesNorth = if (drawerNorthString == null) {
            Data.GET_LOCALS_N()
        } else {
            GsonUtil.parseJsonArray2List(drawerNorthString, String::class.java)
        }
        val drawerSouthString = SharedPreferencesUtil.getSettingPreferences()
            .getString("drawer_south_$currentState", null)
        val localSelectedTitlesSouth = if (drawerSouthString == null) {
            Data.GET_LOCALS_S()
        } else {
            GsonUtil.parseJsonArray2List(drawerSouthString, String::class.java)
        }
        selectedTitleNorth.clear()
        selectedTitleSouth.clear()
        if (currentState == 0) {
            if (App.getUser().haveWholeSchoolAccess()) {
                selectedTitleNorth.addAll(localSelectedTitlesNorth)
                selectedTitleSouth.addAll(localSelectedTitlesSouth)
            } else {
                if (App.getUser().whichGroup == 0) {
                    //北区隐藏南区的筛选
                    linearLayout_main_south.visibility = View.GONE
                    selectedTitleNorth.addAll(localSelectedTitlesNorth)
                } else {
                    linearLayout_main_north.visibility = View.GONE
                    selectedTitleSouth.addAll(localSelectedTitlesSouth)
                }
            }
        } else {
            linearLayout_main_north.visibility = View.VISIBLE
            linearLayout_main_south.visibility = View.VISIBLE
            selectedTitleNorth.addAll(localSelectedTitlesNorth)
            selectedTitleSouth.addAll(localSelectedTitlesSouth)
        }
        checkableAdapterNorth.updateBooleanArrayAfterSelectedTitles()
        checkableAdapterSouth.updateBooleanArrayAfterSelectedTitles()
    }

    private fun updateRadioGroup() {
        if (SharedPreferencesUtil.getSettingPreferences().getBoolean(
                "order_time_desc_$currentState",
                currentState != 0
            )
        ) {
            radioGroup_main.check(R.id.radioButton_desc)
        } else {
            radioGroup_main.check(R.id.radioButton_asc)
        }
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return 0
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_main_logout -> {
                logout()
                true
            }
            R.id.menu_main_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.menu_main_modify_password -> {
                ModifyPasswordDialog.with(this).show()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun logout() {
        MiPushClient.unsubscribe(this, "0", null)
        MiPushClient.unsubscribe(this, "1", null)
        getSharedPreferences(LoginActivity.LOGIN_INFO, Context.MODE_PRIVATE).edit().also {
            it.putBoolean(LoginActivity.AUTO_LOGIN_KEY, false)
            it.apply()
        }
        finish()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun shouldInit(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfos = am.runningAppProcesses as List<ActivityManager.RunningAppProcessInfo>
        val mainProcessName = applicationInfo.processName
        val myPid = Process.myPid()
        processInfos.forEach {
            if (it.pid == myPid && mainProcessName == it.processName) {
                return true
            }
        }
        return false
    }

    private fun initMiPushSubscribes() {
        if (App.getUser().haveWholeSchoolAccess()) {
            MiPushClient.subscribe(XFrame.getContext(), "0", null)
            MiPushClient.subscribe(XFrame.getContext(), "1", null)
        } else {
            if (App.getUser().whichGroup == 0) {
                MiPushClient.subscribe(XFrame.getContext(), "0", null)
                MiPushClient.unsubscribe(XFrame.getContext(), "1", null)
            } else {
                MiPushClient.subscribe(XFrame.getContext(), "1", null)
                MiPushClient.unsubscribe(XFrame.getContext(), "0", null)
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        initWelcomeDayDPPhoto()
        mainPagerAdapter = MainPagerAdapter(supportFragmentManager, lifecycle)
        searchTaskAdapter =
            TaskAdapter(this, App.getUser().haveDeleteAccess(), searchDataList, this)
    }

    private fun initWelcomeDayDPPhoto() {
        if (intent != null) {
            val dayDP = intent.getBooleanExtra(DAY_DP_STATE_KEY, DAY_DP_STATE_DEFAULT_VALUE)
            if (dayDP) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                frameLayout_main_welcome.visibility = View.VISIBLE
                class MyHandler : Handler() {
                    override fun handleMessage(msg: Message) {
                        if (msg.what == 0) {
                            button_main_skip.text =
                                String.format(getString(R.string.Skip), cnt--)
                            postDelayed({
                                if (cnt > 0) {
                                    sendEmptyMessage(0)
                                } else {
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                                    if (!animation.hasStarted()) {
                                        frameLayout_main_welcome.startAnimation(animation)
                                    }
                                }
                            }, 1000)
                        } else super.handleMessage(msg)
                    }
                }

                val myHandler = MyHandler()
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        frameLayout_main_welcome.visibility = View.GONE
                        initPermissions()
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
                val currentDate = XDateUtils.getCurrentDate(getString(R.string.DatePattern))
                button_main_skip.setOnClickListener {
                    cnt = 0
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    if (!animation.hasStarted()) {
                        frameLayout_main_welcome.startAnimation(animation)
                    }
                }
                Glide.with(this)
                    .load(
                        String.format(
                            "%s?path=/opt/dayDP/&fileName=%s.jpg",
                            HttpUtil.Urls.File.DOWNLOAD,
                            currentDate
                        )
                    )
                    .error(R.drawable.xloading_error)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar_main_welcome.visibility = View.GONE
                            imageView_main_welcome.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            cnt = 0
                            myHandler.sendEmptyMessage(0)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar_main_welcome.visibility = View.GONE
                            myHandler.sendEmptyMessage(0)
                            return false
                        }
                    }).into(imageView_main_welcome)
            } else {
                initPermissions()
                frameLayout_main_welcome.visibility = View.GONE
            }
        }
    }

    private fun initPermissions() {
        val firstRequestPermissions = SharedPreferencesUtil.getSettingPreferences().getBoolean(
            "first_request_permissions",
            true
        )
        if (firstRequestPermissions) {
            val dialog = AlertDialog.Builder(this@MainActivity)
                .setTitle(getString(R.string.FirstOpenTipTitle))
                .setCancelable(false)
                .setPositiveButton(R.string.Confirm) { _, _ -> }
                .setMessage(R.string.FirstOpenTipMessage)
                .create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    dialog.cancel()
                    requestPermissions()
                    SharedPreferencesUtil.getSettingPreferences().edit().putBoolean(
                        "first_request_permissions",
                        false
                    ).apply()
                }
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        XPermission.requestPermissions(this,
            REQUEST_CODE_PERMISSION,
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            object : XPermission.OnPermissionListener {
                override fun onPermissionGranted() {
                    if (shouldInit()) {
                        MiPushClient.registerPush(this@MainActivity, APP_ID, APP_KEY)
                    }
                    initMiPushSubscribes()
                    if (SharedPreferencesUtil.getSettingPreferences().getBoolean(
                            "first_show_tap_target_view", true
                        )
                    ) {
                        SharedPreferencesUtil.getSettingPreferences().edit().putBoolean(
                            "first_show_tap_target_view", false
                        ).apply()
                        TapTargetSequence(this@MainActivity).targets(
                            TapTarget.forToolbarNavigationIcon(
                                toolbar,
                                "报修单筛选",
                                "独立保存未处理，处理中，已维修三个界面的检索条件\n如果没有报修单记得看一下是不是没有选择园区"
                            ).cancelable(false),
                            TapTarget.forToolbarMenuItem(
                                toolbar,
                                R.id.menu_main_search,
                                "搜索",
                                "根据输入智能检索符合条件的报修单"
                            ).cancelable(false),
                            TapTarget.forToolbarOverflow(
                                toolbar,
                                "更多操作",
                                "修改密码、退出登录、关于"
                            ).cancelable(false)
                        ).listener(object : TapTargetSequence.Listener {
                            override fun onSequenceCanceled(lastTarget: TapTarget?) {
                                //ignore
                            }

                            override fun onSequenceFinish() {
                                XToast.custom(
                                    "更多功能请自行探索，祝您使用愉快！",
                                    ContextCompat.getColor(this@MainActivity, R.color.colorPrimary),
                                    5000
                                )
                            }

                            override fun onSequenceStep(
                                lastTarget: TapTarget?,
                                targetClicked: Boolean
                            ) {
                                //ignore
                            }
                        }).start()
                    }
                }

                override fun onPermissionDenied() {
                    XPermission.showTipsDialog(this@MainActivity)
                }
            })
    }

    override fun initView() {
        recyclerView_main_search.layoutManager = WrapContentLinearLayoutManager(this)
        recyclerView_main_search.adapter = searchTaskAdapter
        recyclerView_main_search.addOnScrollListener(MyOnScrollListener(object :
            MyOnScrollListener.IStateChangeListener {
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
        }))
        fab_scroll_to_top.setOnClickListener { recyclerView_main_search.smoothScrollToPosition(0) }
        view_pager.adapter = mainPagerAdapter
        view_pager.isUserInputEnabled = false
        view_pager.offscreenPageLimit = 1
        bottomNavigationView.setOnNavigationItemSelectedListener {
            return@setOnNavigationItemSelectedListener when (it.itemId) {
                R.id.navigation_new_task -> {
                    EventBus.getDefault().post(QuitSelectMode(currentState, true))
                    view_pager.currentItem = 0
                    true
                }
                R.id.navigation_processing -> {
                    EventBus.getDefault().post(QuitSelectMode(currentState, true))
                    view_pager.currentItem = 1
                    true
                }
                R.id.navigation_my_self -> {
                    EventBus.getDefault().post(QuitSelectMode(currentState, true))
                    view_pager.currentItem = 2
                    true
                }
                else -> {
                    false
                }
            }
        }
        view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        EventBus.getDefault().post(CurrentStateChange(0))
                        bottomNavigationView.selectedItemId = R.id.navigation_new_task
                    }
                    1 -> {
                        EventBus.getDefault().post(GetSecondFragmentCurrentState())
                        bottomNavigationView.selectedItemId = R.id.navigation_processing
                    }
                    2 -> {
                        EventBus.getDefault().post(CurrentStateChange(3))
                        showBottomNavigationView()
                        bottomNavigationView.selectedItemId = R.id.navigation_my_self
                    }
                    else -> {
                        super.onPageSelected(position)
                    }
                }

            }
        })
        initDrawerLayout()
        initDeleteAppBar()
        updateDrawerSelectedTitles()
    }

    private fun initDrawerLayout() {
        textView_main_sub_title.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.QQGroupUrl))))
        }
        if (SharedPreferencesUtil.getSettingPreferences().getBoolean("show_host", true)) {
            textView_main_host.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.HOST))))
                App.copyToClipboard(this, getString(R.string.HOST))
            }
        } else {
            textView_main_host.visibility = View.GONE
        }
        updateRadioGroup()
        radioGroup_main.setOnCheckedChangeListener { _, checkedId ->
            SharedPreferencesUtil.getSettingPreferences().edit()
                .putBoolean("order_time_desc_$currentState", checkedId == R.id.radioButton_desc)
                .apply()
        }
        checkableAdapterNorth = CheckableAdapter(
            Data.GET_LOCALS_N().toTypedArray(),
            object : CheckableAdapter.IOnCheckedChangeListener {
                override fun onCheckChanged(selectAll: Boolean, selectedTitles: ArrayList<String>) {
                    if (selectAll) {
                        textView_main_select_all_north.text = getString(R.string.CancelSelectAll)
                    } else {
                        textView_main_select_all_north.text = getString(R.string.SelectAll)
                    }
                    SharedPreferencesUtil.getSettingPreferences().edit().also {
                        it.putString(
                            "drawer_north_$currentState",
                            GsonUtil.getInstance().toJson(selectedTitles)
                        )
                        it.apply()
                    }
                    XLog.d("selected_titles_state($currentState) = $selectedTitles")
                }
            }, selectedTitleNorth
        )
        checkableAdapterSouth = CheckableAdapter(
            Data.GET_LOCALS_S().toTypedArray(),
            object : CheckableAdapter.IOnCheckedChangeListener {
                override fun onCheckChanged(selectAll: Boolean, selectedTitles: ArrayList<String>) {
                    if (selectAll) {
                        textView_main_select_all_south.text = getString(R.string.CancelSelectAll)
                    } else {
                        textView_main_select_all_south.text = getString(R.string.SelectAll)
                    }
                    SharedPreferencesUtil.getSettingPreferences().edit().also {
                        it.putString(
                            "drawer_south_$currentState",
                            GsonUtil.getInstance().toJson(selectedTitles)
                        )
                        it.apply()
                    }
                    XLog.d("selected_titles_state($currentState) = $selectedTitles")
                }
            }, selectedTitleSouth
        )
        if (checkableAdapterNorth.isSelectAll()) {
            textView_main_select_all_north.text = getString(R.string.CancelSelectAll)
        } else {
            textView_main_select_all_north.text = getString(R.string.SelectAll)
        }
        if (checkableAdapterSouth.isSelectAll()) {
            textView_main_select_all_south.text = getString(R.string.CancelSelectAll)
        } else {
            textView_main_select_all_south.text = getString(R.string.SelectAll)
        }
        recyclerView_main_north.layoutManager = WrapContentLinearLayoutManager(this)
        recyclerView_main_north.adapter = checkableAdapterNorth
        recyclerView_main_south.layoutManager = WrapContentLinearLayoutManager(this)
        recyclerView_main_south.adapter = checkableAdapterSouth
        linearLayout_main_north.setOnClickListener { checkableAdapterNorth.toggleSelectAll() }
        linearLayout_main_south.setOnClickListener { checkableAdapterSouth.toggleSelectAll() }
        val slideWithStart =
            SharedPreferencesUtil.getSettingPreferences().getBoolean("slide_with_start", true)
        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (slideWithStart) {
                    //获得侧滑菜单的宽度
                    val startView = drawer_layout.getChildAt(1)
                    val centerView = drawer_layout.getChildAt(0)
                    val startViewWidth = startView.measuredWidth
                    //根据滑动百分比计算内容部分应该向右边移动的距离
                    val marginLeft = (startViewWidth * slideOffset)
                    centerView.translationX = marginLeft
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                if (!isSearchMode) {
                    EventBus.getDefault().post(DrawerClose(currentState))
                }
            }

            override fun onDrawerOpened(drawerView: View) {
            }
        })
    }

    private fun initDeleteAppBar() {
        appbar_layout_delete.setPadding(
            0,
            App.getStatusHeight(this),
            0,
            0
        )
        toolbar_delete.setNavigationIcon(R.drawable.ic_close)
        toolbar_delete.setNavigationOnClickListener {
            if (isSearchMode) {
                searchTaskAdapter.quitSelect()
            } else {
                EventBus.getDefault().post(
                    QuitSelectMode(
                        currentState,
                        true
                    )
                )
            }
        }
        toolbar_delete.menu.findItem(R.id.menu_main_delete).setOnMenuItemClickListener {
            if (isSearchMode) {
                searchTaskAdapter.confirmSelect()
            } else {
                EventBus.getDefault().post(
                    ConfirmSelect(
                        currentState
                    )
                )
            }
            return@setOnMenuItemClickListener true
        }
        toolbar_delete.menu.findItem(R.id.menu_main_select_all).setOnMenuItemClickListener {
            if (it.title == getString(R.string.SelectAll)) {
                it.setTitle(R.string.ClearAll)
            } else {
                it.setTitle(R.string.SelectAll)
            }
            if (isSearchMode) {
                searchTaskAdapter.toggleSelectAOrClearAll()
            } else {
                EventBus.getDefault().post(
                    ToggleSelectOrClearAll(
                        currentState
                    )
                )
            }
            return@setOnMenuItemClickListener true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tool_bar_main, menu)
        val searchManager = XFrame.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu?.findItem(R.id.menu_main_search)?.actionView as SearchView
        searchView.setOnSearchClickListener { onStartSearch() }
        searchView.setOnCloseListener {
            onQuitSearch()
            false
        }
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(this)
        searchView.queryHint = getString(R.string.QueryHint)
        return true
    }

    private fun onQuitSearch() {
        isSearchMode = false
        showBottomNavigationView()
        frameLayout_main_search.visibility = View.GONE
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    private fun onStartSearch() {
        isSearchMode = true
        hideBottomNavigationView()
        frameLayout_main_search.visibility = View.VISIBLE
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        onSearchChange(query ?: "")
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        onSearchChange(newText ?: "")
        return true
    }

    private fun onSearchChange(s: String) {
        val oldSize = searchDataList.size
        inputString = s
        searchDataList.clear()
        searchTaskAdapter.notifyItemRangeRemoved(0, oldSize)
        if (s.isNotEmpty()) {
            val dataQueryBuilder: QueryBuilder<Data> = App.daoSession.dataDao.queryBuilder()
            searchDataList.addAll(
                if ("有图" == s) {
                    dataQueryBuilder.where(DataDao.Properties.Photo.notEq("")).build().list()
                } else {
                    val s1 = "%$s%"
                    try {
                        val idOrTelOrQq = Integer.valueOf(s)
                        dataQueryBuilder.whereOr(
                            DataDao.Properties.Id.eq(idOrTelOrQq),
                            DataDao.Properties.Id.like(s1),
                            DataDao.Properties.Tel.eq(idOrTelOrQq),
                            DataDao.Properties.Tel.like(s1),
                            DataDao.Properties.Qq.eq(idOrTelOrQq),
                            DataDao.Properties.Qq.like(s1),
                            DataDao.Properties.Model.like(s1),
                            DataDao.Properties.Message.like(s1),
                            DataDao.Properties.RepairMessage.like(s1)
                        ).build().list()
                    } catch (e: NumberFormatException) {
                        dataQueryBuilder.whereOr(
                            DataDao.Properties.Name.like(s1),
                            DataDao.Properties.Local.like(s1),
                            DataDao.Properties.College.like(s1),
                            DataDao.Properties.Grade.like(s1),
                            DataDao.Properties.Model.like(s1),
                            DataDao.Properties.Message.like(s1),
                            DataDao.Properties.Repairer.like(s1),
                            DataDao.Properties.Mark.like(s1),
                            DataDao.Properties.Service.like(s1),
                            DataDao.Properties.RepairMessage.like(s1)
                        ).build().list()
                    }
                }
            )
            searchTaskAdapter.notifyItemRangeInserted(0, searchDataList.size)
        }
    }

    override fun onItemClick(position: Int, data: Data, cardView: CardView) {
        startActivityForResult(
            Intent(this, DetailsActivity::class.java).putExtra(
                DetailsActivity.ID_KEY,
                data.id
            ).putExtra(DetailsActivity.POSITION_KEY, position)
            , VIEW_TASK_FROM_SEARCH
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == VIEW_TASK_FROM_SEARCH) {
                //不需要判断which == -1
                if (searchDataList.isNotEmpty()) {
                    val position = data.getIntExtra(DetailsActivity.POSITION_KEY, -1)
                    if (position != -1) {
                        val fromJson = GsonUtil.getInstance().fromJson(
                            data.getStringExtra(DetailsActivity.DATA_KEY),
                            Data::class.java
                        )
                        //更新position的data
                        searchDataList.removeAt(position)
                        searchDataList.add(position, fromJson)
                        searchTaskAdapter.notifyItemChanged(position)
                        updateAllList()
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateAllList() {
        //更新其余所有list
        EventBus.getDefault().post(DrawerClose(0))
        EventBus.getDefault().post(DrawerClose(1))
        EventBus.getDefault().post(DrawerClose(2))
        EventBus.getDefault().post(MySelfTaskModified(1))
        EventBus.getDefault().post(MySelfTaskModified(2))
    }

    override fun onStartSelect(count: Int) {
        onStartSelect(StartSelect(count, searchDataList.size))
    }

    override fun onSelect(count: Int) {
        updateDeleteAppBar(count, searchDataList.size)
    }

    override fun onConfirmSelect(count: Int, sparseBooleanArray: SparseBooleanArray) {
        val builder = StringBuilder()
        val ids: ArrayList<Long> = ArrayList()
        val needToDelete: ArrayList<Data> = ArrayList()
        for (i in 0 until sparseBooleanArray.size()) {
            val key = sparseBooleanArray.keyAt(i)
            if (sparseBooleanArray[key]) {
                val data = searchDataList[key]
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
                        LoadingDialog.with(this@MainActivity).cancel()
                        val deleted = ArrayList<Data>().also {
                            it.addAll(searchDataList)
                            it.removeAll(needToDelete)
                        }
                        val oldSize = searchDataList.size
                        searchDataList.clear()
                        searchTaskAdapter.notifyItemRangeRemoved(0, oldSize)
                        searchDataList.addAll(deleted)
                        searchTaskAdapter.notifyItemRangeInserted(0, searchDataList.size)
                        searchTaskAdapter.clearAllSelected()
                        updateAllList()
                        XToast.success(getString(R.string.DeleteSuccess))
                    }

                    override fun onFailure(error: String) {
                        LoadingDialog.with(this@MainActivity).cancel()
                        XToast.error("${getString(R.string.DeleteFail)}\n$error")
                    }
                })
            }.show()
    }

    override fun onQuitSelectMode() {
        recyclerView_main_search.stopScroll()
        onQuitSelectMode(QuitSelectMode(-1, false))
    }

    override fun onConfirmClick(position: Int, data: Data) {
        order(data, position)
    }

    private fun order(data: Data, position: Int) {
        LoadingDialog.with(this).show()
        Data.queryById(data.id, object : HttpUtil.HttpUtilCallBack<Data> {
            override fun onResponse(result: Data) {
                LoadingDialog.with(this@MainActivity).cancel()
                if (result.state.toInt() == 0) {
                    val booleans = booleanArrayOf(false)
                    val dataBackUpString = result.toString()
                    AlertDialog.Builder(this@MainActivity).setCancelable(false)
                        .setTitle("${result.local} - ${result.id}")
                        .setNegativeButton(R.string.Cancel, null)
                        .setPositiveButton(
                            R.string.Order
                        ) { _, _ ->
                            LoadingDialog.with(this@MainActivity).show()
                            result.orderDate = System.currentTimeMillis()
                            result.repairer = App.getUser().userName
                            result.state = 1.toShort()
                            result.modify(
                                dataBackUpString,
                                object : HttpUtil.HttpUtilCallBack<Data> {

                                    override fun onResponse(result: Data) {
                                        LoadingDialog.with(this@MainActivity).cancel()
                                        if (booleans[0]) {
                                            Data.openQq(result.qq)
                                        }
                                        XToast.success(getString(R.string.OrderSuccess))
                                        //更新position的data
                                        searchDataList.removeAt(position)
                                        searchDataList.add(position, result)
                                        searchTaskAdapter.notifyItemChanged(position)
                                        EventBus.getDefault().post(TaskOrdered(0, result, null))
                                    }

                                    override fun onFailure(error: String) {
                                        LoadingDialog.with(this@MainActivity).cancel()
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
                    LoadingDialog.with(this@MainActivity).cancel()
                    XToast.warning("唉呀，有人抢先了...\n该报修单已被 " + result.repairer + " 处理")
//                    EventBus.getDefault().post(
//                        TaskOrdered(
//                            state,
//                            result,
//                            position
//                        )
//                    )
                }
            }

            override fun onFailure(error: String) {
                LoadingDialog.with(this@MainActivity).cancel()
                XToast.error("${getString(R.string.OrderFail)}\n$error")
            }
        })
    }

    companion object {
        const val DAY_DP_STATE_KEY = "dayDP"
        const val DAY_DP_STATE_DEFAULT_VALUE = false
        private const val BACK_TIME = 2000
        const val VIEW_TASK_FROM_SEARCH = 920
        private const val APP_ID = "2882303761518324785"
        private const val APP_KEY = "5241832490785"
        private const val REQUEST_CODE_PERMISSION = 1208
    }

}