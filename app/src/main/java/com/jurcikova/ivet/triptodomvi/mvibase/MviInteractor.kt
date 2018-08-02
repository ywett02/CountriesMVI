package com.jurcikova.ivet.triptodomvi.mvibase

import io.reactivex.ObservableTransformer

interface MviInteractor<A : MviAction, R : MviResult> {
    val actionProcessor: ObservableTransformer<A, R>
}