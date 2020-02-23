package jn.mjz.aiot.jnuetc.kotlin.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import jn.mjz.aiot.jnuetc.kotlin.model.custom.ContextViewModel
import jn.mjz.aiot.jnuetc.kotlin.model.entity.User
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.view.activity.login.LoginActivity

/**
 * LoginViewModel
 *
 * @author qq1962247851
 * @date 2020/2/18 13:22
 */
class LoginViewModel(context: Context) : ContextViewModel(context) {
    val user =
        MutableLiveData<User>().apply {
            val userJson =
                getContext().getSharedPreferences(
                    LoginActivity.LOGIN_INFO,
                    Context.MODE_PRIVATE
                ).getString(LoginActivity.USER_JSON_KEY, null)
            value = if (userJson == null) {
                User()
            } else {
                GsonUtil.getInstance().fromJson(
                    userJson,
                    User::class.java
                )
            }
        }

}