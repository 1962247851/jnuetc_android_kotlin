package jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youth.xframe.utils.XFormatTimeUtils
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.entity.DataChangeLog
import kotlinx.android.synthetic.main.item_rv_data_change_log.view.*

/**
 * @author qq1962247851
 * @date 2020/1/20 12:19
 */
class DataChangeLogAdapter(
    private val context: Context,
    private val dataChangeLogs: List<DataChangeLog>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return if (dataChangeLogs.isEmpty()) ItemViewTypes.NO_DATA else ItemViewTypes.DATA
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == ItemViewTypes.NO_DATA) {
            HolderNoData(
                LayoutInflater.from(context).inflate(
                    R.layout.adapter_no_data_change_log,
                    parent,
                    false
                )
            )
        } else {
            HolderData(
                LayoutInflater.from(context).inflate(
                    R.layout.item_rv_data_change_log,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (viewHolder is HolderData) {
            val dataChangeLog = dataChangeLogs[position]
            viewHolder.itemView.textView_item_change_log_name.text = dataChangeLog.name
            viewHolder.itemView.textView_item_change_log_time.text =
                XFormatTimeUtils.getTimeSpanByNow1(dataChangeLog.date)
            viewHolder.itemView.textView_item_change_log_content.text = dataChangeLog.changeInfo
        }
    }

    override fun getItemCount(): Int {
        return if (dataChangeLogs.isEmpty()) 1 else dataChangeLogs.size
    }

}