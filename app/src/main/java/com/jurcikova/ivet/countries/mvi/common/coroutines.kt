package com.jurcikova.ivet.countries.mvi.common

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

class AndroidJob(lifecycle: Lifecycle) : Job by Job(), LifecycleObserver {
	init {
		lifecycle.addObserver(this)
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
	fun destroy() = cancel()
}

fun View.setOnClick(scope: CoroutineScope, action: suspend () -> Unit) {
	// launch one actor as a parent of the context job
	val eventActor = scope.actor<Unit> {
		for (event in channel) action()
	}
	// install a listener to activate this actor
	setOnClickListener { eventActor.offer(Unit) }
}

