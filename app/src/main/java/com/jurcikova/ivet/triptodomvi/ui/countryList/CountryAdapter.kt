package com.jurcikova.ivet.triptodomvi.ui.countryList

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jurcikova.ivet.triptodomvi.business.entity.Country
import com.jurcikova.ivet.triptodomvi.databinding.ItemCountryBinding
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class CountryAdapter : ListAdapter<Country, CountryViewHolder>(CountryDiffCallback()) {

    private val onClickSubject = PublishSubject.create<Country>()

    val countryClickObservable: LiveData<Country>
        get() = LiveDataReactiveStreams.fromPublisher(onClickSubject.toFlowable(BackpressureStrategy.BUFFER))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder =
            LayoutInflater.from(parent.context).let { inflater ->
                ItemCountryBinding.inflate(inflater, parent, false).let { itemCountryBinding ->
                    CountryViewHolder(itemCountryBinding)
                }
            }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        getItem(position).let { country ->
            holder.itemBinding.root.setOnClickListener {
                onClickSubject.onNext(country)
            }
            holder.bind(country)
        }
    }
}

class CountryViewHolder(val itemBinding: ItemCountryBinding) : RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(item: Country?) {
        itemBinding.country = item
        itemBinding.executePendingBindings()
    }
}

class CountryDiffCallback : DiffUtil.ItemCallback<Country>() {
    override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean = oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean = oldItem == newItem
}