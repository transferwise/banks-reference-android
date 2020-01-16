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
package com.transferwise.banks.account

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
import com.transferwise.banks.databinding.FragmentCheckAccountBinding
import com.transferwise.banks.util.sharedViewModel

/**
 * [CheckAccountFragment] checks whether or not a customer already has a TransferWise account linked:
 *
 * - when linked: skip [AnonymousQuoteFragment] and [ConnectAccountFragment]
 * - when not linked: go to [AnonymousQuoteFragment] and after [ConnectAccountFragment]
 *
 * This is an additional screen that isn't present in the design guide:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
internal class CheckAccountFragment : Fragment() {

    private lateinit var binding: FragmentCheckAccountBinding
    private val viewModel by viewModels<CheckAccountViewModel> { getFactory() }
    private val args: CheckAccountFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCheckAccountBinding.inflate(inflater, container, false)
        viewModel.uiState.observe(viewLifecycleOwner, Observer(::updateUi))
        binding.retry.setOnClickListener { viewModel.checkAccount() }
        return binding.root
    }

    private fun updateUi(state: CheckAccountUiState) = when (state) {
        is CheckAccountUiState.Checking -> {
            binding.progress.isVisible = true
            binding.status.text = getString(R.string.check_account_progress)
            binding.fail.isVisible = false
            binding.retry.isVisible = false
        }
        CheckAccountUiState.Failed -> {
            binding.progress.isVisible = false
            binding.status.text = getString(R.string.check_account_failed)
            binding.fail.isVisible = true
            binding.retry.isVisible = true
        }
    }

    private fun getFactory() = AccountViewModelFactory(sharedViewModel, args.customerId, args.quote)
}
