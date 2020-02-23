package jn.mjz.aiot.jnuetc.kotlin.model.custom

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import java.lang.reflect.InvocationTargetException

/**
 * ViewModelProvider.Factory which may create AndroidViewModel and
 * ViewModel, which have an empty constructor.
 */
class ContextViewModelFactory
/**
 * Creates a `ContextViewModelFactory`
 *
 * @param context an context to pass in [ContextViewModel]
 */(private val context: Context) : NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (ContextViewModel::class.java.isAssignableFrom(modelClass)) {
            try {
                val parameterTypeArray =
                    arrayOf<Class<*>>(
                        Context::class.java
                    )
                modelClass.getConstructor(*parameterTypeArray).newInstance(context)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InstantiationException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            }
        } else super.create(modelClass)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var sInstance: ContextViewModelFactory? = null

        /**
         * Retrieve a singleton instance of ContextViewModelFactory.
         *
         * @param context an context to pass in [ContextViewModel]
         * @return A valid [ContextViewModelFactory]
         */
        fun getInstance(context: Context): ContextViewModelFactory {
            if (sInstance == null) {
                sInstance = ContextViewModelFactory(context)
            }
            return sInstance!!
        }
    }

}