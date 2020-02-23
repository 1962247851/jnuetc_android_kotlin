package jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * @author qq1962247851
 * @date 2020/1/20 16:37
 */
class WrapContentStaggeredGridLayoutManager : StaggeredGridLayoutManager {

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(spanCount: Int, orientation: Int) : super(spanCount, orientation)

    override fun onLayoutChildren(
        recycler: Recycler,
        state: RecyclerView.State
    ) {
        try {
            super.onLayoutChildren(recycler, state)
//        } catch (e: Exception) {
        } catch (e: IndexOutOfBoundsException) {
//            e.printStackTrace()
        } catch (e: NullPointerException) {
//            e.printStackTrace()
        }
    }
}