package com.jurcikova.ivet.countries.mvi.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
		binding.setLifecycleOwner(this)

		binding.toolbar.let {
			it.title = title
			it.inflateMenu(R.menu.menu_search)
			it.setOnMenuItemClickListener { item ->
				NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(this, R.id.my_nav_host_fragment))
			}
		}
	}

	override fun onSupportNavigateUp() = findNavController(R.id.my_nav_host_fragment).navigateUp()
}
