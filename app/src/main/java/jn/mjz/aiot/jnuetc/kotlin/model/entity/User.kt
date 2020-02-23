package jn.mjz.aiot.jnuetc.kotlin.model.entity

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.youth.xframe.utils.http.HttpCallBack
import com.youth.xframe.utils.http.XHttp
import jn.mjz.aiot.jnuetc.kotlin.BR
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil.HttpUtilCallBack
import java.util.*

/**
 * @author qq1962247851
 */
class User : BaseObservable() {
    var id: Long? = null
    var openId: String? = null
    var formId: String? = null
    @get:Bindable
    var userName: String = ""
        set(userName) {
            field = userName
            notifyPropertyChanged(BR.userName)
        }
    var sex: Int? = null
    @get:Bindable
    var sno: String = ""
        set(sno) {
            field = sno
            notifyPropertyChanged(BR.sno)
        }
    @get:Bindable
    var password: String = ""
        set(password) {
            field = password
            notifyPropertyChanged(BR.password)
        }
    private var rootLevel: Int? = null
    var whichGroup: Int = -1
    var regDate: Long = 0
    override fun toString(): String {
        return GsonUtil.getInstance().toJson(this)
    }

    /**
     * 根据[.getRole] 返回园区
     *
     * @return 所有园区/北区/南区
     */
    val groupStringIfNotAll: String
        get() = if (haveWholeSchoolAccess()) "所有园区" else if (whichGroup == 0) "北区" else "南区"

    /**
     * 判断用户角色
     *
     * @return 用户角色
     */
    private val role: UserRoles
        get() = when (rootLevel) {
            1 -> UserRoles.WHOLE_SCHOOL
            2 -> UserRoles.DELETE
            3 -> UserRoles.ADMINISTRATOR
            0 -> UserRoles.NORMAL
            else -> UserRoles.NORMAL
        }

    /**
     * 判断是否可以收到整个学校的报修单
     *
     * @return 是否可以收到整个学校的报修单
     */
    fun haveWholeSchoolAccess(): Boolean {
        return role != UserRoles.NORMAL
    }

    /**
     * 判断是否为最高管理员
     *
     * @return 是否为最高管理员
     */
    fun haveAdministratorAccess(): Boolean {
        return role == UserRoles.ADMINISTRATOR
    }

    /**
     * rootLevel == 2 || rootLevel == 3
     *
     * @return 判断是否有删单权限，即管理员才有删单权限
     */
    fun haveDeleteAccess(): Boolean {
        return role != UserRoles.NORMAL && role != UserRoles.WHOLE_SCHOOL
    }

    /**
     * rootLevel == 2 || rootLevel == 3
     *
     * @return 判断是否有管理员权限
     */
    fun haveModifyAccess(): Boolean {
        return role != UserRoles.NORMAL && role != UserRoles.WHOLE_SCHOOL
    }

    /**
     * 判断与报修单是否有关系
     *
     * @param data 要判断的报修单
     * @return 是否有关系
     */
    fun haveRelationWithData(data: Data): Boolean {
        return data.repairer.contains(userName)
    }

    companion object {
        /**
         * 查询所有用户名字，不排除自己的名字
         *
         * @param callBack 回调
         */
        fun queryAllUser(callBack: HttpUtilCallBack<ArrayList<String>>) {
            XHttp.obtain()
                .post(HttpUtil.Urls.User.QUERY_ALL, null, object : HttpCallBack<MyResponse>() {
                    override fun onSuccess(myResponse: MyResponse) {
                        if (myResponse.error == MyResponse.SUCCESS) {
                            val resultList =
                                GsonUtil.parseJsonArray2List(
                                    myResponse.bodyJson,
                                    User::class.java
                                )
                            val userNames =
                                ArrayList<String>()
                            for (i in resultList.indices) {
                                userNames.add(resultList[i].userName)
                            }
                            callBack.onResponse(userNames)
                        } else {
                            callBack.onFailure(myResponse.msg)
                        }
                    }

                    override fun onFailed(error: String) {
                        callBack.onFailure(error)
                    }
                })
        }
    }
}