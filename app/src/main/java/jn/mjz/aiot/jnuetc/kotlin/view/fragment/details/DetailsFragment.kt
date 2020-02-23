package jn.mjz.aiot.jnuetc.kotlin.view.fragment.details

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.bigkoo.convenientbanner.listener.OnItemClickListener
import com.youth.xframe.utils.permission.XPermission
import com.youth.xframe.utils.permission.XPermission.OnPermissionListener
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.databinding.FragmentDetailsBinding
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data
import jn.mjz.aiot.jnuetc.kotlin.model.entity.eventbus.TaskOrdered
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil.HttpUtilCallBack
import jn.mjz.aiot.jnuetc.kotlin.view.activity.detail.DetailsActivity
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractFragmentWithDataBinding
import jn.mjz.aiot.jnuetc.kotlin.view.custom.LoadingDialog
import jn.mjz.aiot.jnuetc.kotlin.viewmodel.DetailsViewModel
import org.greenrobot.eventbus.EventBus
import java.util.*

class DetailsFragment : AbstractFragmentWithDataBinding() {
    private lateinit var fragmentDetailsBinding: FragmentDetailsBinding
    private lateinit var detailsViewModel: DetailsViewModel
    private var mListener: OnFragmentInteractionListener? =
        null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentDetailsBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return fragmentDetailsBinding.root
    }

    private fun order() {
        LoadingDialog.with(context!!).show()
        Data.queryById(
            detailsViewModel.data.value!!.id,
            object : HttpUtilCallBack<Data?> {
                override fun onResponse(result: Data) {
                    LoadingDialog.with(context!!).cancel()
                    if (result.state.toInt() == 0) {
                        val booleans = booleanArrayOf(false)
                        val dataBackUpString = result.toString()
                        AlertDialog.Builder(context!!).setCancelable(false)
                            .setTitle("${result.local} - ${result.id}")
                            .setNegativeButton(R.string.Cancel, null)
                            .setPositiveButton(
                                R.string.Order
                            ) { _, _ ->
                                LoadingDialog.with(context!!).cancel()
                                result.orderDate = System.currentTimeMillis()
                                result.repairer = App.getUser().userName
                                result.state = 1.toShort()
                                result.modify(
                                    dataBackUpString,
                                    object :
                                        HttpUtilCallBack<Data?> {
                                        override fun onResponse(o: Data) {
                                            LoadingDialog.with(context!!).cancel()
                                            activity!!.finish()
                                            XToast.success(getString(R.string.OrderSuccess))
                                            EventBus.getDefault()
                                                .post(
                                                    TaskOrdered(
                                                        0,
                                                        result,
                                                        (activity!! as DetailsActivity).position
                                                    )
                                                )
                                        }

                                        override fun onFailure(error: String) {
                                            LoadingDialog.with(context!!).cancel()
                                            XToast.error("${getString(R.string.OrderFail)}\n$error")
                                        }
                                    })
                            }
                            .setMultiChoiceItems(
                                R.array.open_qq_after_order_dialog,
                                booleans
                            ) { _, _, _ -> }
                            .show()
                    } else {
                        LoadingDialog.with(context!!).cancel()
                        XToast.warning("唉呀，有人抢先了...\n该报修单已被 " + result.repairer + " 处理")
                        EventBus.getDefault().post(
                            TaskOrdered(
                                0, result, activity!!.intent.getIntExtra(
                                    "position",
                                    -1
                                )
                            )
                        )
                    }
                }

                override fun onFailure(error: String) {
                    LoadingDialog.with(context!!).cancel()
                    XToast.error("${getString(R.string.OrderFail)}\n$error")
                }
            })
    }

    override fun onAttach(context: Context) {
        mListener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnFragmentInteractionListener"
            )
        }
        super.onAttach(context)
    }

    override fun onDetach() {
        mListener = null
        EventBus.getDefault().unregister(this)
        super.onDetach()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_details
    }

    override fun initData(savedInstanceState: Bundle?) {
        detailsViewModel = ViewModelProvider(activity!!).get(
            DetailsViewModel::class.java
        )
        detailsViewModel.modifyMode.observe(
            activity!!,
            Observer { modifyMode: Boolean? ->
                fragmentDetailsBinding.modifyMode = modifyMode
            }
        )
        detailsViewModel.data.observe(
            activity!!,
            Observer { data: Data? ->
                if (data != null) {
                    fragmentDetailsBinding.data = data
                }
            }
        )
        fragmentDetailsBinding.onLocalSelectListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val data =
                    fragmentDetailsBinding.data
                if (data != null) {
                    data.local = Data.GET_LOCALS()[position]
                    detailsViewModel.data.value = detailsViewModel.data.value
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        fragmentDetailsBinding.onCollegeSelectListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val data =
                    fragmentDetailsBinding.data
                if (data != null) {
                    data.college = Data.getColleges()[position]
                    detailsViewModel.data.value = detailsViewModel.data.value
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        fragmentDetailsBinding.onGradeSelectListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val data =
                    fragmentDetailsBinding.data
                if (data != null) {
                    data.grade = Data.GET_GRADES()[position]
                    detailsViewModel.data.value = detailsViewModel.data.value
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        fragmentDetailsBinding.onItemClickListener = OnItemClickListener { position: Int ->
            if (mListener != null) {
                mListener!!.onItemClick(position)
            }
        }
        fragmentDetailsBinding.modelTextWatcher = object : TextWatcher {
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
                detailsViewModel.data.value!!.model = s.toString()
                detailsViewModel.data.value = detailsViewModel.data.value
            }
        }
        fragmentDetailsBinding.messageTextWatcher = object : TextWatcher {
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
                detailsViewModel.data.value!!.message = s.toString()
                detailsViewModel.data.value = detailsViewModel.data.value
            }
        }
        fragmentDetailsBinding.nameTextWatcher = object : TextWatcher {
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
                detailsViewModel.data.value!!.name = s.toString()
                detailsViewModel.data.value = detailsViewModel.data.value
            }
        }
        fragmentDetailsBinding.telTextWatcher = object : TextWatcher {
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
                detailsViewModel.data.value!!.tel = s.toString()
                detailsViewModel.data.value = detailsViewModel.data.value
            }
        }
        fragmentDetailsBinding.qqTextWatcher = object : TextWatcher {
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
                detailsViewModel.data.value!!.qq = s.toString()
                detailsViewModel.data.value = detailsViewModel.data.value
            }
        }
        fragmentDetailsBinding.onClickListener = View.OnClickListener { v: View ->
            when (v.id) {
                R.id.button_details_tel -> XPermission.requestPermissions(
                    activity,
                    0,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    object : OnPermissionListener {
                        override fun onPermissionGranted() {
                            val intent = Intent()
                            intent.action = Intent.ACTION_DIAL
                            intent.data = Uri.parse(
                                String.format(
                                    Locale.getDefault(),
                                    "tel:%s",
                                    detailsViewModel.data.value!!.tel
                                )
                            )
                            startActivity(intent)
                        }

                        override fun onPermissionDenied() {
                            XPermission.showTipsDialog(activity)
                        }
                    })
                R.id.button_details_qq -> Data.openQq(
                    detailsViewModel.data.value!!.qq
                )
                R.id.button_details_order -> {
                    order()
                }
                else -> {
                }
            }
        }
    }

    override fun initView() {
    }

    interface OnFragmentInteractionListener {
        /**
         * 点击轮播图
         *
         * @param position 哪一个
         */
        fun onItemClick(position: Int)
    }
}