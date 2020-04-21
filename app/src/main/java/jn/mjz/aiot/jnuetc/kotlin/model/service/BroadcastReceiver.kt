package jn.mjz.aiot.jnuetc.kotlin.model.service

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.xiaomi.mipush.sdk.*
import com.youth.xframe.XFrame
import com.youth.xframe.utils.XAppUtils
import com.youth.xframe.utils.log.XLog
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.DrawerClose
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.MySelfTaskModified
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.NewDataFromCloud
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.NewVersionFromCloud
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.view.activity.detail.DetailsActivity
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.datachangelog.DataChangeLogFragment.Companion.sortLogByTimeDesc
import org.greenrobot.eventbus.EventBus

class BroadcastReceiver : PushMessageReceiver() {
    private var mRegId: String? = null
    private val mResultCode: Long = -1
    private val mReason: String? = null
    private val mCommand: String? = null
    private var mMessage: String? = null
    private var mTopic: String? = null
    private var mAlias: String? = null
    private var mUserAccount: String? = null
    private var mStartTime: String? = null
    private var mEndTime: String? = null

    /**
     * 透传消息到达手机端后，SDK会将消息通过广播方式传给AndroidManifest中注册的PushMessageReceiver的子类的[onReceivePassThroughMessage]
     *
     * @param context
     * @param message
     */
    override fun onReceivePassThroughMessage(
        context: Context,
        message: MiPushMessage
    ) {
        XLog.json(GsonUtil.getInstance().toJson(message))
        mMessage = message.content
        if (!TextUtils.isEmpty(message.topic)) {
            mTopic = message.topic
        } else if (!TextUtils.isEmpty(message.alias)) {
            mAlias = message.alias
        } else if (!TextUtils.isEmpty(message.userAccount)) {
            mUserAccount = message.userAccount
        }
    }

    /**
     * 用户点击之后再传给您的PushMessageReceiver的子类的[onNotificationMessageClicked]
     *
     * @param context
     * @param message
     */
    override fun onNotificationMessageClicked(
        context: Context,
        message: MiPushMessage
    ) {
        val data: Data = GsonUtil.getInstance().fromJson(message.content, Data::class.java)
        val sorted = sortLogByTimeDesc(data.dataChangeLogs)
        data.dataChangeLogs.clear()
        data.dataChangeLogs.addAll(sorted)
        var intent: Intent
        var currentContext: Context
        try {
            currentContext = XFrame.getContext()
            intent = Intent(currentContext, DetailsActivity::class.java).putExtra(
                DetailsActivity.ID_KEY,
                data.id
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } catch (e: NullPointerException) {
            XLog.d("XFrame.getContext() == null")
            currentContext = context
            intent =
                Intent(
                    currentContext,
                    DetailsActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        currentContext.startActivity(intent)
        if (!TextUtils.isEmpty(message.topic)) {
            mTopic = message.topic
        } else if (!TextUtils.isEmpty(message.alias)) {
            mAlias = message.alias
        } else if (!TextUtils.isEmpty(message.userAccount)) {
            mUserAccount = message.userAccount
        }
    }

    /**
     * 通知消息到达时会到达PushMessageReceiver子类的onNotificationMessageArrived方法
     * 对于应用在前台时不弹出通知的通知消息，SDK会将消息通过广播方式传给AndroidManifest中注册的PushMessageReceiver的子类的
     * [onNotificationMessageArrived]
     *
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃ {
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "arrived": true,
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "content": "",
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "description": "3.0.5 测试",
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "extra": {
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃         "channel_name": "新版本通知",
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃         "web_uri": "https:\/\/www.jiangnan-dzjsb.club",
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃         "notify_effect": "3",
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃         "__m_ts": "1582439566659",
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃         "channel_id": "new_version"
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     },
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "isNotified": false,
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "messageId": "tcm53866582439566240iZ",
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "messageType": 2,
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "notifyId": 21,
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "notifyType": 7,
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "passThrough": 0,
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "title": "有新版本啦(点我直接打开浏览器下载)",
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃     "topic": "**ALL**"
    2020-02-23 14:32:51.723 19573-19673/jn.mjz.aiot.jnuetc.kotlin D/XFrame: ┃ }
     *
     * @param context
     * @param message
     */
    override fun onNotificationMessageArrived(
        context: Context,
        message: MiPushMessage
    ) {
        XLog.json(GsonUtil.getInstance().toJson(message))
        if (message.extra["channel_id"] == "new_version") {
            if (XAppUtils.getVersionCode(context) < message.notifyId) {
                XLog.d("发现新版本")
                EventBus.getDefault()
                    .post(NewVersionFromCloud(message.extra["web_uri"], message.description))
            } else {
                val newData: Data? =
                    GsonUtil.getInstance().fromJson(message.content, Data::class.java)
                if (newData != null) {
                    val localData: Data? = App.daoSession.dataDao.load(newData.id)
                    if (localData == null) {
                        XLog.d("新增报修单")
                        App.daoSession.dataDao.insert(newData)
                        EventBus.getDefault().post(NewDataFromCloud(newData))
                    } else {
                        if (message.extra["user_name"].equals(App.getUser().userName)) {
                            //本地操作，无需修改
                            XLog.d("报修单信息更新（本地操作）")
                        } else {
                            XLog.d("报修单信息更新")
                            App.daoSession.dataDao.update(newData)
                            if (localData.state == newData.state) {
                                EventBus.getDefault().post(DrawerClose(localData.state.toInt()))
                            } else {
                                EventBus.getDefault().post(DrawerClose(localData.state.toInt()))
                                EventBus.getDefault().post(DrawerClose(newData.state.toInt()))
                            }
                            EventBus.getDefault().post(MySelfTaskModified(1))
                            EventBus.getDefault().post(MySelfTaskModified(2))
                        }
                    }
                }
            }
        }
        mMessage = message.content
        if (!TextUtils.isEmpty(message.topic)) {
            mTopic = message.topic
        } else if (!TextUtils.isEmpty(message.alias)) {
            mAlias = message.alias
        } else if (!TextUtils.isEmpty(message.userAccount)) {
            mUserAccount = message.userAccount
        }
    }

    /**
     * 用来接收客户端向服务器发送命令消息后返回的响应
     *
     * @param context
     * @param message
     */
    override fun onCommandResult(
        context: Context,
        message: MiPushCommandMessage
    ) {
        val command = message.command
        val arguments = message.commandArguments
        val cmdArg1 =
            if (arguments != null && arguments.size > 0) arguments[0] else null
        val cmdArg2 =
            if (arguments != null && arguments.size > 1) arguments[1] else null
        if (MiPushClient.COMMAND_REGISTER == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mRegId = cmdArg1
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mAlias = cmdArg1
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mAlias = cmdArg1
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mTopic = cmdArg1
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mTopic = cmdArg1
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mStartTime = cmdArg1
                mEndTime = cmdArg2
            }
        }
    }

    /**
     * 用来接受客户端向服务器发送注册命令消息后返回的响应
     *
     * @param context
     * @param message
     */
    override fun onReceiveRegisterResult(
        context: Context,
        message: MiPushCommandMessage
    ) {
        val command = message.command
        val arguments = message.commandArguments
        val cmdArg1 =
            if (arguments != null && arguments.size > 0) arguments[0] else null
        val cmdArg2 =
            if (arguments != null && arguments.size > 1) arguments[1] else null
        if (MiPushClient.COMMAND_REGISTER == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mRegId = cmdArg1
            }
        }
    }
}