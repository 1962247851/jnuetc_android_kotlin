package jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.youth.xframe.XFrame
import com.youth.xframe.utils.XDateUtils
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.util.DateUtil
import kotlinx.android.synthetic.main.item_rv_task.view.*
import java.util.*

/**
 * TaskAdapter
 *
 * @author qq1962247851
 * @date 2020/2/18 15:47
 */
class TaskAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private var context: Context
    //总的list
    private var allDataList: ArrayList<Data>
    //实际展示用的list
    private val tempDataList = ArrayList<Data>()
    private var iTaskListener: ITaskListener
    private var isSelectMode = false
    private var enableSelect = false
    private val booleanArray = SparseBooleanArray()
    private var selectCnt = 0

    companion object {
        /**
         * 每次加载的个数
         */
        private const val LOAD_COUNT = 10
        private const val TAG = "TaskAdapter "
    }

    /**
     * 加载更多
     */
    fun loadMoreData() {
        val currentCount = tempDataList.size
        if (allDataList.size > currentCount) {
            for (i in currentCount until currentCount + LOAD_COUNT) {
                if (i > allDataList.size) {
                    break
                } else {
                    tempDataList.add(allDataList[i])
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (holder is HolderData) {
            val data = allDataList[position]
            if (payloads.isNotEmpty()) {
                when (payloads[0]) {
                    "updateState" -> updateState(data, holder)
                    "updateSelect" -> updateSelect(position, holder)
                    "selectMode" -> selectMode(position, holder)
                    "quitSelectMode" -> quitSelectMode(position, holder)
                    else -> {
                    }
                }
            } else {
                updateUI(position, data, holder)
            }
        } else if (holder is HolderNoData) {
            if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                val lp = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                lp.isFullSpan = true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(position: Int, data: Data, holder: HolderData) {
        holder.itemView.card_view.tag = position
        holder.itemView.tv_item_location_and_id.text =
            String.format("%s - %s", data.local, data.id.toString())
        holder.itemView.tv_item_date_and_name.text =
            String.format("%s %s", DateUtil.getDateAndTime(data.date, " "), data.name)
        holder.itemView.tv_item_model.text =
            String.format("%s：%s", context.getString(R.string.Model), data.model)
        if (data.photo != null && data.photo.isNotEmpty()) {
            holder.itemView.cardView_photo.visibility = View.VISIBLE
            holder.itemView.text_view_photo.text =
                String.format(context.getString(R.string.PhotoNumber), data.photoUrlList.size)
        } else {
            holder.itemView.cardView_photo.visibility = View.GONE
        }
        if (data.repairer.length > 3) {
            holder.itemView.cardView_team_work.visibility = View.VISIBLE
        } else {
            holder.itemView.cardView_team_work.visibility = View.GONE
        }
        if (data.state.toInt() == 0) {
            val twoDataDifference = XDateUtils.getTwoDataDifference(Date(data.date))
            if (twoDataDifference.day > 1) {
                holder.itemView.text_view_long_time_no_order.text = String.format(
                    context.getString(R.string.WaitTimeTooLong), twoDataDifference.day
                )
                holder.itemView.cardView_long_time_no_order.visibility = View.VISIBLE
            } else {
                holder.itemView.cardView_long_time_no_order.visibility = View.GONE
            }
        } else {
            holder.itemView.cardView_long_time_no_order.visibility = View.GONE
        }
        holder.itemView.etv_item_message.text =
            String.format("%s：%s", context.getString(R.string.Message), data.message)
        updateState(data, holder)
        if (isSelectMode) {
            selectMode(position, holder)
            updateSelect(position, holder)
        } else {
            quitSelectMode(position, holder)
        }
        holder.itemView.card_view.setOnClickListener {
            if (!isSelectMode) {
                iTaskListener.onItemClick(position, data, holder.itemView.card_view)
            } else {
                //防止错位
                val pos = holder.itemView.card_view.tag as Int
                val b = booleanArray[pos, false]
                booleanArray.put(pos, !b)
                updateSelect(pos, holder)
                iTaskListener.onSelect(if (b) --selectCnt else ++selectCnt)
            }
        }
        holder.itemView.tv_item_confirm.setOnClickListener {
            iTaskListener.onConfirmClick(
                position,
                data
            )
        }
        if (enableSelect) {
            holder.itemView.card_view.setOnLongClickListener {
                toggleSelectMode()
                if (isSelectMode && enableSelect) {
                    val pos = holder.itemView.card_view.tag as Int
                    val b = booleanArray[pos, false]
                    booleanArray.put(pos, !b)
                }
                return@setOnLongClickListener true
            }
        } else {
            holder.itemView.card_view.setOnLongClickListener(null)
        }
    }

    private fun startSelect() {
        if (!isSelectMode) {
            isSelectMode = true
            notifyItemRangeChanged(0, allDataList.size, "updateSelect")
            notifyItemRangeChanged(0, allDataList.size, "selectMode")
            selectCnt = 1
            iTaskListener.onStartSelect(selectCnt)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateState(data: Data, holder: HolderData) {
        val repairer = data.repairer
        when (data.state.toInt()) {
            0 -> {
                holder.itemView.text_view_photo.setBackgroundColor(XFrame.getColor(R.color.Yellow))
                holder.itemView.text_view_photo.setTextColor(XFrame.getColor(android.R.color.black))
                holder.itemView.text_view_team_work.setBackgroundColor(XFrame.getColor(R.color.Yellow))
                holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_new_left)
                holder.itemView.tv_item_confirm.visibility = View.VISIBLE
                holder.itemView.tv_item_time_info.visibility = View.GONE
                holder.itemView.tv_item_state.setText(R.string.NewData)
                holder.itemView.tv_item_confirm.setText(R.string.Order)
            }
            1 -> {
                holder.itemView.text_view_photo.setBackgroundColor(XFrame.getColor(R.color.Green))
                holder.itemView.text_view_photo.setTextColor(XFrame.getColor(android.R.color.white))
                holder.itemView.text_view_team_work.setBackgroundColor(XFrame.getColor(R.color.Green))
                holder.itemView.tv_item_confirm.visibility = View.GONE
                holder.itemView.tv_item_time_info.visibility = View.VISIBLE
                holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_processing_left)
                if (repairer != App.getUser().userName) {
                    holder.itemView.tv_item_state.text =
                        "$repairer  ${context.getString(R.string.Processing)}"
                } else {
                    holder.itemView.tv_item_state.setText(R.string.Processing)
                }
                holder.itemView.tv_item_time_info.text =
                    "${context.getString(R.string.OrderTime)} ${DateUtil.getDateAndTime(
                        data.orderDate,
                        " "
                    )}"
            }
            2 -> {
                holder.itemView.text_view_photo.setBackgroundColor(XFrame.getColor(R.color.colorPrimary))
                holder.itemView.text_view_photo.setTextColor(XFrame.getColor(android.R.color.white))
                holder.itemView.text_view_team_work.setBackgroundColor(XFrame.getColor(R.color.colorPrimary))
                holder.itemView.tv_item_confirm.visibility = View.GONE
                holder.itemView.tv_item_time_info.visibility = View.VISIBLE
                holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_done_left)
                if (repairer == App.getUser().userName) {
                    holder.itemView.tv_item_state.setText(R.string.Done)
                } else {
                    holder.itemView.tv_item_state.text =
                        "$repairer  ${context.getString(R.string.Done)}"
                }
                holder.itemView.tv_item_time_info.text = String.format(
                    "%s：%s\n%s：%s",
                    context.getString(R.string.OrderTime),
                    DateUtil.getDateAndTime(data.orderDate, " "),
                    context.getString(R.string.FeedbackTime),
                    DateUtil.getDateAndTime(data.repairDate, " ")
                )
            }
            else -> {
            }
        }
    }

    private fun quitSelectMode(
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        when (allDataList[position].state.toInt()) {
            0 -> holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_new_left)
            1 -> holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_processing_left)
            2 -> holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_done_left)
            else -> {
            }
        }
    }

    private fun selectMode(
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        if (!booleanArray[position, false]) {
            holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_unselect)
        }
    }

    private fun updateSelect(
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        if (!booleanArray[position, false]) {
            holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_unselect)
        } else {
            when (allDataList[position].state.toInt()) {
                0 -> holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_new_all)
                1 -> holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_processing_all)
                2 -> holder.itemView.relativeLayout_item_state.setBackgroundResource(R.drawable.background_task_view_state_done_all)
                else -> {
                }
            }
        }
    }

    fun confirmSelect() {
        iTaskListener.onConfirmSelect(selectCnt, booleanArray)
    }

    fun toggleSelectMode() {
        if (isSelectMode) {
            quitSelect()
        } else {
            startSelect()
        }
    }

    fun quitSelect() {
        if (isSelectMode) {
            selectCnt = 0
            isSelectMode = false
            booleanArray.clear()
            notifyItemRangeChanged(0, allDataList.size, "quitSelectMode")
            iTaskListener.onQuitSelectMode()
        }
    }

    fun toggleSelectAOrClearAll() {
        if (isSelectAll()) {
            clearAllSelected()
        } else {
            selectAll()
        }
    }

    fun clearAllSelected() {
        booleanArray.clear()
        selectCnt = 0
        notifyItemRangeChanged(0, allDataList.size, "updateSelect")
        iTaskListener.onSelect(selectCnt)
    }

    fun isSelectAll(): Boolean {
        return selectCnt == allDataList.size
    }

    fun selectAll() {
        if (allDataList.isNotEmpty()) {
            allDataList.forEachIndexed { index, _ ->
                booleanArray.put(index, true)
            }
            selectCnt = allDataList.size
            notifyItemRangeChanged(0, allDataList.size, "updateSelect")
            iTaskListener.onSelect(selectCnt)
        }
    }

    fun isSelectMode(): Boolean {
        return isSelectMode
    }

    fun selectNone(): Boolean {
        for (i in 0 until booleanArray.size()) {
            val key = booleanArray.keyAt(i)
            if (booleanArray[key]) {
                return false
            }
        }
        return true
    }

    fun setEnableSelect(enableSelect: Boolean) {
        this.enableSelect = enableSelect
    }

    constructor(
        context: Context,
        enableSelect: Boolean,
        list: ArrayList<Data>,
        iTaskListener: ITaskListener
    ) {
        this.context = context
        this.enableSelect = enableSelect
        this.allDataList = list
        this.iTaskListener = iTaskListener
    }

    constructor(
        context: Context,
        list: ArrayList<Data>,
        iTaskListener: ITaskListener
    ) {
        this.context = context
        this.allDataList = list
        this.iTaskListener = iTaskListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
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
            else -> {
                HolderData(
                    LayoutInflater.from(context).inflate(
                        R.layout.item_rv_task,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return if (allDataList.isEmpty()) 1 else allDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (allDataList.isEmpty()) {
            ItemViewTypes.NO_DATA
        } else {
            ItemViewTypes.DATA
        }
    }

    interface ITaskListener {
        /**
         * 点击项目
         *
         * @param position 位置
         * @param data     Data
         */
        fun onItemClick(
            position: Int,
            data: Data,
            cardView: CardView
        )

        /**
         * 开始多选
         *
         * @param count 选择的数目
         */
        fun onStartSelect(count: Int)

        /**
         * 正在选择
         *
         * @param count 选中的数目
         */
        fun onSelect(count: Int)

        /**
         * 选择完成
         * @param count 选择的个数
         * @param sparseBooleanArray 选中数组
         */
        fun onConfirmSelect(count: Int, sparseBooleanArray: SparseBooleanArray)

        /**
         * 取消多选
         */
        fun onQuitSelectMode()

        /**
         * 接单
         *
         * @param position 位置
         * @param data     Data
         */
        fun onConfirmClick(
            position: Int,
            data: Data
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //ignore
    }
}