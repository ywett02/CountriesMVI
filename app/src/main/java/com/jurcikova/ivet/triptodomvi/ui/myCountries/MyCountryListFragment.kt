package com.jurcikova.ivet.triptodomvi.ui.myCountries

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jurcikova.ivet.triptodomvi.R
import com.jurcikova.ivet.triptodomvi.common.BindFragment
import com.jurcikova.ivet.triptodomvi.databinding.FragmentMyCountryListBinding
import com.jurcikova.ivet.triptodomvi.mvibase.MviIntent
import com.jurcikova.ivet.triptodomvi.mvibase.MviView
import com.strv.ktools.inject
import com.strv.ktools.logD
import io.reactivex.Observable

class MyCountryListFragment : Fragment(), MviView<MyCountryListIntent, MyCountryListViewState> {

    //delegate the binding initialization to BindFragment delegate
    private val binding: FragmentMyCountryListBinding by BindFragment(R.layout.fragment_my_country_list)

    private val viewModel: MyCountryListViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(MyCountryListViewModel::class.java)
    }

    private val myCountryAdapter by inject<MyCountryAdapter>()

    private val initialIntent = Observable.just(MyCountryListIntent.InitialIntent)

    private val swipeToRefreshIntent by lazy {
        binding.swiperefresh.refreshes()
                .map {
                    MyCountryListIntent.SwipeToRefresh
                }
    }

    private val changeStateOfCountryIntent = myCountryAdapter.countryStateChangeSubject.map {
        MyCountryListIntent.ChangeCountryStateIntent(it)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupListView()
        setupFabListener()
    }

    override fun intents(): Observable<MyCountryListIntent> = Observable.merge(
            initialIntent,
            swipeToRefreshIntent,
            changeStateOfCountryIntent
    )

    /**
     *  Start the stream by passing [MviIntent] to [MviViewModel]
     */
    private fun startStream() {
        // Pass the UI's intents to the ViewModel
        viewModel.processIntents(intents())
    }

    override fun render(state: MyCountryListViewState) {
        binding.model = state

        state.countryStateChange?.let { countryState ->
            showStateChange(countryState)
        }

        state.error?.let { error ->
            showErrorState(error)
        }
    }

    private fun setupListView() {
        binding.rvCountries.layoutManager = LinearLayoutManager(activity)
        binding.rvCountries.adapter = myCountryAdapter
    }


    private fun setupFabListener() {

    }

    private fun showStateChange(state: MyCountryListViewState.CountryState) {
        activity?.let {
            Toast.makeText(it, "Country was marked as ${it.getString(state.stringRes)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorState(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}