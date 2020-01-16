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
package com.transferwise.banks.account.connect

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.transferwise.banks.R
import com.transferwise.banks.account.AccountViewModelFactory
import com.transferwise.banks.account.connect.ConnectAccountUiState.AccountExists
import com.transferwise.banks.account.connect.ConnectAccountUiState.CreatingAccount
import com.transferwise.banks.account.connect.ConnectAccountUiState.Failed
import com.transferwise.banks.account.connect.ConnectAccountUiState.Idle
import com.transferwise.banks.account.connect.ConnectAccountUiState.NoNetwork
import com.transferwise.banks.databinding.FragmentConnectAccountBinding
import com.transferwise.banks.util.sharedViewModel
import com.transferwise.banks.util.toast

/**
 * Please refer to the design guide for more information about each screen:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
class ConnectAccountFragment : Fragment() {

    private lateinit var binding: FragmentConnectAccountBinding
    private val viewModel by viewModels<ConnectAccountViewModel> { getFactory() }
    private val args: ConnectAccountFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentConnectAccountBinding.inflate(inflater, container, false)

        binding.createAccount.setOnClickListener { viewModel.doOnCreateAccount() }
        binding.terms.movementMethod = LinkMovementMethod.getInstance()
        binding.loginAccount.text = coloredLoginText()
        binding.loginAccount.setOnClickListener { viewModel.doOnLogin() }

        viewModel.uiState.observe(viewLifecycleOwner, Observer(::updateUi))

        return binding.root
    }

    private fun updateUi(uiState: ConnectAccountUiState) = when (uiState) {
        Idle -> hideCreatingAccount()
        CreatingAccount -> showCreatingAccount()
        AccountExists -> {
            hideCreatingAccount()
            toast(R.string.create_account_exists)
        }
        Failed -> {
            hideCreatingAccount()
            toast(R.string.create_account_failed)
        }
        NoNetwork -> {
            hideCreatingAccount()
            toast(R.string.error_no_network)
        }
    }

    private fun hideCreatingAccount() {
        binding.creatingAccount.isVisible = false
        binding.createAccount.isClickable = true
        binding.createAccount.setText(R.string.create_account_create)
    }

    private fun showCreatingAccount() {
        binding.creatingAccount.isVisible = true
        binding.createAccount.isClickable = false
        binding.createAccount.text = ""
    }

    private fun coloredLoginText(): SpannableStringBuilder {
        val text = getString(R.string.create_account_login)
        val start = text.indexOf('?') + 1
        val color = ResourcesCompat.getColor(resources, R.color.secondaryTextColor, activity!!.theme)
        val accentColor = ResourcesCompat.getColor(resources, R.color.primaryColor, activity!!.theme)

        val spannable = SpannableStringBuilder(text)
        spannable.setSpan(ForegroundColorSpan(color), 0, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(accentColor), start, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun getFactory() = AccountViewModelFactory(sharedViewModel, args.customerId, args.quote)
}
