package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.widget.Toast
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.common.onClick
import com.jurcikova.ivet.countries.mvi.ui.BaseFragment
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryDetailBinding
import com.strv.ktools.logMe
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.launch

class CountryDetailFragment : BaseFragment<FragmentCountryDetailBinding, CountryDetailIntent, CountryDetailViewState>() {

    private val viewModel: CountryDetailViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(CountryDetailViewModel::class.java)
    }

    override val binding: FragmentCountryDetailBinding by BindFragment(R.layout.fragment_country_detail)

    override val intents = actor<CountryDetailIntent> {
        for (intent in channel) {
            intent.logMe()
            viewModel.intentProcessor.send(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch(UI, parent = job) {
            viewModel.state.consume {
                for (state in this) {
                    state.logMe()
                    render(state)
                }
            }
        }

        setupIntents()
    }

    override fun setupIntents() {
        launch(UI, parent = job) {
            intents.send(CountryDetailIntent.InitialIntent(CountryDetailFragmentArgs.fromBundle(arguments).argCountryName))
        }

        binding.fabAdd.onClick {
            viewModel.state.value.let {
                if (it.isFavorite) intents.send(CountryDetailIntent.RemoveFromFavoriteIntent(it.country!!.name))
                else intents.send(CountryDetailIntent.AddToFavoriteIntent(it.country!!.name))
            }
        }
    }

    override fun initViews() {
    }

    override fun render(state: CountryDetailViewState) {
        binding.countryDetailViewState = state

        if (state.showMessage) {
            showMessage(state.isFavorite)
        }

        state.error?.let {
            showErrorMessage(it)
        }
    }

    private fun showMessage(isFavorite: Boolean) {
        activity?.let {
            Toast.makeText(it, "Country was marked as ${if (isFavorite) "favorite" else "not favorite"}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorMessage(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}