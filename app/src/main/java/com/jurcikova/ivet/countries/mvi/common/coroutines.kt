package com.jurcikova.ivet.countries.mvi.common

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.withContext

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

suspend fun <T> ReceiveChannel<T>.consumeEachOnUI(action: (T) -> Unit) = onUI {
	consumeEach(action)
}

suspend fun onUI(block: suspend CoroutineScope.() -> Unit) =
	withContext(Dispatchers.Main, block)


