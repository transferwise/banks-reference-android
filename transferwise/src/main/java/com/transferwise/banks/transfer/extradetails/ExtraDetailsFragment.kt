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
package com.transferwise.banks.transfer.extradetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.transferwise.banks.databinding.FragmentExtraDetailsBinding
import com.transferwise.banks.util.sharedViewModel

/**
 * Note: this screen will be skipped, when there are no mandatory extra details.
 * (e.g. if the dynamic form directly moves from [DynamicFormState.Loading] to [DynamicFormState.Complete])
 *
 * Please refer to the design guide for more information about each screen:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
internal class ExtraDetailsFragment : Fragment() {

    private lateinit var binding: FragmentExtraDetailsBinding
    private val args: ExtraDetailsFragmentArgs by navArgs()
    private val webService get() = sharedViewModel.webService

    private val viewModel by viewModels<ExtraDetailsViewModel> { getFactory() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentExtraDetailsBinding.inflate(inflater, container, false)

        viewModel.uiState.observe(viewLifecycleOwner, Observer(::updateUi))

        binding.buttonContinue.setOnClickListener { viewModel.doOnSave() }
        return binding.root
    }

    private fun updateUi(state: ExtraDetailsUiState) = when (state) {
        ExtraDetailsUiState.Loading -> {
            binding.toolbar.visibility = View.INVISIBLE
            binding.buttonContinue.visibility = View.INVISIBLE
        }
        ExtraDetailsUiState.Idle -> {
            binding.toolbar.visibility = View.VISIBLE
            binding.buttonContinue.visibility = View.VISIBLE
        }
    }

    private fun getFactory() = ExtraDetailsViewModelFactory(
        ExtraDetailsWebService(webService, args.customerId, args.transferSummary.quoteId, args.transferSummary.recipientId), sharedViewModel, binding.dynamicForm.controller, args.customerId, args.transferSummary
    )

    override fun onStart() {
        super.onStart()
        viewModel.doOnStart()
    }
}
