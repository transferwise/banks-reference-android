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
package com.transferwise.banks.recipients.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.transferwise.banks.R
import com.transferwise.banks.databinding.FragmentCreateRecipientBinding
import com.transferwise.banks.recipients.create.CreateRecipientUiState.Error
import com.transferwise.banks.recipients.create.CreateRecipientUiState.Idle
import com.transferwise.banks.recipients.create.CreateRecipientUiState.Saving
import com.transferwise.banks.util.sharedViewModel
import com.transferwise.banks.util.toast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Please refer to the design guide for more information about each screen:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
internal class CreateRecipientFragment : Fragment() {

    private lateinit var binding: FragmentCreateRecipientBinding
    private val viewModel by viewModels<CreateRecipientViewModel> { getFactory() }

    val args: CreateRecipientFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCreateRecipientBinding.inflate(inflater)

        binding.save.setOnClickListener {
            binding.dynamicForm.clearFocus()
            viewModel.saveRecipient()
        }

        viewModel.uiState.observe(viewLifecycleOwner, Observer(::refreshUi))

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.doOnStart()
    }

    private fun refreshUi(state: CreateRecipientUiState) = when (state) {
        is Error -> {
            binding.dynamicForm.animate().alpha(1f)
            binding.save.alpha = 1f
            binding.progressBarSave.isVisible = false
            showError()
        }
        is Saving -> {
            binding.dynamicForm.animate().alpha(0.5f)
            binding.save.alpha = 0f
            binding.progressBarSave.isVisible = true
            Unit
        }
        Idle -> {
            binding.dynamicForm.animate().alpha(1f)
            binding.save.alpha = 1f
            binding.progressBarSave.isVisible = false
        }
    }

    private fun showError() = toast(R.string.create_recipient_failed)

    private fun getFactory(): CreateRecipientViewModelFactory = CreateRecipientViewModelFactory(
        sharedViewModel, binding.dynamicForm.controller, resources, args.customerId, args.quoteId, args.targetCurrency
    )
}
