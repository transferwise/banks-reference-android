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
package com.transferwise.banks.shared

import com.transferwise.banks.api.data.TransferSummary

internal sealed class NavigationAction {
    data class ToAnonymousQuote(val customerId: Int, val quote: QuoteDescription) : NavigationAction()
    data class ToAnonymousSelectCurrency(val customerId: Int, val quote: QuoteDescription) : NavigationAction()
    data class ToConnectAccount(val customerId: Int, val quote: QuoteDescription) : NavigationAction()
    data class ToLogin(val url: String, val quote: QuoteDescription) : NavigationAction()
    data class ToQuote(val customerId: Int, val quote: QuoteDescription) : NavigationAction()
    data class ToSelectCurrency(val customerId: Int, val quote: QuoteDescription) : NavigationAction()
    data class ToCreateRecipient(val customerId: Int, val quoteId: String, val targetCurrency: String) : NavigationAction()

    data class ToRecipients(val customerId: Int, val quoteId: String, val targetCurrency: String) : NavigationAction()
    data class ToExtraDetails(val customerId: Int, val transferSummary: TransferSummary) : NavigationAction()
    data class ToConfirmation(
        val customerId: Int,
        val transferSummary: TransferSummary,
        val details: TransferDetails = TransferDetails()
    ) : NavigationAction()

    data class ToPaymentSent(val amount: Float, val currency: String, val recipient: String) : NavigationAction()
    object Done : NavigationAction()
}
