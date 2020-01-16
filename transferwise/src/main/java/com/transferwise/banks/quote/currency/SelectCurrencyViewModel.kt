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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.data.Currency
import com.transferwise.banks.quote.currency.SelectCurrencyUiState.Currencies
import com.transferwise.banks.quote.currency.SelectCurrencyUiState.Failed
import com.transferwise.banks.quote.currency.SelectCurrencyUiState.Loading
import com.transferwise.banks.quote.navigation.QuoteNavigationFactory
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.util.toCurrencyFlagUri
import kotlinx.coroutines.launch

internal class SelectCurrencyViewModel(
    webService: BanksWebService,
    private val sharedViewModel: SharedViewModel,
    private val customerId: Int,
    private val quote: QuoteDescription,
    private val navigationFactory: QuoteNavigationFactory
) : ViewModel() {

    val uiState = MutableLiveData<SelectCurrencyUiState>()

    init {
        uiState.value = Loading
        viewModelScope.launch {
            try {
                val currencies = webService.getCurrencies()
                uiState.value = Currencies(currencies.filterNot { it.code == quote.sourceCurrency }.toItems())
            } catch (e: Throwable) {
                uiState.value = Failed
            }
        }
    }

    private fun List<Currency>.toItems() = map {
        CurrencyItem(it.name, it.code.toUpperCase(), it.code.toCurrencyFlagUri())
    }

    fun doOnCurrencySelected(currency: CurrencyItem) = sharedViewModel.navigationAction.setValue(
        navigationFactory.toQuote(customerId, quote.copyWithTargetCurrency(currency.code))
    )

    fun doOnBack() = sharedViewModel.navigationAction.setValue(navigationFactory.toQuote(customerId, quote))
}
