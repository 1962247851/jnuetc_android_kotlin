package jn.mjz.aiot.jnuetc.kotlin.view.adapter.viewpager2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.task.TaskFragment

/**
 * @author qq1962247851
 * @date 2020/2/18 16:34
 */
class SecondPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        return TaskFragment(position + 1)
    }

    override fun getItemCount(): Int {
        return 2
    }
}