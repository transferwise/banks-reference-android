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
package com.transferwise.banks.recipients

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
import com.transferwise.banks.R
import com.transferwise.banks.databinding.FragmentGetRecipientsBinding
import com.transferwise.banks.util.sharedViewModel
import com.transferwise.banks.util.toast

/**
 * Please refer to the design guide for more information about each screen:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
internal class GetRecipientsFragment : Fragment() {

    private lateinit var binding: FragmentGetRecipientsBinding
    private val viewModel by viewModels<GetRecipientsViewModel> { getFactory() }
    private val adapter: GetRecipientsAdapter = GetRecipientsAdapter(::onRecipientSelected)

    val args: GetRecipientsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGetRecipientsBinding.inflate(inflater, container, false)

        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(context)
        binding.add.setOnClickListener { viewModel.doOnAddRecipient() }

        viewModel.uiState.observe(viewLifecycleOwner, Observer(::refreshUi))

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.doOnStart()
    }

    private fun refreshUi(state: GetRecipientsUiState) {
        when (state) {
            is GetRecipientsUiState.Select -> {
                binding.loading.isVisible = false
                binding.recyclerview.animate().alpha(1f)
                adapter.submitList(state.recipients)
            }
            is GetRecipientsUiState.Loading -> {
                binding.loading.isVisible = true
                binding.recyclerview.animate().alpha(0.5f)
            }
            is GetRecipientsUiState.Failed -> toast(R.string.getrecipients_failed)
        }
    }

    private fun onRecipientSelected(recipient: RecipientItem) {
        viewModel.doOnRecipientSelected(recipient)
    }

    private fun getFactory() = GetRecipientsViewModelFactory(sharedViewModel, args.customerId, args.quoteId, args.targetCurrency)
}
