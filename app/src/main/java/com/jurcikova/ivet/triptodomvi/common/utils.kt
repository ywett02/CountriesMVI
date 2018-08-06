package com.jurcikova.ivet.triptodomvi.common

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