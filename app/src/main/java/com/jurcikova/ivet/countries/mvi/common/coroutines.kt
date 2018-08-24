package com.jurcikova.ivet.countries.mvi.common

import android.view.View
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

fun View.onClick(action: suspend () -> Unit) {
    setOnClickListener {
        launch(UI) {
            action.invoke()
        }
    }
}