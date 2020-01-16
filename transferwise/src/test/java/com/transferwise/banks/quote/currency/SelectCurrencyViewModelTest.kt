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

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.data.Currency
import com.transferwise.banks.quote.currency.SelectCurrencyUiState.Currencies
import com.transferwise.banks.quote.currency.SelectCurrencyUiState.Failed
import com.transferwise.banks.quote.navigation.DefaultQuoteNavigationFactory
import com.transferwise.banks.quote.navigation.QuoteNavigationFactory
import com.transferwise.banks.shared.NavigationAction.ToQuote
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.testutils.TestCoroutineExtension
import com.transferwise.banks.testutils.TestLiveDataExtension
import com.transferwise.banks.util.fake.FakeConfiguration
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class, TestCoroutineExtension::class, TestLiveDataExtension::class)
internal class SelectCurrencyViewModelTest {

    @Mock
    lateinit var webService: BanksWebService

    @Nested
    @DisplayName("When screen opens")
    inner class OnStart {

        @Test
        internal fun `get currencies`() = runBlockingTest {
            viewModel(webService)

            verify(webService).getCurrencies()
        }

        @Test
        internal fun `show currencies with name`() = runBlockingTest {
            whenever(webService.getCurrencies()).thenReturn(listOf(Currency("British pound", "GBP")))
            val viewModel = viewModel(webService)

            assertThat(viewModel.currenciesState.items[0].name).isEqualTo("British pound")
        }

        @Test
        internal fun `show currencies with code`() = runBlockingTest {
            whenever(webService.getCurrencies()).thenReturn(listOf(Currency("", "gbp")))
            val viewModel = viewModel(webService)

            assertThat(viewModel.currenciesState.items[0].code).isEqualTo("GBP")
        }

        @Test
        internal fun `show currencies with image uri`() = runBlockingTest {
            whenever(webService.getCurrencies()).thenReturn(listOf(Currency("", "GBP")))
            val viewModel = viewModel(webService)

            assertThat(viewModel.currenciesState.items[0].imageUri).isEqualTo("file:///android_asset/gbp.png")
        }

        @Test
        internal fun `don't show source currency`() = runBlockingTest {
            whenever(webService.getCurrencies()).thenReturn(listOf(Currency("", "GBP"), Currency("", "EUR")))
            val quote = sourceQuote(0f, "GBP", "USD")
            val viewModel = viewModel(webService, quote = quote)

            assertThat(viewModel.currenciesState.items[0].code).isEqualTo("EUR")
        }

        @Test
        internal fun `show failed when any exception`() = runBlockingTest {
            whenever(webService.getCurrencies()).thenThrow(RuntimeException())
            val viewModel = viewModel(webService)

            assertThat(viewModel.uiState.value).isEqualTo(Failed)
        }
    }

    @Nested
    @DisplayName("When currency selected")
    inner class CurrencySelected {

        @Test
        internal fun `go back to quote with customer id`() {
            val sharedModel = SharedViewModel(FakeConfiguration)
            val viewModel = viewModel(sharedModel = sharedModel, customerId = 5, navigation = DefaultQuoteNavigationFactory())

            viewModel.doOnCurrencySelected(CurrencyItem("", "", ""))

            assertThat((sharedModel.navigationAction.value as ToQuote).customerId).isEqualTo(5)
        }

        @Test
        internal fun `go back to quote with source amount`() {
            val sharedModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = sourceQuote(100f, "", "")
            val viewModel = viewModel(sharedModel = sharedModel, quote = fakeQuote, navigation = DefaultQuoteNavigationFactory())

            viewModel.doOnCurrencySelected(CurrencyItem("", "", ""))

            assertThat((sharedModel.navigationAction.value as ToQuote).quote.amount).isEqualTo(100f)
        }

        @Test
        internal fun `go back to quote with source currency`() {
            val sharedModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = sourceQuote(0f, "EUR", "")
            val viewModel = viewModel(sharedModel = sharedModel, quote = fakeQuote, navigation = DefaultQuoteNavigationFactory())

            viewModel.doOnCurrencySelected(CurrencyItem("", "", ""))

            assertThat((sharedModel.navigationAction.value as ToQuote).quote.sourceCurrency).isEqualTo("EUR")
        }

        @Test
        internal fun `go back to quote with selected target currency`() {
            val sharedModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = sourceQuote(100f, "", "USD")
            val viewModel = viewModel(sharedModel = sharedModel, quote = fakeQuote, navigation = DefaultQuoteNavigationFactory())

            viewModel.doOnCurrencySelected(CurrencyItem("", "GBP", ""))

            assertThat((sharedModel.navigationAction.value as ToQuote).quote.targetCurrency).isEqualTo("GBP")
        }
    }

    @Nested
    @DisplayName("When back pressed")
    inner class BackPressed {

        @Test
        internal fun `go back to quote with customer id`() {
            val sharedModel = SharedViewModel(FakeConfiguration)
            val viewModel = viewModel(sharedModel = sharedModel, customerId = 5, navigation = DefaultQuoteNavigationFactory())

            viewModel.doOnBack()

            assertThat((sharedModel.navigationAction.value as ToQuote).customerId).isEqualTo(5)
        }

        @Test
        internal fun `go back to quote with previous quote`() {
            val sharedModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = sourceQuote(100f, "GBP", "USD")
            val viewModel = viewModel(sharedModel = sharedModel, quote = fakeQuote, navigation = DefaultQuoteNavigationFactory())

            viewModel.doOnBack()

            assertThat((sharedModel.navigationAction.value as ToQuote).quote).isEqualTo(fakeQuote)
        }
    }

    private fun sourceQuote(amount: Float, sourceCurrency: String, targetCurrency: String) =
        QuoteDescription.FixedSource(amount, sourceCurrency, targetCurrency)

    private fun viewModel(
        service: BanksWebService = mock(),
        sharedModel: SharedViewModel = mock(),
        customerId: Int = 0,
        quote: QuoteDescription = sourceQuote(0f, "", ""),
        navigation: QuoteNavigationFactory = mock()
    ) = SelectCurrencyViewModel(service, sharedModel, customerId, quote, navigation)
}

private val SelectCurrencyViewModel.currenciesState get() = uiState.value as Currencies
