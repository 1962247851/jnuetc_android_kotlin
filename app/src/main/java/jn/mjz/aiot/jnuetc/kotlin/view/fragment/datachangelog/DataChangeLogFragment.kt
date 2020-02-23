package jn.mjz.aiot.jnuetc.kotlin.view.fragment.datachangelog

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.DataChangeLog
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.DataChangeLogAdapter
import jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview.WrapContentLinearLayoutManager
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractFragment
import jn.mjz.aiot.jnuetc.kotlin.viewmodel.DetailsViewModel
import kotlinx.android.synthetic.main.fragment_data_change_log.*
import java.util.*

class DataChangeLogFragment : AbstractFragment() {
    private var dataChangeLogJson: String? = null
    private var dataChangeLogAdapter: DataChangeLogAdapter? = null
    private val dataChangeLogs: MutableList<DataChangeLog> =
        ArrayList()
    private lateinit var detailsViewModel: DetailsViewModel

    override fun getLayoutId(): Int {
        return R.layout.fragment_data_change_log
    }


    override fun initData(savedInstanceState: Bundle?) {
        detailsViewModel =
            ViewModelProvider(activity!!).get(
                DetailsViewModel::class.java
            )
        detailsViewModel.data.observe(
            activity!!,
            Observer { data: Data? ->
                if (data != null) {
                    val changeLogs =
                        data.dataChangeLogs
                    val sorted = sortLogByTimeDesc(changeLogs)
                    data.dataChangeLogs.clear()
                    data.dataChangeLogs.addAll(sorted)
                    val changeLogsString = GsonUtil.getInstance().toJson(changeLogs)
                    if (changeLogsString != dataChangeLogJson) {
                        val oldSize = dataChangeLogs.size
                        dataChangeLogJson = changeLogsString
                        if (oldSize != 0) {
                            dataChangeLogs.clear()
                            dataChangeLogAdapter?.notifyItemRangeRemoved(0, oldSize)
                        }
                        dataChangeLogs.addAll(changeLogs)
                        dataChangeLogAdapter?.notifyItemRangeInserted(0, dataChangeLogs.size)
                    }
                }
            }
        )
        dataChangeLogAdapter = DataChangeLogAdapter(context!!, dataChangeLogs)
    }

    override fun initView() {
        recyclerView_data_change_log.layoutManager = WrapContentLinearLayoutManager(context)
        recyclerView_data_change_log.adapter = dataChangeLogAdapter
    }

    companion object {
        @JvmStatic
        fun sortLogByTimeDesc(dataChangeLogs: List<DataChangeLog>): List<DataChangeLog> {
            return dataChangeLogs.sortedByDescending {
                it.date
            }
        }
    }
}