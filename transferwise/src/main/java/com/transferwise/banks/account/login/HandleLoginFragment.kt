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
package com.transferwise.banks.account.login

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
import com.transferwise.banks.account.AccountViewModelFactory
import com.transferwise.banks.account.login.HandleLoginUiState.LoggingIn
import com.transferwise.banks.databinding.FragmentHandleLoginBinding
import com.transferwise.banks.util.sharedViewModel

/**
 * Please refer to the design guide for more information about each screen:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
internal class HandleLoginFragment : Fragment() {

    private val viewModel by viewModels<HandleLoginViewModel> { getFactory() }
    private lateinit var binding: FragmentHandleLoginBinding
    private val args: HandleLoginFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHandleLoginBinding.inflate(inflater, container, false)
        binding.back.setOnClickListener { viewModel.doGoBack() }

        viewModel.uiState.observe(viewLifecycleOwner, Observer(::updateUi))

        return binding.root
    }

    private fun updateUi(uiState: HandleLoginUiState) = when (uiState) {
        is LoggingIn -> {
            binding.progress.isVisible = true
            binding.fail.isVisible = false
            binding.status.text = getString(R.string.handle_login_progress)
            binding.back.isVisible = false
        }
        is HandleLoginUiState.Failed -> {
            binding.progress.isVisible = false
            binding.fail.isVisible = true
            binding.status.text = getString(R.string.handle_login_error)
            binding.back.isVisible = true
        }
        is HandleLoginUiState.NoNetwork -> {
            binding.progress.isVisible = false
            binding.fail.isVisible = true
            binding.status.text = getString(R.string.error_no_network)
            binding.back.isVisible = true
        }
    }

    private fun getFactory() = AccountViewModelFactory(sharedViewModel, args.customerId, args.quote, args.code)
}
