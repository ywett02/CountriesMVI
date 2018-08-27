package com.jurcikova.ivet.countries.mvi.ui

import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jurcikova.ivet.countries.mvi.common.AndroidJob
import com.jurcikova.ivet.countries.mvi.mvibase.MviIntent
import com.jurcikova.ivet.countries.mvi.mvibase.MviView
import com.jurcikova.ivet.countries.mvi.mvibase.MviViewState
import kotlinx.coroutines.experimental.channels.Channel

abstract class BaseFragment<VB : ViewDataBinding, I : MviIntent, S : MviViewState> : Fragment(), MviView<I, S> {

    protected val job = AndroidJob(lifecycle)

    //delegate the binding initialization to BindFragment delegate
    protected abstract val binding: VB

    protected abstract fun initViews()

    protected abstract fun setupIntents()

    override val intents = Channel<I>()

    /**
     *  Start the stream by passing [MviIntent] to [MviViewModel]
     */
    protected abstract fun startStream()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = binding.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startStream()
        setupIntents()
    }
}