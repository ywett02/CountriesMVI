package com.jurcikova.ivet.countries.mvi.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

fun Fragment.navigate(actionId: Int, bundle: Bundle) {
    findNavController().navigate(actionId, bundle)
}

fun bundleOf(bundle: Pair<String, String>) =
        Bundle().apply {
            putString(bundle.first, bundle.second)
        }