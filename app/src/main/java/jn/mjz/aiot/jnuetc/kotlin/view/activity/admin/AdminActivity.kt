package jn.mjz.aiot.jnuetc.kotlin.view.activity.admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import androidx.databinding.DataBindingUtil
import com.youth.xframe.utils.http.HttpCallBack
import com.youth.xframe.utils.http.XHttp
import com.youth.xframe.utils.permission.XPermission
import com.youth.xframe.utils.permission.XPermission.OnPermissionListener
import com.youth.xframe.widget.XToast
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.databinding.ActivityAdminBinding
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.custom.GlideAppEngine
import jn.mjz.aiot.jnuetc.kotlin.model.entity.MyResponse
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Version
import jn.mjz.aiot.jnuetc.kotlin.model.util.*
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil.HttpUtilCallBack
import jn.mjz.aiot.jnuetc.kotlin.model.util.UpdateUtil.IUpdateListener
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractActivity
import jn.mjz.aiot.jnuetc.kotlin.view.custom.LoadingDialog
import java.io.File


/**
 * AdminActivity
 *
 * @author qq1962247851
 * @date 2020/2/20 16:45
 */
class AdminActivity : AbstractActivity(true) {

    lateinit var activityAdminBinding: ActivityAdminBinding

    override fun getLayoutId(): Int {
        return R.layout.activity_admin
    }

    override fun initData(savedInstanceState: Bundle?) {
        activityAdminBinding =
            DataBindingUtil.setContentView(this, layoutId) as ActivityAdminBinding
        activityAdminBinding.user = App.getUser()
        checkRepair(false)
        checkDayDP(false)
        checkRegister(false)
    }

