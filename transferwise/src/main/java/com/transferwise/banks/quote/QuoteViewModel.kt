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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transferwise.banks.api.request.QuoteRequest
import com.transferwise.banks.quote.api.QuoteWebService
import com.transferwise.banks.quote.navigation.QuoteNavigationFactory
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.util.TextProvider
import com.transferwise.dynamicform.api.ErrorConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class QuoteViewModel(
    private val webService: QuoteWebService,
    private val sharedViewModel: SharedViewModel,
    private val customerId: Int,
    quote: QuoteDescription,
    private val navigationFactory: QuoteNavigationFactory,
    private val textProvider: TextProvider,
    private val errorConverter: ErrorConverter = ErrorConverter()
) : ViewModel() {

    val uiState = MutableLiveData<QuoteUiState>()
    private var currentQuote = quote

    init {
        uiState.value = quote.asFetchingState(textProvider)
    }

    fun doOnStart() = viewModelScope.cancelPreviousAndRun {
        getQuote(currentQuote)
    }

    fun doOnNewQuote(amount: Float?) = viewModelScope.cancelPreviousAndRun {
        if (amount == null) invalidAmountResult() else getQuote(currentQuote.toFixedSource(amount))
    }

    fun doOnNewFixedTargetQuote(amount: Float?) = viewModelScope.cancelPreviousAndRun {
        if (amount == null) invalidAmountResult() else getQuote(currentQuote.toFixedTarget(amount))
    }

    fun doOnContinue() =
        sharedViewModel.navigationAction.setValue(navigationFactory.toNextScreen(customerId, currentQuote, currentQuote.id))

    fun doOnSelectCurrency() =
        sharedViewModel.navigationAction.setValue(navigationFactory.toSelectCurrency(customerId, currentQuote))

    private suspend fun getQuote(quote: QuoteDescription): GetQuoteResult {
        currentQuote = quote
        uiState.value = currentQuote.asFetchingState(textProvider)
        return downloadQuote(quote)
    }

    private suspend fun downloadQuote(quote: QuoteDescription) = try {
        val response = webService.getQuote(customerId, quote.toQuoteRequest())
        if (response.isSuccessful) {
            val result = response.body()!!
            GetQuoteResult(result.asAvailableState(textProvider), currentQuote.copyWithId(result.id))
        } else {
            val error = errorConverter.convert(response.errorBody()?.string()).values.first()
            GetQuoteResult(quote.asErrorState(error, textProvider), currentQuote)
        }
    } catch (e: Exception) {
        GetQuoteResult(quote.asErrorState(textProvider.networkError(), textProvider), currentQuote)
    }

    private fun QuoteDescription.toQuoteRequest() = when (this) {
        is QuoteDescription.FixedSource -> QuoteRequest(amount, null, currentQuote.sourceCurrency, currentQuote.targetCurrency)
        is QuoteDescription.FixedTarget -> QuoteRequest(null, amount, currentQuote.sourceCurrency, currentQuote.targetCurrency)
    }

    private fun invalidAmountResult() = GetQuoteResult(currentQuote.asInvalidAmountState(textProvider), currentQuote)

    var previousJob: Job? = null

    /**
     * Having more than one quote request open to the backend isn't just inefficient, but it could
     * also cause race conditions e.g.:
     *
     * 1. Get quote for 100$
     * 2. Get quote for 1000$ (just add 1 extra digit)
     * 3. Result for 1000$ returns first
     * 4. Result for 100$ returns second => UI is in incorrect state
     *
     * Hence enforcing quotes to be processed "one at a time" removes this race condition and also
     * allows the "continue" button to be disabled until the last quote request was successful.
     *
     * This does pose a Threading problem as the result of a cancelled coroutine should no longer
     * be show in the UI or stored in a data object. Therefore the [cancelPreviousAndRun] accepts a
     * block that returns a [GetQuoteResult] that contains the next state and an updated quote.
     * Only when the coroutine hasn't been cancelled, these are applied.
     */
    private fun CoroutineScope.cancelPreviousAndRun(block: suspend () -> GetQuoteResult) {
        previousJob?.cancel()
        previousJob = launch {
            val result = block()
            if (isActive) {
                currentQuote = result.quote
                uiState.value = result.uiState
            }
        }
    }
}

private data class GetQuoteResult(
    val uiState: QuoteUiState,
    val quote: QuoteDescription
)
