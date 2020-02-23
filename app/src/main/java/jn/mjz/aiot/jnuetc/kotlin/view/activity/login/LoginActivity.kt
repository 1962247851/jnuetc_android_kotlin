package jn.mjz.aiot.jnuetc.kotlin.view.activity.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.youth.xframe.utils.http.HttpCallBack
import com.youth.xframe.utils.http.XHttp
import com.youth.xframe.utils.statusbar.XStatusBar
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.databinding.ActivityLoginBinding
import jn.mjz.aiot.jnuetc.kotlin.model.custom.ContextViewModelFactory
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.MyResponse
import jn.mjz.aiot.jnuetc.kotlin.model.entity.User
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Version
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.SharedPreferencesUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.UpdateUtil
import jn.mjz.aiot.jnuetc.kotlin.view.activity.admin.AdminActivity
import jn.mjz.aiot.jnuetc.kotlin.view.activity.main.MainActivity
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractActivity
import jn.mjz.aiot.jnuetc.kotlin.view.custom.LoadingDialog
import jn.mjz.aiot.jnuetc.kotlin.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*

/**
 * LoginActivity
 *
 * @author qq1962247851
 * @date 2020/2/18 12:27
 */
class LoginActivity : AbstractActivity(true) {

    private var dayDPState = MainActivity.DAY_DP_STATE_DEFAULT_VALUE
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun preFinish(): Boolean {
        setResult(Activity.RESULT_CANCELED)
        return true
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return 0
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(MainActivity.DAY_DP_STATE_KEY, dayDPState)
        super.onSaveInstanceState(outState)
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            dayDPState = savedInstanceState.getBoolean(
                MainActivity.DAY_DP_STATE_KEY,
                MainActivity.DAY_DP_STATE_DEFAULT_VALUE
            )
        }
        binding = DataBindingUtil.setContentView(this, layoutId)
        loginViewModel =
            ContextViewModelFactory.getInstance(this).create(LoginViewModel::class.java)
        loginViewModel.user.observe(this, Observer {
            binding.valid =
                it.sno.length == 10 && it.password.length in 7..18
            binding.user = loginViewModel.user.value
        })
        binding.snoTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length == 10) {
                    username.error = null
                } else {
                    username.error = getString(R.string.invalid_sno)
                }
                loginViewModel.user.value?.sno = s.toString()
                loginViewModel.user.value = loginViewModel.user.value
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                //ignore
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                //ignore
            }
        }
        binding.passwordTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length in 7..18) {
                    password.error = null
                } else {
                    password.error = getString(R.string.invalid_password)
                }
                loginViewModel.user.value?.password = s.toString()
                loginViewModel.user.value = loginViewModel.user.value
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                //ignore
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                //ignore
            }
        }
        binding.setOnLoginClick { login(true) }
        binding.setOnForgetClick {
            Data.openQq(getString(R.string.DeveloperQQ))
        }
    }

    override fun initView() {
        UpdateUtil.checkForUpdate(true, this, object : UpdateUtil.IServerAvailableListener {
            override fun onServerValid(currentVersion: Version?, newVersion: Version?) {
                if (((currentVersion == null && newVersion == null) || (currentVersion?.id == newVersion?.id))) {
                    //没有更新
                    AdminActivity.checkService(
                        "dayDP",
                        object : HttpUtil.HttpUtilCallBack<Boolean> {
                            override fun onResponse(result: Boolean) {
                                if (SharedPreferencesUtil.getSettingPreferences().getBoolean(
                                        "welcome",
                                        true
                                    )
                                ) {
                                    dayDPState = result
                                }
                                login(false)
                            }

                            override fun onFailure(error: String) {
                                XToast.error(error)
                            }
                        })
                }
            }

            override fun onServerInvalid() {
                //ignore
            }
        })
        XStatusBar.setTranslucent(this)
    }

    private fun login(loginManually: Boolean) {
        val autoLogin = getSharedPreferences(LOGIN_INFO, Context.MODE_PRIVATE)
            .getBoolean(AUTO_LOGIN_KEY, false)
        if (autoLogin || loginManually) {
            if (autoLogin) {
                linearLayout_login_logo_welcome.visibility = View.VISIBLE
            }
            LoadingDialog.with(this).show()
            XHttp.obtain().post(HttpUtil.Urls.User.LOGIN, HashMap<String, Any>(2).also {
                it["sno"] = loginViewModel.user.value!!.sno
                it["password"] = loginViewModel.user.value!!.password
            }, object : HttpCallBack<MyResponse>() {
                override fun onSuccess(result: MyResponse) {
                    LoadingDialog.with(this@LoginActivity).cancel()
                    if (result.error == MyResponse.SUCCESS) {
                        val userFromCloud =
                            GsonUtil.getInstance().fromJson(result.bodyJson, User::class.java)
                        getSharedPreferences(
                            LOGIN_INFO,
                            Context.MODE_PRIVATE
                        ).edit().also {
                            it.putString(USER_JSON_KEY, result.bodyJson)
                            it.putBoolean(AUTO_LOGIN_KEY, true)
                            it.apply()
                        }
                        setResult(Activity.RESULT_OK)
                        finish()
                        XToast.success("${getString(R.string.welcome)} ${userFromCloud.userName}")
                        startActivity(
                            Intent(this@LoginActivity, MainActivity::class.java).putExtra(
                                MainActivity.DAY_DP_STATE_KEY,
                                dayDPState
                            )
                        )
                    } else {
                        if (autoLogin) {
                            linearLayout_login_logo_welcome.visibility = View.GONE
                        }
                        XToast.error("${getString(R.string.login_failed)}\n${result.msg}")
                    }
                }

                override fun onFailed(error: String) {
                    LoadingDialog.with(this@LoginActivity).cancel()
                    if (autoLogin) {
                        linearLayout_login_logo_welcome.visibility = View.GONE
                    }
                    XToast.error("${getString(R.string.login_failed)}\n$error")
                }
            })
        } else {
            linearLayout_login_logo_welcome.visibility = View.GONE
        }
    }

    companion object {
        const val LOGIN_INFO = "login_info"
        const val USER_JSON_KEY = "user_json"
        const val AUTO_LOGIN_KEY = "auto_login"
    }
}