/*
 * Copyright 2019 TransferWise Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.transferwise.banks.quote.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.transferwise.banks.databinding.FragmentSelectCurrencyBinding
import com.transferwise.banks.quote.QuoteViewModelFactory
import com.transferwise.banks.quote.currency.SelectCurrencyUiState.Currencies
import com.transferwise.banks.quote.currency.SelectCurrencyUiState.Failed
import com.transferwise.banks.quote.currency.SelectCurrencyUiState.Loading
import com.transferwise.banks.util.sharedViewModel

/**
 * Base screen to select currency, there is a subclass [AnonymousSelectCurrencyFragment] to select a
 * currency in the anonymous quote flow.
 *
 * Please refer to the design guide for more information about each screen:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
internal open class SelectCurrencyFragment : Fragment() {

    private lateinit var binding: FragmentSelectCurrencyBinding
    private val viewModel by viewModels<SelectCurrencyViewModel> { getFactory() }
    private lateinit var adapter: SelectCurrencyAdapter
    private val args: SelectCurrencyFragmentArgs by navArgs()
    protected open val isAnonymous = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSelectCurrencyBinding.inflate(inflater, container, false)
        adapter = SelectCurrencyAdapter { viewModel.doOnCurrencySelected(it) }

        viewModel.uiState.observe(viewLifecycleOwner, Observer(::updateUi))

        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(context)
        binding.back.setOnClickListener { viewModel.doOnBack() }

        return binding.root
    }

    private fun updateUi(state: SelectCurrencyUiState) = when (state) {
        is Currencies -> {
            binding.loading.isVisible = false
            adapter.submitList(state.items)
            binding.error.isVisible = false
            binding.fail.isVisible = false
            binding.back.isVisible = false
        }
        is Loading -> {
            binding.loading.isVisible = true
            binding.error.isVisible = false
            binding.fail.isVisible = false
            binding.back.isVisible = false
        }
        is Failed -> {
            binding.loading.isVisible = false
            binding.error.isVisible = true
            binding.fail.isVisible = true
            binding.back.isVisible = true
        }
    }

    private fun getFactory() = QuoteViewModelFactory(sharedViewModel, resources, args.customerId, args.quote, isAnonymous)
}
