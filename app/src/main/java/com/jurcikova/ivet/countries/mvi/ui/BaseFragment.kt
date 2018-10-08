package com.jurcikova.ivet.countries.mvi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.jurcikova.ivet.countries.mvi.common.AndroidJob
import com.jurcikova.ivet.countries.mvi.mvibase.MviIntent
import com.jurcikova.ivet.countries.mvi.mvibase.MviView
import com.jurcikova.ivet.countries.mvi.mvibase.MviViewState
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.channels.Channel
import kotlin.coroutines.experimental.CoroutineContext


abstract class BaseFragment<VB : ViewDataBinding, I : MviIntent, S : MviViewState> : Fragment(), CoroutineScope, MviView<I, S> {

    protected val job = AndroidJob(lifecycle)

    //delegate the binding initialization to BindFragment delegate
    protected abstract val binding: VB

    protected abstract fun initViews()

    protected abstract fun setupIntents()

    override val intents = Channel<I>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startStream()
        setupIntents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.destroy()
    }

    /**
     *  Start the stream by passing [MviIntent] to [MviViewModel]
     */
    protected abstract fun startStream()
}