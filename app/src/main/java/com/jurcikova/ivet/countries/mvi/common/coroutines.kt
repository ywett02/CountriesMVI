package com.jurcikova.ivet.countries.mvi.common

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.view.View
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consume

class AndroidJob(lifecycle: Lifecycle) : Job by Job(), LifecycleObserver {
    init {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() = cancel()
}

fun View.setOnClick(contextJob: Job, action: suspend () -> Unit) {
    // launch one actor as a parent of the context job
    val eventActor = actor<Unit>(context = UI,
            start = CoroutineStart.UNDISPATCHED,
            capacity = Channel.CONFLATED,
            parent = contextJob) {
        for (event in channel) action()
    }
    // install a listener to activate this actor
    setOnClickListener { eventActor.offer(Unit) }
}

suspend fun <E> ReceiveChannel<E>.consumeEach(action: (E) -> Unit) =
        consume {
            for (e in this) action(e)
        }

