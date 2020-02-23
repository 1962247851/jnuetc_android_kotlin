package jn.mjz.aiot.jnuetc.kotlin.view.custom

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.youth.xframe.utils.log.XLog
import jn.mjz.aiot.jnuetc.kotlin.R

/**
 * BaseToolbarButtonDialog
 *
 * @author qq1962247851
 * @date 2020/2/21 12:31
 */
abstract class BaseToolbarButtonDialog : AlertDialog {

    init {
        setContentView(R.layout.base_dialog)
    }

    protected constructor(context: Context) : super(context) {
        XLog.d("BaseToolbarButtonDialog 1个参数")
    }

    protected constructor(context: Context, themeResId: Int) : super(
        context,
        themeResId
    ) {
        XLog.d("BaseToolbarButtonDialog 2个参数")
    }

    protected constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener) {
        XLog.d("BaseToolbarButtonDialog 3个参数")
    }
}