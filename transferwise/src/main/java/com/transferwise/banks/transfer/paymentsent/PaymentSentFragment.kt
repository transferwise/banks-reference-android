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
package com.transferwise.banks.transfer.paymentsent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.transferwise.banks.databinding.FragmentPaymentSentBinding
import com.transferwise.banks.util.sharedViewModel

/**
 * Please refer to the design guide for more information about each screen:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
internal class PaymentSentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentSentBinding
    private val viewModel by viewModels<PaymentSentViewModel> { getFactory() }

    private val args: PaymentSentFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPaymentSentBinding.inflate(inflater, container, false)

        viewModel.uiState.observe(viewLifecycleOwner, Observer { binding.summary.text = it.summary })
        binding.root.setOnClickListener { viewModel.doOnNextClicked() }

        return binding.root
    }

    private fun getFactory() = PaymentSentViewModelFactory(sharedViewModel, resources, args.amount, args.currency, args.recipient)
}
