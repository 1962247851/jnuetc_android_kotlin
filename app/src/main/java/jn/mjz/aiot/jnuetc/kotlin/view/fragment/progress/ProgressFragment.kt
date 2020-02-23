package jn.mjz.aiot.jnuetc.kotlin.view.fragment.progress

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.DataTimeLineAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.WrapContentLinearLayoutManager
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractFragment
import jn.mjz.aiot.jnuetc.kotlin.viewmodel.DetailsViewModel
import kotlinx.android.synthetic.main.fragment_progress.*

class ProgressFragment : AbstractFragment() {
    private var dataTimeLineAdapter: DataTimeLineAdapter? = null
    private lateinit var detailsViewModel: DetailsViewModel

    override fun getLayoutId(): Int {
        return R.layout.fragment_progress
    }

    override fun initData(savedInstanceState: Bundle?) {
        detailsViewModel =
            ViewModelProvider(activity!!).get(
                DetailsViewModel::class.java
            )
    }

    override fun initView() {
        recyclerView_progress.layoutManager = WrapContentLinearLayoutManager(context)
        detailsViewModel.data.observe(
            activity!!,
            Observer {
                if (dataTimeLineAdapter == null) {
                    dataTimeLineAdapter = DataTimeLineAdapter(context!!, it)
                    recyclerView_progress.adapter = dataTimeLineAdapter
                }
                dataTimeLineAdapter?.data = it
                dataTimeLineAdapter?.notifyItemRangeChanged(0, 3)
            }
        )
    }
}