    override fun initView() {
        activityAdminBinding.buttonAdminPush.setOnClickListener {
            val message: String = activityAdminBinding.tietPushMessage.text.toString()
            val url: String = activityAdminBinding.tietPushUrl.text.toString()
            if (message.isEmpty() || url.isEmpty()) {
                XToast.info(getString(R.string.DataEmpty))
            } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                XToast.error(getString(R.string.InvalidUrl))
            } else {
                LoadingDialog.with(this).show()
                UpdateUtil.checkForUpdate(object : IUpdateListener {

                    override fun haveNewVersion(currentVersion: Version, newVersion: Version) {
                        LoadingDialog.with(this@AdminActivity).cancel()
                        XToast.error("当前不是最新版本")
                    }

                    override fun develop() {
                        UpdateUtil.insert(
                            message,
                            url,
                            object : HttpUtilCallBack<Boolean> {
                                override fun onResponse(result: Boolean) {
                                    LoadingDialog.with(this@AdminActivity).cancel()
                                    if (result) {
                                        XToast.success(getString(R.string.PushSuccess))
                                    }
                                }

                                override fun onFailure(error: String) {
                                    LoadingDialog.with(this@AdminActivity).cancel()
                                    XToast.error("${getString(R.string.PushFail)}\n$error")
                                }
                            })
                    }

                    override fun error(error: String) {
                        LoadingDialog.with(this@AdminActivity).cancel()
                        XToast.error("${getString(R.string.PushFail)}\n$error")
                    }

                    override fun noUpdate(currentVersion: Version) {
                        LoadingDialog.with(this@AdminActivity).cancel()
                        XToast.error("当前是最新版本")
                    }
                })
            }
        }
        activityAdminBinding.switchRepair.setOnClickListener {
            changeRepairer(activityAdminBinding.switchRepair.isChecked)
        }
        activityAdminBinding.switchAdminDayDP.setOnClickListener {
            changeDayDP(activityAdminBinding.switchAdminDayDP.isChecked)
        }
        activityAdminBinding.switchAdminRegister.setOnClickListener {
            changeRegister(activityAdminBinding.switchAdminRegister.isChecked)
        }
        activityAdminBinding.buttonAdminExport.setOnClickListener {
            XPermission.requestPermissions(
                this,
                CREATE_FILE,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                object : OnPermissionListener {
                    override fun onPermissionGranted() {
                        FileUtil.createFile(
                            this@AdminActivity,
                            "text/*",
                            String.format(
                                "报修单数据%s.xls",
                                DateUtil.getDateAndTime(System.currentTimeMillis(), " ")
                            )
                        )
                    }

                    override fun onPermissionDenied() {
                        XPermission.showTipsDialog(this@AdminActivity)
                    }
                })
        }
        activityAdminBinding.buttonAdminUploadDP.setOnClickListener {
            XPermission.requestPermissions(
                this@AdminActivity,
                SELECT_PHOTO_FILE,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                object : OnPermissionListener {
                    override fun onPermissionGranted() {
                        Matisse.from(this@AdminActivity)
                            .choose(MimeType.of(MimeType.JPEG, MimeType.PNG))
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.85f)
                            .imageEngine(GlideAppEngine())
                            .forResult(SELECT_PHOTO_FILE)
                    }

                    override fun onPermissionDenied() {
                        XPermission.showTipsDialog(this@AdminActivity)
                    }
                })
        }
        activityAdminBinding.buttonAdminInsert.setOnClickListener {
            val code = activityAdminBinding.tidtAdminCode.text.toString()
            if (code.isNotEmpty()) {
                insertCode(code)
            } else {
                XToast.error(getString(R.string.DataEmpty))
            }
        }
    }

    private fun insertCode(code: String) {
        LoadingDialog.with(this).show()
        XHttp.obtain().post(
            HttpUtil.Urls.Code.INSERT,
            HashMap<String, Any>(1).also { it["code"] = code },
            object : HttpCallBack<MyResponse>() {

                override fun onSuccess(myResponse: MyResponse) {
                    LoadingDialog.with(this@AdminActivity).cancel();
                    if (myResponse.error == MyResponse.SUCCESS) {
                        App.copyToClipboard(this@AdminActivity, code)
                        activityAdminBinding.tidtAdminCode.text = null
                        XToast.success(getString(R.string.InsertCodeSuccess))
                    } else {
                        if (myResponse.error == MyResponse.FAILED) {
                            activityAdminBinding.tidtAdminCode.text = null
                        }
                        XToast.error("${getString(R.string.InsertCodeFail)}\n${myResponse.msg}")
                    }
                }

                override fun onFailed(error: String?) {
                    LoadingDialog.with(this@AdminActivity).cancel()
                    XToast.error("${getString(R.string.InsertCodeFail)}\n$error")
                }
            })
    }

    override fun preFinish(): Boolean {
        return true
    }

    private fun changeRepairer(available: Boolean) {
        LoadingDialog.with(this).show()
        changeService("repair", available, object : HttpUtilCallBack<Boolean> {
            override fun onResponse(result: Boolean) {
                LoadingDialog.with(this@AdminActivity).cancel()
                activityAdminBinding.switchRepair.isChecked = result
                XToast.success(getString(R.string.RefreshSuccess))
            }

            override fun onFailure(error: String) {
                LoadingDialog.with(this@AdminActivity).cancel()
                activityAdminBinding.switchRepair.isChecked = !available
                XToast.error("${getString(R.string.RefreshFail)}\n$error")
            }
        })
    }

    private fun changeDayDP(available: Boolean) {
        LoadingDialog.with(this).show()
        changeService("dayDP", available, object : HttpUtilCallBack<Boolean> {
            override fun onResponse(result: Boolean) {
                LoadingDialog.with(this@AdminActivity).cancel()
                activityAdminBinding.switchAdminDayDP.isChecked = result
                XToast.success(getString(R.string.RefreshSuccess))
            }

            override fun onFailure(error: String) {
                LoadingDialog.with(this@AdminActivity).cancel()
                activityAdminBinding.switchAdminDayDP.isChecked = !available
                XToast.error("${getString(R.string.RefreshFail)}\n$error")
            }
        })
    }

    private fun changeRegister(available: Boolean) {
        LoadingDialog.with(this).show()
        changeService("register", available, object : HttpUtilCallBack<Boolean> {
            override fun onResponse(result: Boolean) {
                LoadingDialog.with(this@AdminActivity).cancel()
                activityAdminBinding.switchAdminRegister.isChecked = result
                XToast.success(getString(R.string.RefreshSuccess))
            }

            override fun onFailure(error: String) {
                LoadingDialog.with(this@AdminActivity).cancel()
                activityAdminBinding.switchAdminRegister.isChecked = !available
                XToast.error("${getString(R.string.RefreshFail)}\n$error")
            }
        })
    }

    private fun checkRepair(showSuccessToast: Boolean) {
        LoadingDialog.with(this).show()
        checkService("repair", object : HttpUtilCallBack<Boolean> {
            override fun onResponse(result: Boolean) {
                LoadingDialog.with(this@AdminActivity).cancel()
                activityAdminBinding.switchRepair.isChecked = result
                if (showSuccessToast) {
                    XToast.success(getString(R.string.RefreshSuccess))
                }
            }

            override fun onFailure(error: String) {
                LoadingDialog.with(this@AdminActivity).cancel()
                XToast.error("${getString(R.string.RefreshFail)}\n$error")
            }
        })
    }

    private fun checkDayDP(showSuccessToast: Boolean) {
        LoadingDialog.with(this@AdminActivity).show()
        checkService("dayDP", object : HttpUtilCallBack<Boolean> {
            override fun onResponse(result: Boolean) {
                LoadingDialog.with(this@AdminActivity).cancel()
                activityAdminBinding.switchAdminDayDP.isChecked = result
                if (showSuccessToast) {
                    XToast.success(getString(R.string.RefreshSuccess))
                }
            }

            override fun onFailure(error: String) {
                LoadingDialog.with(this@AdminActivity).cancel()
                XToast.error("${getString(R.string.RefreshFail)}\n$error")
            }
        })
    }

    private fun checkRegister(showSuccessToast: Boolean) {
        LoadingDialog.with(this@AdminActivity).show()
        checkService("register", object : HttpUtilCallBack<Boolean> {
            override fun onResponse(result: Boolean) {
                LoadingDialog.with(this@AdminActivity).cancel()
                activityAdminBinding.switchAdminRegister.isChecked = result
                if (showSuccessToast) {
                    XToast.success(getString(R.string.RefreshSuccess))
                }
            }

            override fun onFailure(error: String) {
                LoadingDialog.with(this@AdminActivity).cancel()
                XToast.error("${getString(R.string.RefreshFail)}\n$error")
            }
        })
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == CREATE_FILE) {
                LoadingDialog.with(this@AdminActivity).show()
                FileUtil.exportDatasToExcel(
                    this@AdminActivity,
                    data.data,
                    object : FileUtil.IOnExportListener {
                        override fun onSuccess() {
                            LoadingDialog.with(this@AdminActivity).cancel()
                            XToast.success(getString(R.string.ExportSuccess))
                        }

                        override fun onError() {
                            LoadingDialog.with(this@AdminActivity).cancel()
                            XToast.error(getString(R.string.ExportFail))
                        }
                    })
            } else if (requestCode == SELECT_PHOTO_FILE) {
                LoadingDialog.with(this@AdminActivity).show()
                val uris: List<Uri> = Matisse.obtainResult(data)
                val file: File = FileUtil.uriToFile(uris[0], this)!!
                val fileName = DateUtil.getCurrentDate("yyyy-MM-dd").toString() + ".jpg"
                FileUtil.uploadTipDp(fileName, file, object : HttpCallBack<Boolean>() {
                    override fun onSuccess(aBoolean: Boolean) {
                        LoadingDialog.with(this@AdminActivity).cancel()
                        XToast.success(getString(R.string.UploadSuccess))
                    }

                    override fun onFailed(error: String) {
                        LoadingDialog.with(this@AdminActivity).cancel()
                        XToast.error("${getString(R.string.UploadFail)}\n$error")
                    }
                })
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val CREATE_FILE = 1234
        const val SELECT_PHOTO_FILE = 1235
        @JvmStatic
        fun checkService(type: String, callBack: HttpUtilCallBack<Boolean>) {
            XHttp.obtain().post(HttpUtil.Urls.State.CHECK_SERVICE, HashMap<String, Any>(1).also {
                it["type"] = type
            }, object : HttpCallBack<MyResponse>() {
                override fun onSuccess(result: MyResponse) {
                    if (result.error == MyResponse.SUCCESS) {
                        callBack.onResponse(
                            GsonUtil.getInstance().fromJson(
                                result.bodyJson,
                                Boolean::class.java
                            )
                        )
                    } else {
                        callBack.onFailure(result.msg)
                    }
                }

                override fun onFailed(error: String) {
                    callBack.onFailure(error)
                }
            })
        }

        @JvmStatic
        fun changeService(type: String, available: Boolean, callBack: HttpUtilCallBack<Boolean>) {
            XHttp.obtain().post(HttpUtil.Urls.State.CHANGE_SERVICE, HashMap<String, Any>(2).also {
                it["type"] = type
                it["available"] = available
            }, object : HttpCallBack<MyResponse>() {
                override fun onSuccess(result: MyResponse) {
                    if (result.error == MyResponse.SUCCESS) {
                        callBack.onResponse(
                            GsonUtil.getInstance().fromJson(
                                result.bodyJson,
                                Boolean::class.java
                            )
                        )
                    } else {
                        callBack.onFailure(result.msg)
                    }
                }

                override fun onFailed(error: String) {
                    callBack.onFailure(error)
                }
            })
        }
    }
}