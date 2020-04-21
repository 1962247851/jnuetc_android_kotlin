package jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youth.xframe.XFrame
import jn.mjz.aiot.jnuetc.kotlin.R
import kotlinx.android.synthetic.main.item_rv_checkable_view.view.*

/**
 * @author 19622
 */
class CheckableAdapter(
    private val titles: Array<String>,
    private val i: IOnCheckedChangeListener,
    private val selectedTitles: ArrayList<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val booleanArray = SparseBooleanArray(titles.size)

    /**
     * selectedTitles更新后，通过调用此方法更新UI
     */
    fun updateBooleanArrayAfterSelectedTitles() {
        booleanArray.clear()
        selectedTitles.forEachIndexed { _, s ->
            booleanArray.put(titles.indexOf(s), true)
        }
        i.onCheckChanged(isSelectAll(), selectedTitles)
        notifyItemRangeChanged(0, titles.size, "updateSelect")
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return HolderData(
            LayoutInflater.from(XFrame.getContext()).inflate(
                R.layout.item_rv_checkable_view,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
//            holder.itemView.checkedTextView_item.setTextColor(ContextCompat.getColor(XFrame.getContext(),R.color.leftSelectableTextColor))
            holder.itemView.checkedTextView_item.isChecked =
                booleanArray[holder.itemView.checkedTextView_item.tag as Int]
        } else super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val checkedItem = holder.itemView.checkedTextView_item
        checkedItem.tag = position
        checkedItem.text = titles[position]
        checkedItem.isChecked =
            booleanArray.get(checkedItem.tag as Int)
        holder.itemView.linear_layout_item_checkable.setOnClickListener {
            checkedItem.isChecked = !checkedItem.isChecked
            val checked = checkedItem.isChecked
            booleanArray.put(checkedItem.tag as Int, checked)
            if (checked) {
                if (selectedTitles.indexOf(titles[position]) == -1) {
                    selectedTitles.add(titles[position])
                    i.onCheckChanged(isSelectAll(), selectedTitles)
                }
            } else {
                selectedTitles.remove(titles[position])
                i.onCheckChanged(false, selectedTitles)
            }
        }
    }

    fun isSelectAll(): Boolean {
        return titles.size == selectedTitles.size
    }

    fun toggleSelectAll() {
        if (isSelectAll()) {
            clearAllSelect()
        } else {
            selectAll()
        }
    }

    fun selectAll() {
        titles.forEachIndexed { index, _ ->
            booleanArray.put(index, true)
            selectedTitles.clear()
            selectedTitles.addAll(titles)
        }
        i.onCheckChanged(true, selectedTitles)
        notifyItemRangeChanged(0, titles.size, "updateSelect")
    }

    fun clearAllSelect() {
        booleanArray.clear()
        selectedTitles.clear()
        i.onCheckChanged(false, selectedTitles)
        notifyItemRangeChanged(0, titles.size, "updateSelect")
    }

    interface IOnCheckedChangeListener {
        fun onCheckChanged(selectAll: Boolean, selectedTitles: ArrayList<String>)
    }

}