package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding2.view.RxView
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.ui.BaseFragment
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryDetailBinding
import com.strv.ktools.logD
import io.reactivex.Observable
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountryDetailFragment : BaseFragment<FragmentCountryDetailBinding, CountryDetailIntent, CountryDetailViewState>() {

    private val countryDetailViewModel: CountryDetailViewModel by viewModel()

    private val initialIntent by lazy {
        Observable.just(CountryDetailIntent.InitialIntent(CountryDetailFragmentArgs.fromBundle(arguments).argCountryName) as CountryDetailIntent)
    }

    private val favoriteButtonClickedIntent by lazy {
        RxView.clicks(binding.fabAdd).flatMap {
            countryDetailViewModel.statesStream().map {
                if (!it.isFavorite) CountryDetailIntent.AddToFavoriteIntent(it.country!!.name) else {
                    CountryDetailIntent.RemoveFavoriteIntent(it.country!!.name)
                }
            }.take(1)
        }
    }

    override val binding: FragmentCountryDetailBinding by BindFragment(R.layout.fragment_country_detail)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        countryDetailViewModel.states().observe(this, Observer { state ->
            logD("state: $state")

            render(state!!)
        })
    }

    override fun initViews() {
    }

    override fun startStream() {
        countryDetailViewModel.processIntents(intents())
    }

    override fun intents(): Observable<CountryDetailIntent> = Observable.merge(
            initialIntent,
            favoriteButtonClickedIntent
    )

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