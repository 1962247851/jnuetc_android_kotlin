package jn.mjz.aiot.jnuetc.kotlin.viewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.youth.xframe.XFrame
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.datachangelog.DataChangeLogFragment
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.details.DetailsFragment
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.feedback.FeedbackFragment
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.progress.ProgressFragment
import java.util.*

/**
 * @author qq1962247851
 * @date 2020/2/19 11:22
 */
class DetailsViewModel : ViewModel() {
    var dataStringBackup: String = ""
    val data = MutableLiveData<Data>()
    val modifyMode = MutableLiveData<Boolean>().apply {
        value = false
    }
    val fragmentList = LinkedList<Fragment>().apply {
        add(DetailsFragment())
        add(ProgressFragment())
        add(DataChangeLogFragment())
        add(FeedbackFragment())
    }
    val pageTitleList = LinkedList<String>().apply {
        add(XFrame.getString(R.string.Details))
        add(XFrame.getString(R.string.Progress))
        add(XFrame.getString(R.string.Log))
        add(XFrame.getString(R.string.Feedback))
    }
}