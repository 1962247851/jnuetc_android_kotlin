package jn.mjz.aiot.jnuetc.kotlin.view.fragment.second

import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.*
import jn.mjz.aiot.jnuetc.kotlin.model.util.AnimationUtil
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.viewpager2.SecondPagerAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractFragment
import kotlinx.android.synthetic.main.fragment_second.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * SecondFragment
 *
 * @author qq1962247851
 * @date 2020/2/18 17:36
 */
class SecondFragment : AbstractFragment() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_second
    }

    override fun initData(savedInstanceState: Bundle?) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetSecondFragmentCurrentState(getSecondFragmentCurrentState: GetSecondFragmentCurrentState) {
        postCurrentState()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskCountChange(taskCountChange: TaskCountChange) {
        if (taskCountChange.state == 1) {
            tab_layout.getTabAt(0)?.text =
                "${getString(R.string.Processing)}（${taskCountChange.newCount}）"
        } else if (taskCountChange.state == 2) {
            tab_layout.getTabAt(1)?.text =
                "${getString(R.string.Done)}（${taskCountChange.newCount}）"
        }
    }

    override fun initView() {
        view_pager.adapter = SecondPagerAdapter(childFragmentManager, lifecycle)
        view_pager.isUserInputEnabled = false
//        view_pager.offscreenPageLimit = 2
        TabLayoutMediator(
            tab_layout,
            view_pager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                if (position == 0) {
                    tab.text = getString(R.string.Processing)
                } else {
                    tab.text = getString(R.string.Done)
                }
            }).attach()
        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                postCurrentState()
            }
        })
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun postCurrentState() {
        if (tab_layout.getTabAt(0)?.isSelected != false) {
            EventBus.getDefault().post(QuitSelectMode(2, true))
            EventBus.getDefault().post(
                CurrentStateChange(
                    1
                )
            )
        } else {
            EventBus.getDefault().post(QuitSelectMode(1, true))
            EventBus.getDefault().post(
                CurrentStateChange(
                    2
                )
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStartSelect(startSelect: StartSelect) {
        hideTabLayout()
        view_pager.setPadding(0, 0, 0, 0)
    }

    private fun showTabLayout() {
        if (tab_layout.visibility != View.VISIBLE) {
            tab_layout.visibility = View.VISIBLE
            tab_layout.startAnimation(AnimationUtil.moveToViewLocationFromTop())
        }
    }

    private fun hideTabLayout() {
        if (tab_layout.visibility != View.GONE) {
            tab_layout.visibility = View.GONE
            tab_layout.startAnimation(AnimationUtil.moveToViewTopQuickly())
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCancelSelect(quitSelectMode: QuitSelectMode) {
        showTabLayout()
        view_pager.setPadding(0, tab_layout.height, 0, 0)
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDestroy()
    }

}