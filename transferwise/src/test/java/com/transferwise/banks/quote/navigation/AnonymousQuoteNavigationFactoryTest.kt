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

import com.transferwise.banks.shared.NavigationAction.ToAnonymousQuote
import com.transferwise.banks.shared.NavigationAction.ToAnonymousSelectCurrency
import com.transferwise.banks.shared.NavigationAction.ToConnectAccount
import com.transferwise.banks.shared.QuoteDescription
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AnonymousQuoteNavigationFactoryTest {

    @Test
    fun `to next screen navigates to connect account`() {
        val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")

        val navigationAction = AnonymousQuoteNavigationFactory().toNextScreen(5, fakeQuote, null)

        assertThat(navigationAction).isEqualTo(ToConnectAccount(5, fakeQuote))
    }

    @Test
    fun `to select currency navigates to anonymous select currency`() {
        val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")

        val navigationAction = AnonymousQuoteNavigationFactory().toSelectCurrency(5, fakeQuote)

        assertThat(navigationAction).isEqualTo(ToAnonymousSelectCurrency(5, fakeQuote))
    }

    @Test
    fun `to quote navigates to anonymous quote`() {
        val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")

        val navigationAction = AnonymousQuoteNavigationFactory().toQuote(5, fakeQuote)

        assertThat(navigationAction).isEqualTo(ToAnonymousQuote(5, fakeQuote))
    }
}
