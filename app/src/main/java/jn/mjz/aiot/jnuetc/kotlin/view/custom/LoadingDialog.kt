package jn.mjz.aiot.jnuetc.kotlin.view.custom

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import jn.mjz.aiot.jnuetc.kotlin.R

/**
 * LoadingDialog
 *
 * @author qq1962247851
 * @date 2020/2/18 14:19
 */
class LoadingDialog(context: Context) : AlertDialog(context) {
    override fun dismiss() {
        if (dialog != null) {
            dialog = null
        }
        super.dismiss()
    }

    companion object {
        private var dialog: LoadingDialog? = null
        @JvmStatic
        fun with(context: Context): LoadingDialog {
            if (dialog == null) {
                dialog = LoadingDialog(context)
            }
            return dialog!!
        }
    }

    init {
        setCancelable(false)
        setView(View.inflate(context, R.layout.loading_dialog, null))
    }
}