package jn.mjz.aiot.jnuetc.kotlin.model.application

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.xiaomi.channel.commonutils.logger.LoggerInterface
import com.xiaomi.mipush.sdk.Logger
import com.youth.xframe.XFrame
import com.youth.xframe.base.XApplication
import com.youth.xframe.utils.http.HttpCallBack
import com.youth.xframe.utils.http.XHttp
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.entity.MingJu
import jn.mjz.aiot.jnuetc.kotlin.model.entity.MyResponse
import jn.mjz.aiot.jnuetc.kotlin.model.entity.User
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DaoMaster
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DaoSession
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil
import jn.mjz.aiot.jnuetc.kotlin.model.custom.OkHttpEngine
import jn.mjz.aiot.jnuetc.kotlin.model.util.SharedPreferencesUtil
import jn.mjz.aiot.jnuetc.kotlin.view.activity.login.LoginActivity
import java.util.*

/**
 * @author qq1962247851
 * @date 2020/02/17 21:51
 */
class App : XApplication() {

    override fun onCreate() {
        super.onCreate()
        XFrame.initXLog().isDebug = DEBUG_MODE
        XFrame.initXHttp(OkHttpEngine())
        daoSession =
            DaoMaster(DaoMaster.DevOpenHelper(this, DB_NAME).writableDatabase).newSession()
        val newLogger: LoggerInterface = object : LoggerInterface {
            override fun setTag(tag: String) {
                // ignore
            }

            override fun log(content: String, t: Throwable) {
                Log.d(TAG, content, t)
            }

            override fun log(content: String) {
                Log.d(TAG, content)
            }
        }
        Logger.setLogger(this, newLogger)
    }

    companion object {
        const val REQUEST_EXTERNAL_STROGE = 123
        const val DB_NAME = "JNUETC_KOTLIN.db"
        private const val TAG = "App"
        const val DEBUG_MODE = true
        lateinit var daoSession: DaoSession
        /**
         * 获取当前user
         *
         * @return user
         */
        @JvmStatic
        fun getUser(): User {
            return GsonUtil.getInstance().fromJson(
                XFrame.getContext().getSharedPreferences(
                    LoginActivity.LOGIN_INFO,
                    Context.MODE_PRIVATE
                ).getString(LoginActivity.USER_JSON_KEY, null), User::class.java
            )
        }

        /**
         * 初始化Activity标题栏的副标题，设置名句
         *
         * @param toolbar  toolbar
         * @param activity activity
         */
        @JvmStatic
        fun initToolbar(
            toolbar: Toolbar?,
            activity: AppCompatActivity
        ) {
            if (toolbar != null) {
                val showMingJu =
                    SharedPreferencesUtil.getSettingPreferences().getBoolean("show_ming_ju", true)
                if (showMingJu) {
                    val params = HashMap<String, Any>(1)
                    val topic = SharedPreferencesUtil.getSettingPreferences().getString(
                        "ming_ju_topic",
                        "随机"
                    )!!
                    if (topic != "随机") {
                        params["topic"] = topic
                    }
                    XHttp.obtain().post(
                        HttpUtil.Urls.MingJuURL.GET_MING_JU,
                        params,
                        object : HttpCallBack<MyResponse>() {
                            override fun onSuccess(myResponse: MyResponse) {
                                if (myResponse.error == MyResponse.SUCCESS) {
                                    val result =
                                        GsonUtil.getInstance()
                                            .fromJson(
                                                myResponse.bodyJson,
                                                MingJu::class.java
                                            )
                                    activity.supportActionBar!!.subtitle = result.content
                                    toolbar.setOnClickListener {
                                        val builder =
                                            AlertDialog.Builder(activity)
                                        val message =
                                            "来自：《" + result.shiName + "》" + "\n" +
                                                    "作者：" + result.author + "\n" +
                                                    "话题：" + result.topic
                                        builder.setTitle(result.content)
                                            .setMessage(message)
                                            .setNeutralButton(
                                                "换一句"
                                            ) { _: DialogInterface?, _: Int ->
                                                initToolbar(
                                                    toolbar,
                                                    activity
                                                )
                                            }
                                            .setPositiveButton("关闭", null)
                                            .setNegativeButton(
                                                "复制诗句"
                                            ) { _: DialogInterface?, _: Int ->
                                                copyToClipboard(
                                                    activity,
                                                    result.content
                                                )
                                            }
                                        builder.show()
                                    }
                                } else {
                                    XToast.error("名句获取失败\n" + myResponse.msg)
                                }
                            }

                            override fun onFailed(error: String) {
                                XToast.error("名句获取失败\n$error")
                            }
                        })
                }
            }
        }

        /**
         * 文字复制到剪切板
         *
         * @param context context
         * @param text    要复制的文字
         */
        @JvmStatic
        fun copyToClipboard(context: Context, text: String?) {
            val systemService =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            systemService.setPrimaryClip(ClipData.newPlainText("text", text))
            XToast.success(String.format(XFrame.getString(R.string.CopyToClipboard), text))
        }

        /**
         * 获取状态栏高度
         *
         * @param activity activity
         * @return 高度
         */
        @JvmStatic
        fun getStatusHeight(activity: AppCompatActivity): Int {
            var statusHeight: Int
            val rect = Rect()
            activity.window.decorView
                .getWindowVisibleDisplayFrame(rect)
            statusHeight = rect.top
            if (0 == statusHeight) {
                val localClass: Class<*>
                try {
                    localClass = Class.forName("com.android.internal.R\$dimen")
                    val `object` = localClass.newInstance()
                    val height = localClass.getField("status_bar_height")[`object`]
                        .toString().toInt()
                    statusHeight = activity.resources
                        .getDimensionPixelSize(height)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return statusHeight
        }
    }

}