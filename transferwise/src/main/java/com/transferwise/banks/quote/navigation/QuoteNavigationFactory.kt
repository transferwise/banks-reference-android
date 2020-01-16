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
package com.transferwise.banks.quote.navigation

import com.transferwise.banks.shared.NavigationAction
import com.transferwise.banks.shared.QuoteDescription

/**
 * Allows for different navigation in the anonymous quote vs regular quote case.
 */
internal interface QuoteNavigationFactory {
    fun toNextScreen(customerId: Int, quote: QuoteDescription, quoteId: String?): NavigationAction
    fun toSelectCurrency(customerId: Int, quote: QuoteDescription): NavigationAction
    fun toQuote(customerId: Int, quote: QuoteDescription): NavigationAction
}
