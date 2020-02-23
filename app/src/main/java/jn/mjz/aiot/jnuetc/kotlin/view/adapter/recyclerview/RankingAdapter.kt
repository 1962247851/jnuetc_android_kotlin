package jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.RankingInfo
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil
import jn.mjz.aiot.jnuetc.kotlin.view.activity.datalist.DataListActivity
import kotlinx.android.synthetic.main.item_rv_ranking.view.*
import kotlinx.android.synthetic.main.item_rv_ranking.view.constraintLayout
import kotlinx.android.synthetic.main.item_rv_ranking_footer.view.*

/**
 * @author qq1962247851
 * @date 2020/1/14 16:38
 */
class RankingAdapter(
    private val context: Context,
    private val list: List<RankingInfo>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (list.isEmpty()) {
            ItemViewTypes.NO_DATA
        } else {
            if (position == list.size) ItemViewTypes.FOOTER else ItemViewTypes.DATA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemViewTypes.NO_DATA -> {
                HolderNoData(
                    LayoutInflater.from(context).inflate(
                        R.layout.adapter_no_data,
                        parent,
                        false
                    )
                )
            }
            ItemViewTypes.DATA -> {
                HolderData(
                    LayoutInflater.from(context).inflate(
                        R.layout.item_rv_ranking,
                        parent,
                        false
                    )
                )
            }
            else -> {
                HolderFooter(
                    LayoutInflater.from(context).inflate(
                        R.layout.item_rv_ranking_footer,
                        parent,
                        false
                    )
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HolderData) {
            val rankingInfo = list[position]
            holder.itemView.tv_item_number.text = (position + 1).toString()
            if (rankingInfo.userName == App.getUser().userName) {
                holder.itemView.constraintLayout.setBackgroundResource(R.drawable.background_task_view_state_done_all)
            } else {
                holder.itemView.constraintLayout.background = null
            }
            holder.itemView.tv_item_name.setText(rankingInfo.userName)
            holder.itemView.tv_item_count.text = "${rankingInfo.count}单"
            when (position) {
                0 -> {
                    holder.itemView.tv_item_number.setTextColor(Color.rgb(255, 215, 0))
                    holder.itemView.tv_item_name.setTextColor(Color.rgb(255, 215, 0))
                    holder.itemView.tv_item_count.setTextColor(Color.rgb(255, 215, 0))
                    holder.itemView.tv_item_name.textSize = 40F
                }
                1 -> {
                    holder.itemView.tv_item_number.setTextColor(Color.rgb(185, 185, 185))
                    holder.itemView.tv_item_name.setTextColor(Color.rgb(185, 185, 185))
                    holder.itemView.tv_item_count.setTextColor(Color.rgb(185, 185, 185))
                    holder.itemView.tv_item_name.textSize = 35F
                }
                2 -> {
                    holder.itemView.tv_item_number.setTextColor(Color.rgb(184, 115, 51))
                    holder.itemView.tv_item_name.setTextColor(Color.rgb(184, 115, 51))
                    holder.itemView.tv_item_count.setTextColor(Color.rgb(184, 115, 51))
                    holder.itemView.tv_item_name.textSize = 30F
                }
                else -> {
                    holder.itemView.tv_item_number.setTextColor(Color.GRAY)
                    holder.itemView.tv_item_name.setTextColor(Color.GRAY)
                    holder.itemView.tv_item_name.textSize = 20F
                    holder.itemView.tv_item_count.setTextColor(Color.GRAY)
                }
            }
            holder.itemView.card_view.setOnClickListener {
                val intent = Intent(context, DataListActivity::class.java)
                intent.putExtra(
                    DataListActivity.DATA_LIST_JSON_KEY,
                    GsonUtil.getInstance().toJson(rankingInfo.dataList)
                )
                intent.putExtra(DataListActivity.TITLE_KEY, rankingInfo.userName)
                context.startActivity(intent)
            }

        } else if (holder is HolderFooter) {
            var cnt = 0
            list.forEach {
                cnt += it.count
            }
            holder.itemView.tv_item_total.text = "${cnt}单"
        }
    }

    override fun getItemCount(): Int {
        return if (list.isEmpty()) 1 else list.size + 1
    }

}