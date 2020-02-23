package jn.mjz.aiot.jnuetc.kotlin.view.custom

import android.content.Context
import android.content.DialogInterface
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import com.youth.xframe.utils.http.HttpCallBack
import com.youth.xframe.utils.http.XHttp
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.MyResponse
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.ModifyPassword
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.SharedPreferencesUtil
import jn.mjz.aiot.jnuetc.kotlin.view.activity.login.LoginActivity
import kotlinx.android.synthetic.main.modify_password.view.*
import org.greenrobot.eventbus.EventBus

/**
 * ModifyPasswordDialog
 *
 * @author qq1962247851
 * @date 2020/2/20 21:40
 */
class ModifyPasswordDialog(context: Context) : AlertDialog(context) {

    private var showPassword = false
    private val inflate = View.inflate(context, R.layout.modify_password, null)
    private var oldPassword = inflate.tiet_old_password.text.toString()
    private var newPassword1 = inflate.tiet_new_password.text.toString()
    private var newPassword2 = inflate.tiet_new_password_again.text.toString()

    override fun dismiss() {
        if (dialog != null) {
            dialog = null
        }
        super.dismiss()
    }

    override fun onBackPressed() {
        preFinish()
    }

    companion object {
        private var dialog: ModifyPasswordDialog? = null
        @JvmStatic
        fun with(context: Context): ModifyPasswordDialog {
            if (dialog == null) {
                dialog =
                    ModifyPasswordDialog(
                        context
                    )
            }
            return dialog!!
        }
    }

    init {
        setCancelable(false)
        setView(inflate)
        inflate.toolbar_modify_password.menu.findItem(R.id.menu_show_password)
            .setOnMenuItemClickListener {
                showPassword = !showPassword
                if (showPassword) {
                    it.setIcon(R.drawable.ic_visible_off)
                    it.setTitle(R.string.HidePassword)
                    inflate.tiet_old_password.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                    inflate.tiet_new_password.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                    inflate.tiet_new_password_again.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                } else {
                    //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    it.setIcon(R.drawable.ic_visible)
                    it.setTitle(R.string.ShowPassword)
                    inflate.tiet_old_password.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    inflate.tiet_new_password.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    inflate.tiet_new_password_again.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                }
                return@setOnMenuItemClickListener true
            }
        inflate.tiet_old_password.addTextChangedListener {
            oldPassword = it.toString()
            if (it.toString().length in 7..18) {
                if (newPassword1 == oldPassword) {
                    inflate.tiet_old_password.error =
                        context.getString(R.string.NewPasswordEqualOld)
                    inflate.tiet_new_password.error =
                        context.getString(R.string.NewPasswordEqualOld)
                    inflate.tiet_new_password_again.error =
                        context.getString(R.string.NewPasswordEqualOld)
                } else {
                    inflate.tiet_old_password.error = null
                    inflate.tiet_new_password.error = null
                    inflate.tiet_new_password_again.error = null
                }
            } else {
                inflate.tiet_old_password.error = context.getString(R.string.invalid_password)
            }
        }
        inflate.tiet_new_password.addTextChangedListener {
            newPassword1 = it.toString()
            if (it.toString().length in 7..18) {
                if (newPassword1 != newPassword2) {
                    inflate.tiet_new_password.error = context.getString(R.string.PasswordNotEqual)
                } else {
                    if (newPassword1 == oldPassword) {
                        inflate.tiet_old_password.error =
                            context.getString(R.string.NewPasswordEqualOld)
                        inflate.tiet_new_password.error =
                            context.getString(R.string.NewPasswordEqualOld)
                        inflate.tiet_new_password_again.error =
                            context.getString(R.string.NewPasswordEqualOld)
                    } else {
                        inflate.tiet_old_password.error = null
                        inflate.tiet_new_password.error = null
                        inflate.tiet_new_password_again.error = null
                    }
                }
            } else {
                inflate.tiet_new_password.error = context.getString(R.string.invalid_password)
            }
        }
        inflate.tiet_new_password_again.addTextChangedListener {
            newPassword2 = it.toString()
            if (it.toString().length in 7..18) {
                if (newPassword1 != newPassword2) {
                    inflate.tiet_new_password_again.error =
                        context.getString(R.string.PasswordNotEqual)
                } else {
                    if (newPassword1 == oldPassword) {
                        inflate.tiet_old_password.error =
                            context.getString(R.string.NewPasswordEqualOld)
                        inflate.tiet_new_password.error =
                            context.getString(R.string.NewPasswordEqualOld)
                        inflate.tiet_new_password_again.error =
                            context.getString(R.string.NewPasswordEqualOld)
                    } else {
                        inflate.tiet_old_password.error = null
                        inflate.tiet_new_password.error = null
                        inflate.tiet_new_password_again.error = null
                    }
                }
            } else {
                inflate.tiet_new_password_again.error = context.getString(R.string.invalid_password)
            }
        }
        inflate.toolbar_modify_password.setNavigationOnClickListener {
            preFinish()
        }
        inflate.button_confirm.setOnClickListener {
            if (oldPassword.isEmpty() || newPassword1.isEmpty() || newPassword2.isEmpty()) {
                XToast.info(context.getString(R.string.DataEmpty))
            } else {
                if (oldPassword.length in 7..18 && newPassword1.length in 7..18 && newPassword2.length in 7..18 && newPassword1 == newPassword2 && oldPassword != newPassword1) {
                    LoadingDialog.with(context).show()
                    XHttp.obtain().post(
                        HttpUtil.Urls.User.MODIFY,
                        HashMap<String, Any>(2).also { map ->
                            map["userJson"] = App.getUser().also {
                                it.password = oldPassword
                            }.toString()
                            map["userToModifyJson"] = App.getUser().also {
                                it.password = newPassword1
                            }.toString()
                        },
                        object : HttpCallBack<MyResponse>() {
                            override fun onSuccess(myResponse: MyResponse) {
                                LoadingDialog.with(context).cancel()
                                if (myResponse.error == MyResponse.SUCCESS) {
                                    XToast.success(context.getString(R.string.PasswordModifySuccess))
                                    SharedPreferencesUtil.getSharedPreferences(LoginActivity.LOGIN_INFO)
                                        .edit()
                                        .remove(
                                            LoginActivity.USER_JSON_KEY
                                        )
                                        .putBoolean(
                                            LoginActivity.AUTO_LOGIN_KEY,
                                            false
                                        )
                                        .apply()
                                    cancel()
                                    EventBus.getDefault().post(ModifyPassword())
                                } else {
                                    XToast.error("${context.getString(R.string.PasswordModifyFail)}\n${myResponse.msg}")
                                }
                            }

                            override fun onFailed(error: String) {
                                LoadingDialog.with(context).cancel()
                                XToast.error("${context.getString(R.string.PasswordModifyFail)}\n$error")
                            }
                        })
                } else {
                    XToast.error(context.getString(R.string.PleaseCheckInput))
                }
            }
        }
    }

    private fun preFinish() {
        ModifyModeBackTipDialog.with(
            context, DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
                cancel()
            }
        ).show()
    }

}