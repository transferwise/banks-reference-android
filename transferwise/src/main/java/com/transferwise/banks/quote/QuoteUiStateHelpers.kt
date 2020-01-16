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

import com.transferwise.banks.api.data.Quote
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.util.TextProvider
import com.transferwise.banks.util.decimalsOrRound
import com.transferwise.banks.util.toCurrencyFlagUri

internal fun Quote.asAvailableState(textProvider: TextProvider) = QuoteUiState.Available(
    sourceAmount.decimalsOrRound(2),
    sourceCurrency,
    sourceCurrency.toCurrencyFlagUri(),
    targetAmount.decimalsOrRound(2),
    targetCurrency,
    targetCurrency.toCurrencyFlagUri(),
    textProvider.quoteFee(fee, sourceCurrency),
    textProvider.quoteRate(rate),
    textProvider.quoteArrivalTime(formattedEstimatedDelivery)
)

internal fun QuoteDescription.asInvalidAmountState(textProvider: TextProvider) = QuoteUiState.Failed(
    "",
    sourceCurrency,
    sourceCurrency.toCurrencyFlagUri(),
    "",
    targetCurrency,
    targetCurrency.toCurrencyFlagUri(),
    textProvider.quoteFeeUnknown(),
    textProvider.quoteRateUnknown(),
    "",
    textProvider.quoteInvalidAmount()
)

internal fun QuoteDescription.asErrorState(error: String, textProvider: TextProvider) = QuoteUiState.Failed(
    if (this is QuoteDescription.FixedSource) amount.decimalsOrRound(2) else "",
    sourceCurrency,
    sourceCurrency.toCurrencyFlagUri(),
    if (this is QuoteDescription.FixedTarget) amount.decimalsOrRound(2) else "",
    targetCurrency,
    targetCurrency.toCurrencyFlagUri(),
    textProvider.quoteFeeUnknown(),
    textProvider.quoteRateUnknown(),
    "",
    error
)

internal fun QuoteDescription.asFetchingState(textProvider: TextProvider) = QuoteUiState.Fetching(
    if (this is QuoteDescription.FixedSource) amount.decimalsOrRound(2) else "",
    sourceCurrency,
    sourceCurrency.toCurrencyFlagUri(),
    if (this is QuoteDescription.FixedTarget) amount.decimalsOrRound(2) else "",
    targetCurrency,
    targetCurrency.toCurrencyFlagUri(),
    textProvider.quoteFeeUnknown(),
    textProvider.quoteRateUnknown(),
    ""
)
