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

import com.transferwise.banks.shared.NavigationAction.ToQuote
import com.transferwise.banks.shared.NavigationAction.ToRecipients
import com.transferwise.banks.shared.NavigationAction.ToSelectCurrency
import com.transferwise.banks.shared.QuoteDescription
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class DefaultQuoteNavigationFactoryTest {

    @Test
    fun `to next screen navigates to recipients`() {
        val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")

        val navigationAction = DefaultQuoteNavigationFactory().toNextScreen(5, fakeQuote, "quoteId")

        Assertions.assertThat(navigationAction).isEqualTo(ToRecipients(5, "quoteId", "EUR"))
    }

    @Test
    fun `to select currency navigates to select currency`() {
        val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")

        val navigationAction = DefaultQuoteNavigationFactory().toSelectCurrency(5, fakeQuote)

        Assertions.assertThat(navigationAction).isEqualTo(ToSelectCurrency(5, fakeQuote))
    }

    @Test
    fun `to quote navigates to quote`() {
        val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")

        val navigationAction = DefaultQuoteNavigationFactory().toQuote(5, fakeQuote)

        Assertions.assertThat(navigationAction).isEqualTo(ToQuote(5, fakeQuote))
    }
}
