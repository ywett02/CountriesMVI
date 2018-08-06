package com.jurcikova.ivet.triptodomvi.ui.myCountries

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jurcikova.ivet.triptodomvi.business.entity.MyCountry
import com.jurcikova.ivet.triptodomvi.databinding.ItemMyCountryBinding
import io.reactivex.subjects.PublishSubject

class MyCountryAdapter : ListAdapter<MyCountry, CountryViewHolder>(MyCountryDiffCallback()) {

    val countryStateChangeSubject = PublishSubject.create<MyCountry>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder =
            LayoutInflater.from(parent.context).let { inflater ->
                ItemMyCountryBinding.inflate(inflater, parent, false).let { itemCountryBinding ->
                    CountryViewHolder(itemCountryBinding)
                }
            }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        getItem(position).let { country ->
            holder.bind(country)
            holder.itemBinding.cbVisited.setOnCheckedChangeListener { _, checked ->
                countryStateChangeSubject.onNext(country.also {
                    it.visited = checked
                })
            }
        }
    }
}

class CountryViewHolder(val itemBinding: ItemMyCountryBinding) : RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(item: MyCountry) {
        itemBinding.country = item
        itemBinding.executePendingBindings()
    }
}

class MyCountryDiffCallback : DiffUtil.ItemCallback<MyCountry>() {
    override fun areItemsTheSame(oldItem: MyCountry, newItem: MyCountry): Boolean = oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: MyCountry, newItem: MyCountry): Boolean = oldItem == newItem
}