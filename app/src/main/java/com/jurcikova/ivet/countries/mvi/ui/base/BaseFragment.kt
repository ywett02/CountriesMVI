package com.jurcikova.ivet.countries.mvi.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.jurcikova.ivet.countries.mvi.mvibase.MviView
import com.jurcikova.ivet.countries.mvi.mvibase.MviViewState
import com.jurcikova.ivet.countries.mvi.mvibase.MviIntent

abstract class BaseFragment<VB: ViewDataBinding, I: MviIntent, S: MviViewState> : Fragment(), MviView<I, S> {

    //delegate the binding initialization to BindFragment delegate
    abstract val binding : VB

    abstract fun initViews()

    /**
     *  Start the stream by passing [MviIntent] to [MviViewModel]
     */
    abstract fun startStream()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startStream()
    }

    protected fun showMessage(message: String) {
        activity?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }
}