package com.jurcikova.ivet.countries.mvi.common

import android.databinding.BindingAdapter
import android.graphics.drawable.PictureDrawable
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.jurcikova.ivet.mvi.R
import com.strv.ktools.logMe

@BindingAdapter("show")
fun View.setShow(show: Boolean) {
    if (parent is ViewGroup)
        TransitionManager.beginDelayedTransition(parent as ViewGroup)
    visibility = if (show) View.VISIBLE else View.GONE
}

@Suppress("UNCHECKED_CAST")
@BindingAdapter("list")
fun <E> RecyclerView.setList(list: List<E>?) {
    list.logMe()
    list?.let {
        (adapter as ListAdapter<E, *>?)?.submitList(it)
    }
}

@BindingAdapter("svg")
fun ImageView.setSvgResource(url: String?) {
    url?.let {
        GlideApp.with(context)
                .`as`(PictureDrawable::class.java)
                .listener(SvgSoftwareLayerSetter())
                .placeholder(R.drawable.world)
                .error(R.drawable.world)
                .load(it)
                .into(this)
    }
}