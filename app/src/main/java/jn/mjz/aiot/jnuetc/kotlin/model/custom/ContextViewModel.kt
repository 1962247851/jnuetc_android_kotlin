package jn.mjz.aiot.jnuetc.kotlin.model.custom

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel

/**
 * Context aware [ViewModel].
 *
 *
 * Subclasses must have a constructor which accepts [Context] as the only parameter.
 *
 *
 */
open class ContextViewModel(@field:SuppressLint("StaticFieldLeak") private val context: Context) :
    ViewModel() {

    /**
     * Return the context.
     */
    fun getContext(): Context {
        return context
    }

}