package jn.mjz.aiot.jnuetc.kotlin.view.custom

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.youth.xframe.XFrame
import jn.mjz.aiot.jnuetc.kotlin.R

class ThemedSwipeRefreshLayout(context: Context, attributeSet: AttributeSet) :
    SwipeRefreshLayout(context, attributeSet) {
    init {
        setColorSchemeColors(
            ContextCompat.getColor(context, R.color.colorPrimary),
            XFrame.getColor(R.color.colorPrimaryDark),
            ContextCompat.getColor(context, R.color.colorAccent)
        )
    }

}