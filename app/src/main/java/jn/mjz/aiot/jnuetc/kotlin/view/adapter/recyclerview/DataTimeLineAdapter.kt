package jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youth.xframe.XFrame
import com.youth.xframe.utils.XDateUtils
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import kotlinx.android.synthetic.main.item_rv_data_time_line.view.*

/**
 * @author qq1962247851
 * @date 2020/1/20 9:58
 */
class DataTimeLineAdapter(private val context: Context, var data: Data) :
    RecyclerView.Adapter<HolderData>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderData {
        return HolderData(
            LayoutInflater.from(context).inflate(R.layout.item_rv_data_time_line, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: HolderData, position: Int) {
        updateUi(data, position, holder)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(
        data: Data,
        position: Int,
        holder: HolderData
    ) {
        if (getItemViewType(position) == LAST) {
            holder.itemView.viewTop.visibility = View.INVISIBLE
        } else {
            holder.itemView.viewTop.visibility = View.VISIBLE
        }
        when (data.state.toInt()) {
            0 -> when (position) {
                0 -> {
                    holder.itemView.viewBottom.visibility = View.VISIBLE
                    holder.itemView.viewTop.visibility = View.INVISIBLE
                    holder.itemView.textView_item_state.text = "待处理"
                    holder.itemView.imageView_item_state.background =
                        XFrame.getDrawable(R.drawable.ic_done_accent)
                    holder.itemView.viewBottom.setBackgroundColor(XFrame.getColor(R.color.colorAccent))
                    holder.itemView.textView_item_content.text = "报修单创建，等待接单"
                    holder.itemView.textView_item_time.text = XDateUtils.millis2String(data.date)
                }
                1 -> {
                    holder.itemView.viewBottom.visibility = View.VISIBLE
                    holder.itemView.viewTop.visibility = View.VISIBLE
                    holder.itemView.textView_item_state.text = "处理中"
                    holder.itemView.imageView_item_state.background =
                        XFrame.getDrawable(R.drawable.ic_more_gray)
                    holder.itemView.viewTop.setBackgroundColor(XFrame.getColor(R.color.colorAccent))
                    holder.itemView.viewBottom.setBackgroundColor(XFrame.getColor(R.color.colorGray))
                    holder.itemView.textView_item_content.text = ""
                    holder.itemView.textView_item_time.text = ""
                }
                2 -> {
                    holder.itemView.viewBottom.visibility = View.INVISIBLE
                    holder.itemView.viewTop.visibility = View.VISIBLE
                    holder.itemView.textView_item_state.text = "已维修"
                    holder.itemView.imageView_item_state.background =
                        XFrame.getDrawable(R.drawable.ic_more_gray)
                    holder.itemView.viewTop.setBackgroundColor(XFrame.getColor(R.color.colorGray))
                    holder.itemView.textView_item_content.text = ""
                    holder.itemView.textView_item_time.text = ""
                }
                else -> {
                }
            }
            1 -> when (position) {
                0 -> {
                    holder.itemView.viewBottom.visibility = View.VISIBLE
                    holder.itemView.viewTop.visibility = View.INVISIBLE
                    holder.itemView.textView_item_state.text = "待处理"
                    holder.itemView.imageView_item_state.background =
                        XFrame.getDrawable(R.drawable.ic_done_accent)
                    holder.itemView.viewBottom.setBackgroundColor(XFrame.getColor(R.color.colorAccent))
                    holder.itemView.textView_item_content.text = "报修单创建，等待接单"
                    holder.itemView.textView_item_time.text = XDateUtils.millis2String(data.date)
                }
                1 -> {
                    holder.itemView.viewBottom.visibility = View.VISIBLE
                    holder.itemView.viewTop.visibility = View.VISIBLE
                    holder.itemView.textView_item_state.text = "处理中"
                    holder.itemView.imageView_item_state.background =
                        XFrame.getDrawable(R.drawable.ic_done_accent)
                    holder.itemView.viewTop.setBackgroundColor(XFrame.getColor(R.color.colorAccent))
                    holder.itemView.viewBottom.setBackgroundColor(XFrame.getColor(R.color.colorAccent))
                    holder.itemView.textView_item_content.text = data.repairer + "正在处理"
                    holder.itemView.textView_item_time.text =
                        XDateUtils.millis2String(data.orderDate)
                }
                2 -> {
                    holder.itemView.viewBottom.visibility = (View.INVISIBLE)
                    holder.itemView.viewTop.visibility = View.VISIBLE
                    holder.itemView.textView_item_state.text = "已维修"
                    holder.itemView.imageView_item_state.background =
                        XFrame.getDrawable(R.drawable.ic_more_gray)
                    holder.itemView.viewTop.setBackgroundColor(XFrame.getColor(R.color.colorAccent))
                    holder.itemView.textView_item_content.text = ""
                    holder.itemView.textView_item_time.text = ""
                }
                else -> {
                }
            }
            else -> when (position) {
                0 -> {
                    holder.itemView.viewBottom.visibility = View.VISIBLE
                    holder.itemView.viewTop.visibility = View.INVISIBLE
                    holder.itemView.textView_item_state.text = "待处理"
                    holder.itemView.imageView_item_state.background =
                        XFrame.getDrawable(R.drawable.ic_done_accent)
                    holder.itemView.viewBottom.setBackgroundColor(XFrame.getColor(R.color.colorAccent))
                    holder.itemView.textView_item_content.text = "报修单创建，等待接单"
                    holder.itemView.textView_item_time.text = XDateUtils.millis2String(data.date)
                }
                1 -> {
                    holder.itemView.viewBottom.visibility = View.VISIBLE
                    holder.itemView.viewTop.visibility = View.VISIBLE
                    holder.itemView.textView_item_state.text = "处理中"
                    holder.itemView.imageView_item_state.background =
                        XFrame.getDrawable(R.drawable.ic_done_accent)
                    holder.itemView.viewTop.setBackgroundColor(XFrame.getColor(R.color.colorAccent))
                    holder.itemView.viewBottom.setBackgroundColor(XFrame.getColor(R.color.colorAccent))
                    holder.itemView.textView_item_content.text = data.repairer + "正在处理"
                    holder.itemView.textView_item_time.text =
                        XDateUtils.millis2String(data.orderDate)
                }
                2 -> {
                    holder.itemView.viewBottom.visibility = View.INVISIBLE
                    holder.itemView.viewTop.visibility = View.VISIBLE
                    holder.itemView.textView_item_state.text = "已维修"
                    holder.itemView.imageView_item_state.background =
                        XFrame.getDrawable(R.drawable.ic_done_accent)
                    holder.itemView.viewTop.setBackgroundColor(XFrame.getColor(R.color.colorAccent))
                    holder.itemView.textView_item_content.text = data.repairer + "已完成维修"
                    holder.itemView.textView_item_time.text =
                        XDateUtils.millis2String(data.repairDate)
                }
                else -> {
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

    companion object {
        private const val FIRST = 0
        private const val BODY = 1
        private const val LAST = 2
    }

}