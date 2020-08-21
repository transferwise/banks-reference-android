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
package com.transferwise.banks.quote

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.transferwise.banks.quote.api.AnonymousQuoteWebService
import com.transferwise.banks.quote.api.DefaultQuoteWebService
import com.transferwise.banks.quote.currency.SelectCurrencyViewModel
import com.transferwise.banks.quote.navigation.AnonymousQuoteNavigationFactory
import com.transferwise.banks.quote.navigation.DefaultQuoteNavigationFactory
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.util.LocalizedTextProvider

internal class QuoteViewModelFactory(
    private val sharedViewModel: SharedViewModel,
    private val resources: Resources,
    private val customerId: Int,
    private val quote: QuoteDescription,
    private val anonymous: Boolean
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
            return QuoteViewModel(
                createQuoteWebService(),
                sharedViewModel,
                customerId,
                quote,
                createNavigationFactory(),
                createTextProvider()
            ) as T
        } else if (modelClass.isAssignableFrom(SelectCurrencyViewModel::class.java)) {
            return SelectCurrencyViewModel(
                sharedViewModel.webService,
                sharedViewModel,
                customerId,
                quote,
                createNavigationFactory()
            ) as T
        }
        throw RuntimeException("Can't create ViewModel - Unsupported Viewmodel class")
    }

    private fun createTextProvider() = if (anonymous) AnonymousQuoteLocalizedTextProvider(resources) else LocalizedTextProvider(resources)
    private fun createNavigationFactory() = if (anonymous) AnonymousQuoteNavigationFactory() else DefaultQuoteNavigationFactory()
    private fun createQuoteWebService() =
        if (anonymous) AnonymousQuoteWebService(sharedViewModel.webService) else DefaultQuoteWebService(sharedViewModel.webService)
}
