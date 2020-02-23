package jn.mjz.aiot.jnuetc.kotlin.model.entity

/**
 * @author qq1962247851
 * @date 2020/1/14 16:52
 */
class RankingInfo(
    var userName: String,
    var dataList: List<Data>
) {
    var count: Int = dataList.size
}