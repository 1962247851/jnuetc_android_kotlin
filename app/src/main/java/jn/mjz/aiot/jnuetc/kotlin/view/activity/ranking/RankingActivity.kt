package jn.mjz.aiot.jnuetc.kotlin.view.activity.ranking

import android.os.Bundle
import android.view.Menu
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.youth.xframe.utils.XDateUtils
import com.youth.xframe.utils.log.XLog
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.RankingInfo
import jn.mjz.aiot.jnuetc.kotlin.model.entity.User
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DataDao
import jn.mjz.aiot.jnuetc.kotlin.model.util.DateUtil
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil.HttpUtilCallBack
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.RankingAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.viewpager.RankingPagerAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractActivity
import kotlinx.android.synthetic.main.activity_ranking.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author 19622
 */
class RankingActivity : AbstractActivity(), OnRefreshListener {
    private lateinit var rankingPagerAdapter: RankingPagerAdapter
    private lateinit var rankingAdapter1: RankingAdapter
    private lateinit var rankingAdapter2: RankingAdapter
    private val rankingInfos1: MutableList<RankingInfo> =
        ArrayList()
    private val rankingInfos2: MutableList<RankingInfo> =
        ArrayList()

    private fun updateRanking(updateAll: Boolean) {
        User.queryAllUser(object : HttpUtilCallBack<ArrayList<String>> {
            override fun onResponse(result: ArrayList<String>) {
                if (updateAll) {
                    getRankingInfo1(result)
                    getRankingInfo2(result)
                } else {
                    if (viewPager_ranking.currentItem == 0) {
                        getRankingInfo1(result)
                    } else {
                        getRankingInfo2(result)
                    }
                }
            }

            override fun onFailure(error: String) {
                XToast.error(error)
            }
        })
    }

    override fun preFinish(): Boolean {
        return true
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return 0
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_ranking
    }

    override fun initData(savedInstanceState: Bundle?) {
        val title = App.getUser().groupStringIfNotAll
        toolbar.title = toolbar.title.toString() + "（" + title + "）"
        rankingAdapter1 = RankingAdapter(this, rankingInfos1)
        rankingAdapter2 = RankingAdapter(this, rankingInfos2)
        rankingPagerAdapter = RankingPagerAdapter(rankingAdapter1, rankingAdapter2, this)
    }

    override fun initView() {
        viewPager_ranking.setScroll(false)
        viewPager_ranking.adapter = rankingPagerAdapter
        tabLayout_ranking.setupWithViewPager(viewPager_ranking)
        srl.setOnRefreshListener(this)
        srl.isRefreshing = true
        updateRanking(true)
    }

    /**
     * 更新周排行榜
     *
     * @param result userNameList
     */
    private fun getRankingInfo1(result: List<String>?) {
        val dateToWeek =
            DateUtil.getDateToWeek(Date(System.currentTimeMillis()))
        val startDate = dateToWeek[0]
        val endDate = dateToWeek[6]
        tabLayout_ranking.getTabAt(0)?.text = "周排行榜\n$startDate-$endDate"
        val startTimeMills = XDateUtils.date2Millis(
            XDateUtils.string2Date(
                "$startDate 00:00:00",
                "yyyy/MM/dd HH:mm:ss"
            )
        )
        val endTimeMills = XDateUtils.date2Millis(
            XDateUtils.string2Date(
                "$endDate 23:59:59",
                "yyyy/MM/dd HH:mm:ss"
            )
        )
        val count = rankingInfos1.size
        rankingInfos1.clear()
        rankingAdapter1.notifyItemRangeRemoved(0, count)
        if (result != null && result.isNotEmpty()) {
            for (userName in result) {
                val list =
                    App.daoSession.dataDao.queryBuilder().where(
                        DataDao.Properties.State.eq(2),
                        DataDao.Properties.RepairDate.ge(startTimeMills),
                        DataDao.Properties.RepairDate.le(endTimeMills),
                        DataDao.Properties.Repairer.like("%$userName%")
                    ).build().list()
                if (list.isNotEmpty()) {
                    rankingInfos1.add(RankingInfo(userName, list))
                    rankingAdapter1.notifyItemInserted(rankingInfos1.size)
                }
            }
            rankingInfos1.sortWith(Comparator { o1: RankingInfo, o2: RankingInfo ->
                o2.count.compareTo(o1.count)
            })
        }
        XToast.success("周排行榜更新成功")
        srl.isRefreshing = false
    }

    /**
     * 更新总排行榜
     *
     * @param result userNameList
     */
    private fun getRankingInfo2(result: List<String>?) {
        val count = rankingInfos2.size
        rankingInfos2.clear()
        rankingAdapter2.notifyItemRangeRemoved(0, count)
        if (result != null && result.isNotEmpty()) {
            for (userName in result) {
                val list =
                    App.daoSession.dataDao.queryBuilder().where(
                        DataDao.Properties.State.eq(2),
                        DataDao.Properties.Repairer.like("%$userName%")
                    ).build().list()
                if (list.isNotEmpty()) {
                    rankingInfos2.add(RankingInfo(userName, list))
                    rankingAdapter2.notifyItemInserted(rankingInfos2.size)
                }
            }
            rankingInfos2.sortWith(Comparator { o1: RankingInfo, o2: RankingInfo ->
                o2.count.compareTo(o1.count)
            })
        }
        XLog.d("getRankingInfo2 size = ${rankingInfos2.size}")
        XToast.success("总排行榜更新成功")
        srl.isRefreshing = false
    }

    override fun onRefresh() {
        updateRanking(false)
    }
}