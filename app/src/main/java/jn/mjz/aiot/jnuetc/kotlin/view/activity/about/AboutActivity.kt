package jn.mjz.aiot.jnuetc.kotlin.view.activity.about

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.youth.xframe.utils.XAppUtils
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Version
import jn.mjz.aiot.jnuetc.kotlin.model.util.UpdateUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.UpdateUtil.IUpdateListener
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractActivity
import jn.mjz.aiot.jnuetc.kotlin.view.custom.LoadingDialog
import kotlinx.android.synthetic.main.activity_about.*
import java.util.*


/**
 * AboutActivity
 *
 * @author qq1962247851
 * @date 2020/2/20 19:22
 */
class AboutActivity : AbstractActivity() {
    override fun preFinish(): Boolean {
        return true
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return R.menu.tool_bar_about
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_about
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_about_history) {
            history()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun initData(savedInstanceState: Bundle?) {
        UpdateUtil.checkForUpdate(object : IUpdateListener {

            override fun haveNewVersion(currentVersion: Version, newVersion: Version) {
                textView_about_version.text = java.lang.String.format(
                    Locale.getDefault(),
                    "版本：%s（有新版本%s）",
                    XAppUtils.getVersionName(this@AboutActivity),
                    newVersion.version
                )
            }

            override fun develop() {
                textView_about_version.text = String.format(
                    "版本：%s（当前是开发版本）",
                    XAppUtils.getVersionName(this@AboutActivity)
                )
            }

            override fun error(error: String) {
                textView_about_version.text = error
            }

            override fun noUpdate(currentVersion: Version) {
                textView_about_version.text = String.format(
                    "版本：%s（当前是最新版本）",
                    XAppUtils.getVersionName(this@AboutActivity)
                )
                textView_about_current_log.text =
                    String.format("当前版本更新日志：\n%s", currentVersion.message)
            }

        })

    }

    override fun initView() {
        linearLayout_about_feedback.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getText(R.string.FeedbackAddress).toString())
                )
            )
        }
        linearLayout_about_git.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getText(R.string.githubAddress).toString())
                )
            )
        }
        textView_about_version.setOnClickListener {
            UpdateUtil.checkForUpdate(false, this, null)
        }
    }

    private fun history() {
        LoadingDialog.with(this).show()
        UpdateUtil.checkHistory(object : UpdateUtil.IHistoryListener {
            override fun success(historyString: String) {
                LoadingDialog.with(this@AboutActivity).cancel()
                AlertDialog.Builder(this@AboutActivity).setMessage(historyString)
                    .setTitle(R.string.HistoryLog).setPositiveButton(R.string.Confirm, null).show()
            }

            override fun error(error: String) {
                LoadingDialog.with(this@AboutActivity).cancel()
                XToast.error("${getString(R.string.RefreshFail)}\n$error")
            }
        })
    }

}