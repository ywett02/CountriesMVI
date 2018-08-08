package com.jurcikova.ivet.triptodomvi.ui.countryDetail

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jakewharton.rxbinding2.view.RxView
import com.jurcikova.ivet.triptodomvi.R
import com.jurcikova.ivet.triptodomvi.common.BindFragment
import com.jurcikova.ivet.triptodomvi.databinding.FragmentCountryDetailBinding
import com.jurcikova.ivet.triptodomvi.mvibase.MviView
import com.strv.ktools.logD
import io.reactivex.Observable

class CountryDetailFragment : Fragment(), MviView<CountryDetailIntent, CountryDetailViewState> {

    //delegate the binding initialization to BindFragment delegate
    private val binding: FragmentCountryDetailBinding by BindFragment(R.layout.fragment_country_detail)

    private val viewModel: CountryDetailViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(CountryDetailViewModel::class.java)
    }

    private var isCountryFavorite = false

    private val initialIntent by lazy {
        Observable.just(CountryDetailIntent.InitialIntent(CountryDetailFragmentArgs.fromBundle(arguments).argCountryName) as CountryDetailIntent)
    }

    private val favoriteButtonClickedIntent by lazy {
        RxView.clicks(binding.fabAdd).map {
            if (!isCountryFavorite) CountryDetailIntent.AddToFavoriteIntent(CountryDetailFragmentArgs.fromBundle(arguments).argCountryName)
            else CountryDetailIntent.RemoveFavoriteIntent(CountryDetailFragmentArgs.fromBundle(arguments).argCountryName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.states().observe(this, Observer { state ->
            logD("state: $state")

            render(state!!)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startStream()
    }

    override fun intents(): Observable<CountryDetailIntent> = Observable.merge(
            initialIntent,
            favoriteButtonClickedIntent
    )

    override fun render(state: CountryDetailViewState) {
        binding.countryDetailViewState = state

        isCountryFavorite = state.isFavorite

        if (state.showMessage) {
            showMessage(state.isFavorite)
        }
    }

    private fun showMessage(isFavorite: Boolean) {
        activity?.let {
            Toast.makeText(it, "Country was marked as ${if (isFavorite) "favorite" else "not favorite"}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startStream() {
        viewModel.processIntents(intents())
    }
}