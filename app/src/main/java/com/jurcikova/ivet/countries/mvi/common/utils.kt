package com.jurcikova.ivet.countries.mvi.common

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.jurcikova.ivet.mvi.R
import io.reactivex.Observable
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.annotations.SchedulerSupport
import java.util.concurrent.TimeUnit

@CheckReturnValue
@SchedulerSupport(SchedulerSupport.NONE)
fun <T : Any, U : Any> Observable<T>.notOfType(clazz: Class<U>): Observable<T> {
    checkNotNull(clazz) { "clazz is null" }
    return filter { !clazz.isInstance(it) }
}

/**
 * Emit an event immediately, then emit an other event after a delay has passed.
 * It is used for time limited UI state (e.g. a snackbar) to allow the stream to control
 * the timing for the showing and the hiding of a UI component.
 *
 * @param immediate Immediately emitted event
 * @param delayed   Event emitted after a delay
 */
fun <T> pairWithDelay(immediate: T, delayed: T): Observable<T> {
    return Observable.timer(2, TimeUnit.SECONDS)
            .map { delayed }
            .startWith(immediate)
}

fun Fragment.navigate(actionId: Int, bundle: Bundle) {
    findNavController().navigate(actionId, bundle)
}

fun Activity.findNavController() =
        Navigation.findNavController(this, R.id.my_nav_host_fragment)

fun Fragment.hideKeyboard() {
    this.activity?.let { activity ->
        activity.currentFocus?.let { view ->
            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).also {
                it.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }
}

fun bundleOf(bundle: Pair<String, String>) =
        Bundle().apply {
            putString(bundle.first, bundle.second)
        }


