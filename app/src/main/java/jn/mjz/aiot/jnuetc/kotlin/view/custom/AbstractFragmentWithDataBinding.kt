package jn.mjz.aiot.jnuetc.kotlin.view.custom

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.youth.xframe.base.ICallback

/**
 * AbstractFragmentWithDataBinding，重写onCreateView
 * 
 * @author qq1962247851
 * @date 2020/1/29 19:08
 */
abstract class AbstractFragmentWithDataBinding : Fragment(), ICallback {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData(savedInstanceState)
        initView()
    }
}