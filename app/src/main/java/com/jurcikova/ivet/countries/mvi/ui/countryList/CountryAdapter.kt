package com.jurcikova.ivet.countries.mvi.ui.countryList

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.common.OnItemClickListener
import com.jurcikova.ivet.mvi.databinding.ItemCountryBinding

class CountryAdapter(val onItemClickListener: OnItemClickListener<Country>) : ListAdapter<Country, CountryViewHolder>(CountryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder =
            LayoutInflater.from(parent.context).let { inflater ->
                ItemCountryBinding.inflate(inflater, parent, false).let { itemCountryBinding ->
                    CountryViewHolder(itemCountryBinding)
                }
            }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        getItem(position).let { country ->
            holder.itemBinding.root.setOnClickListener {
                onItemClickListener.onItemClick(country)
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