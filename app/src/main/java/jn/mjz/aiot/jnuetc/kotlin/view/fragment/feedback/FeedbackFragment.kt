package jn.mjz.aiot.jnuetc.kotlin.view.fragment.feedback

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.youth.xframe.widget.XLoadingDialog
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.databinding.FragmentFeedbackBinding
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.User
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil.HttpUtilCallBack
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractFragmentWithDataBinding
import jn.mjz.aiot.jnuetc.kotlin.viewmodel.DetailsViewModel

class FeedbackFragment : AbstractFragmentWithDataBinding() {
    private lateinit var detailsViewModel: DetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentFeedbackBinding: FragmentFeedbackBinding =
            DataBindingUtil.inflate(inflater, layoutId, container, false)
        detailsViewModel =
            ViewModelProvider(activity!!).get(
                DetailsViewModel::class.java
            )
        detailsViewModel.modifyMode.observe(
            activity!!,
            Observer { modifyMode: Boolean? ->
                fragmentFeedbackBinding.modifyMode = modifyMode
            }
        )
        detailsViewModel.data.observe(
            activity!!,
            Observer { data: Data? ->
                if (data != null) {
                    fragmentFeedbackBinding.data = data
                }
            }
        )
        fragmentFeedbackBinding.onRepairerClick =
            View.OnClickListener { changeRepairer() }
        fragmentFeedbackBinding.feedbackMode = false
        fragmentFeedbackBinding.repairMessageTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                detailsViewModel.data.value!!.repairMessage = s.toString()
                detailsViewModel.data.value = detailsViewModel.data.value
            }
        }
        fragmentFeedbackBinding.onServiceSelectListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val data =
                    fragmentFeedbackBinding.data
                if (data != null) {
                    data.service = Data.GET_SERVICES()[position]
                    detailsViewModel.data.value = detailsViewModel.data.value
                }
            }

        }
        fragmentFeedbackBinding.onMarkSelectListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val data =
                    fragmentFeedbackBinding.data
                if (data != null) {
                    data.mark = Data.GET_MARKS()[position]
                    detailsViewModel.data.value = detailsViewModel.data.value
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        return fragmentFeedbackBinding.root
    }

    private fun changeRepairer() {
        XLoadingDialog.with(context).setCanceled(false).show()
        User.queryAllUser(object :
            HttpUtilCallBack<ArrayList<String>> {
            override fun onResponse(result: ArrayList<String>) {
                XLoadingDialog.with(context).cancel()
                val names = arrayOfNulls<String>(result.size)
                result.toArray<String>(names)
                val checkItems = BooleanArray(result.size)
                val repairerBuilder =
                    AlertDialog.Builder(context!!)
                repairerBuilder.setTitle("请选择一个或多个维修人")
                    .setMultiChoiceItems(
                        names,
                        checkItems
                    ) { _: DialogInterface?, _: Int, _: Boolean -> }
                    .setPositiveButton("确定", null)
                    .setNeutralButton("取消", null)
                val dialog = repairerBuilder.create()
                dialog.show()
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener {
                        val names1 = StringBuilder()
                        for (j in checkItems.indices) {
                            if (checkItems[j]) {
                                names1.append(result[j])
                                names1.append("，")
                            }
                        }
                        if (names1.toString().isEmpty()) {
                            XToast.info("未选择")
                        } else {
                            names1.deleteCharAt(names1.length - 1)
                            val repairer = names1.toString()
                            if (repairer.isNotEmpty()) {
                                detailsViewModel.data.value!!.repairer = repairer
                                dialog.dismiss()
                            } else {
                                XToast.info("请检查维修人")
                            }
                        }
                    }
            }

            override fun onFailure(error: String) {
                XLoadingDialog.with(context).cancel()
                XToast.error("获取用户列表失败\n$error")
            }

        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_feedback
    }

    override fun initData(savedInstanceState: Bundle?) {}
    override fun initView() {}
}