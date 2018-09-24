package com.jurcikova.ivet.countries.mvi.ui.countryList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.mvi.databinding.ItemCountryBinding
import io.reactivex.BackpressureStrategy
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