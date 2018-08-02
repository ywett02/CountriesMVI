package com.jurcikova.ivet.triptodomvi.ui.countryList

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jurcikova.ivet.triptodomvi.R
import com.jurcikova.ivet.triptodomvi.common.BindFragment
import com.jurcikova.ivet.triptodomvi.databinding.FragmentCountryListBinding
import com.jurcikova.ivet.triptodomvi.mvibase.MviIntent
import com.jurcikova.ivet.triptodomvi.mvibase.MviView
import com.strv.ktools.inject
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class CountryListFragment : Fragment(), MviView<CountryListIntent, CountryListViewState> {

    private val viewModel: CountryListViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(CountryListViewModel::class.java)
    }

    //delegate the binding initialization to BindFragment delegate
    private val binding: FragmentCountryListBinding by BindFragment(R.layout.fragment_country_list)

    private val adapter by inject<CountryAdapter>()

    private val initialIntent by lazy {
        Observable.just(CountryListIntent.InitialIntent)
    }

    private val searchIntent by lazy {
        RxTextView.textChanges(binding.edSearch)
                //because after orientation change the passed value would be emitted
                .skip(2)
                .filter {
                    it.length > 2 || it.isEmpty()
                }
                .debounce(500, TimeUnit.MILLISECONDS)
                .map {
                    CountryListIntent.SearchIntent(it.toString())
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.states().observe(this, Observer {
            render(it!!)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = binding.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupListView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startStream()
    }

    override fun render(state: CountryListViewState) {
        binding.model = state

        if (state.error != null) {
            showErrorState(state.error)
        }
    }

    override fun intents() =
            Observable.merge(
                    initialIntent,
                    searchIntent
            )

    /**
     *  Start the stream by passing [MviIntent] to [MviViewModel]
     */
    private fun startStream() {
        // Pass the UI's intents to the ViewModel
        viewModel.processIntents(intents())
    }

    private fun setupListView() {
        binding.rvCountries.layoutManager = LinearLayoutManager(activity)
        binding.rvCountries.adapter = adapter
    }

    private fun showErrorState(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
