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
package com.transferwise.banks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.transferwise.banks.InternationalTransferActivity.Companion.createIntent
import com.transferwise.banks.account.CheckAccountFragmentArgs
import com.transferwise.banks.account.connect.ConnectAccountFragmentDirections
import com.transferwise.banks.account.login.LoginConstants
import com.transferwise.banks.account.login.LoginConstants.DEEPLINK_PARAM_CODE
import com.transferwise.banks.quote.AnonymousQuoteFragmentDirections
import com.transferwise.banks.quote.QuoteFragmentDirections
import com.transferwise.banks.quote.currency.AnonymousSelectCurrencyFragmentDirections
import com.transferwise.banks.recipients.GetRecipientsFragmentDirections
import com.transferwise.banks.shared.NavigationAction
import com.transferwise.banks.shared.NavigationAction.Done
import com.transferwise.banks.shared.NavigationAction.ToAnonymousQuote
import com.transferwise.banks.shared.NavigationAction.ToAnonymousSelectCurrency
import com.transferwise.banks.shared.NavigationAction.ToConfirmation
import com.transferwise.banks.shared.NavigationAction.ToConnectAccount
import com.transferwise.banks.shared.NavigationAction.ToCreateRecipient
import com.transferwise.banks.shared.NavigationAction.ToExtraDetails
import com.transferwise.banks.shared.NavigationAction.ToLogin
import com.transferwise.banks.shared.NavigationAction.ToPaymentSent
import com.transferwise.banks.shared.NavigationAction.ToQuote
import com.transferwise.banks.shared.NavigationAction.ToRecipients
import com.transferwise.banks.shared.NavigationAction.ToSelectCurrency
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.shared.SharedViewModelFactory
import com.transferwise.banks.transfer.confirmation.ConfirmationFragmentDirections

/**
 * The [InternationalTransferActivity] handles the full international payments flow. Start this
 * class using a direct explicit intent or use [createIntent] to pass in extra configuration.
 */
class InternationalTransferActivity : AppCompatActivity() {

    internal lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(theme)
        setContentView(R.layout.activity_international_transfer)

        viewModel = ViewModelProvider(this, SharedViewModelFactory(backendConfiguration)).get(SharedViewModel::class.java)

        val startArgs = CheckAccountFragmentArgs(customerId, quote).toBundle()
        findNavController(R.id.fragment_navigation_host).setGraph(R.navigation.international_transfer_graph, startArgs)

        viewModel.navigationAction.observe(this, Observer(::navigate))
    }

    private fun navigate(action: NavigationAction) = when (action) {
        is ToLogin -> {
            storeQuoteForHandleLoginDeepLink(action)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(action.url)))
        }
        is ToAnonymousQuote -> {
            navigate(AnonymousSelectCurrencyFragmentDirections.toAnonymousQuote(action.customerId, action.quote))
        }
        is ToAnonymousSelectCurrency -> {
            navigate(AnonymousQuoteFragmentDirections.toAnonymousSelectCurrency(action.customerId, action.quote))
        }
        is ToConnectAccount -> {
            navigate(AnonymousQuoteFragmentDirections.toConnectAccount(action.customerId, action.quote))
        }
        is ToQuote -> {
            navigate(ConnectAccountFragmentDirections.toQuote(action.customerId, action.quote))
        }
        is ToSelectCurrency -> {
            navigate(QuoteFragmentDirections.toSelectCurrency(action.customerId, action.quote))
        }
        is ToCreateRecipient -> {
            navigate(GetRecipientsFragmentDirections.toCreateRecipient(action.customerId, action.quoteId, action.targetCurrency))
        }
        is ToRecipients -> {
            navigate(QuoteFragmentDirections.toGetRecipients(action.customerId, action.quoteId, action.targetCurrency))
        }
        is ToExtraDetails -> {
            navigate(GetRecipientsFragmentDirections.toExtraDetails(action.customerId, action.transferSummary))
        }
        is ToConfirmation -> {
            navigate(GetRecipientsFragmentDirections.toConfirmation(action.customerId, action.transferSummary, action.details))
        }
        is ToPaymentSent -> {
            navigate(ConfirmationFragmentDirections.toPaymentSent(action.amount, action.currency, action.recipient))
        }
        is Done -> finish()
    }

    /**
     * Handle the deeplink result of a user login. This isn't handled by the navigation library as that
     * library will always clear the back stack. This implementation however keeps the back stack intact.
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.scheme == LoginConstants.DEEPLINK_SCHEME) {
            val code = intent.data!!.getQueryParameter(DEEPLINK_PARAM_CODE) ?: "invalid"
            navigate(ConnectAccountFragmentDirections.toHandleLogin(customerId, quote, code))
        }
    }

    private fun navigate(direction: NavDirections) = findNavController(R.id.fragment_navigation_host).navigate(direction)

    /**
     * To keep the reference implementation simple, there is no cache/storage. Therefore store the current
     * configuration in the activity intent, so it can be restored after the deeplink result.
     */
    private fun storeQuoteForHandleLoginDeepLink(it: ToLogin) = intent.putExtra(QUOTE_DESCRIPTION, it.quote)

    private val backendConfiguration get() = intent.getParcelableExtra<BackendConfiguration>(BACKEND_CONFIGURATION)
    private val customerId get() = intent.getIntExtra(CUSTOMER_ID, -1)
    private val quote get() = intent.getParcelableExtra<QuoteDescription>(QUOTE_DESCRIPTION)
    private val theme: Int get() = intent.getIntExtra(THEME, R.style.BanksTheme)

    companion object {

        fun createIntent(
            context: Context,
            backendConfiguration: BackendConfiguration,
            customerId: Int,
            sourceCurrency: String,
            targetCurrency: String,
            quoteAmount: Int,
            @StyleRes theme: Int = R.style.BanksTheme
        ) =
            Intent(context, InternationalTransferActivity::class.java).apply {
                putExtra(BACKEND_CONFIGURATION, backendConfiguration)
                putExtra(CUSTOMER_ID, customerId)
                putExtra(QUOTE_DESCRIPTION, QuoteDescription.FixedSource(quoteAmount.toFloat(), sourceCurrency, targetCurrency))
                putExtra(THEME, theme)
            }

        private const val BACKEND_CONFIGURATION = "backendConfiguration"
        private const val CUSTOMER_ID = "customerId"
        private const val QUOTE_DESCRIPTION = "quoteDescription"
        private const val THEME = "theme"
    }
}
