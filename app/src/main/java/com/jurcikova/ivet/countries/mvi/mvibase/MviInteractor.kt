package com.jurcikova.ivet.countries.mvi.mvibase

import io.reactivex.ObservableTransformer

interface MviInteractor<A : MviAction, R : MviResult> {
    val actionProcessor: ObservableTransformer<A, R>
}