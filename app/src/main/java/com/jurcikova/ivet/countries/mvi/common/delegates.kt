package com.jurcikova.ivet.countries.mvi.common

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import kotlin.reflect.KProperty

/**
 * This is a replacement for DataBindingUtil.setContentView(activity, layoutRes)
 *
 * use val binding: ActivityMainBinding by BindActivity(R.layout.activity_main) instead
 * Inpiration garimajain
 */
class BindActivity<in R : Activity, out T : ViewDataBinding>(
        @LayoutRes private val layoutRes: Int) {

    private var value: T? = null

    operator fun getValue(thisRef: R, property: KProperty<*>): T {
        if (value == null) {
            value = DataBindingUtil.setContentView<T>(thisRef, layoutRes)
        }
        return value!!
    }
}

/**
 * This is a replacement for DataBindingUtil.inflate(inflater, layoutRes,
 * rootView, boolean)
 *
 * use private val binding: FragmentMainBinding by BindFragment(R.layout.fragment_main) instead
 */
class BindFragment<in R : Fragment, out T : ViewDataBinding>(
        @LayoutRes private val layoutRes: Int) {

    private var value: T? = null

    operator fun getValue(thisRef: R, property: KProperty<*>): T {
        if (value == null) {
            value = DataBindingUtil.inflate<T>(thisRef.layoutInflater, layoutRes,
                    thisRef.view?.rootView as ViewGroup?, false)
        }
        return value!!
    }
}