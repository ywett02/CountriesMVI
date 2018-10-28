package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jurcikova.ivet.mvi.databinding.ItemCountryPropertyBinding

class CountryPropertyAdapter : ListAdapter<CountryProperty, CountryPropertyViewHolder>(CountryPropertyDiffCallback()) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryPropertyViewHolder =
		CountryPropertyViewHolder(ItemCountryPropertyBinding.inflate(LayoutInflater.from(parent.context), parent, false))

	override fun onBindViewHolder(holder: CountryPropertyViewHolder, position: Int) {
		getItem(position)?.let { countryProperty ->
			holder.bind(countryProperty)
		}
	}
}

class CountryPropertyViewHolder(val itemBinding: ItemCountryPropertyBinding) : RecyclerView.ViewHolder(itemBinding.root) {
	fun bind(item: CountryProperty?) {
		itemBinding.countryProperty = item
		itemBinding.executePendingBindings()
	}
}

class CountryPropertyDiffCallback : DiffUtil.ItemCallback<CountryProperty>() {
	override fun areContentsTheSame(oldItem: CountryProperty, newItem: CountryProperty): Boolean = oldItem == newItem

	override fun areItemsTheSame(oldItem: CountryProperty, newItem: CountryProperty): Boolean = oldItem == newItem
}

data class CountryProperty(val imageResource: Int, val title: String?, val subtitle: String? = null)