package jn.mjz.aiot.jnuetc.kotlin.view.custom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.youth.xframe.XFrame
import com.youth.xframe.base.ICallback
import com.youth.xframe.common.XActivityStack
import com.youth.xframe.utils.permission.XPermission
import com.youth.xframe.utils.statusbar.XStatusBar
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.AppThemeChange
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.NewVersionFromCloud
import jn.mjz.aiot.jnuetc.kotlin.model.util.SharedPreferencesUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * @author qq1962247851
 * @date 2020/1/29 18:22
 */
abstract class AbstractActivity : AppCompatActivity, ICallback {

    private var dataBandingEnable: Boolean = false

    constructor()
    constructor(dataBandingEnable: Boolean) {
        this.dataBandingEnable = dataBandingEnable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        loadTheme()
        super.onCreate(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        XActivityStack.getInstance().addActivity(this)
        if (layoutId > 0) {
            if (dataBandingEnable) {
                initData(savedInstanceState)
                initView()
                initDrawerLayout(initToolbar())
            } else {
                setContentView(layoutId)
                initDrawerLayout(initToolbar())
                initData(savedInstanceState)
                initView()
            }
        }
    }

    private fun initToolbar(): Toolbar? {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            XStatusBar.setColorNoTranslucent(
                this,
                ContextCompat.getColor(this, R.color.colorPrimary)
            )
            App.initToolbar(toolbar, this)
        }
        return toolbar
    }

    private fun initDrawerLayout(toolbar: Toolbar?) {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout == null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            toolbar?.setPadding(
                0,
                App.getStatusHeight(this),
                0,
                0
            )
            val toggle = ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
            XStatusBar.setTransparentForDrawerLayout(this, drawerLayout)
        }
    }

    /**
     * Android M 全局权限申请回调
     *
     * @param requestCode  requestCode
     * @param permissions  permissions
     * @param grantResults grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        XPermission.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewVersionFromCloud(newVersionFromCloud: NewVersionFromCloud) {
        if (newVersionFromCloud.url != null) {
            val dialog =
                AlertDialog.Builder(this).setCancelable(false).setTitle("发现新版本")
                    .setMessage(newVersionFromCloud.description).setPositiveButton("下载") { _, _ -> }
                    .create()
            dialog.show()
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(newVersionFromCloud.url)
                    )
                )
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppThemeChange(appThemeChange: AppThemeChange) {
        loadTheme()
        finish()
        startActivity(Intent(XFrame.getContext(), this.javaClass))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        XActivityStack.getInstance().finishActivity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            if (preFinish()) {
                super.onBackPressed()
            }
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout != null) {
            when {
                drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                drawerLayout.isDrawerOpen(GravityCompat.END) -> {
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                preFinish() -> {
                    super.onBackPressed()
                }
            }
        } else if (preFinish()) {
            super.onBackPressed()
        }
    }

    private fun loadTheme() {
        val themeArray = resources.getStringArray(R.array.app_theme_entries)
        when (SharedPreferencesUtil.getSettingPreferences().getString("theme", themeArray[0])) {
            themeArray[0] -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            themeArray[1] -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    /**
     * @return 是否完成所有操作，退出界面
     */
    abstract fun preFinish(): Boolean

    /**
     * @return 是否有右上角菜单
     */
    abstract fun getOptionsMenuId(menu: Menu?): Int

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (getOptionsMenuId(menu) > 0) {
            menuInflater.inflate(getOptionsMenuId(menu), menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

}