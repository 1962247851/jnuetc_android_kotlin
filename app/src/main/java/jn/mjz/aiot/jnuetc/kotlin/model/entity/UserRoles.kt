package jn.mjz.aiot.jnuetc.kotlin.model.entity

/**
 * 按分组接单
 *
 * @author 19622
 */
enum class UserRoles {
    NORMAL,  //可以看到全校的报修单
    WHOLE_SCHOOL,  //有删单权限
    DELETE,  //有控制报修开关的权限
    ADMINISTRATOR
}