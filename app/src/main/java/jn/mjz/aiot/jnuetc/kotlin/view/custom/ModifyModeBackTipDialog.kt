package jn.mjz.aiot.jnuetc.kotlin.view.custom

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import jn.mjz.aiot.jnuetc.kotlin.R

/**
 * LoadingDialog
 *
 * @author qq1962247851
 * @date 2020/2/18 14:19
 */
class ModifyModeBackTipDialog(context: Context) : AlertDialog(context) {
    override fun dismiss() {
        if (dialog != null) {
            dialog = null
        }
        super.dismiss()
    }

    companion object {
        private var dialog: ModifyModeBackTipDialog? = null
        @JvmStatic
        fun with(
            context: Context,
            clickExitListener: DialogInterface.OnClickListener
        ): ModifyModeBackTipDialog {
            if (dialog == null) {
                dialog = ModifyModeBackTipDialog(context)
                dialog!!.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    context.getString(R.string.Exit),
                    clickExitListener
                )
            }
            return dialog!!
        }
    }

    init {
        setTitle(R.string.ExitInputDialogTip)
        setMessage(context.getString(R.string.ModifyWillNotSave))
        setButton(
            DialogInterface.BUTTON_POSITIVE, context.getString(R.string.Cancel)
        ) { _, _ -> cancel() }
    }
}