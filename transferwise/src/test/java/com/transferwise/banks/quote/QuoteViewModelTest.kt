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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.banks.api.data.Quote
import com.transferwise.banks.quote.QuoteUiState.Available
import com.transferwise.banks.quote.QuoteUiState.Failed
import com.transferwise.banks.quote.api.QuoteWebService
import com.transferwise.banks.quote.navigation.AnonymousQuoteNavigationFactory
import com.transferwise.banks.quote.navigation.DefaultQuoteNavigationFactory
import com.transferwise.banks.quote.navigation.QuoteNavigationFactory
import com.transferwise.banks.shared.NavigationAction.ToConnectAccount
import com.transferwise.banks.shared.NavigationAction.ToRecipients
import com.transferwise.banks.shared.NavigationAction.ToSelectCurrency
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.testutils.TestCoroutineExtension
import com.transferwise.banks.testutils.TestLiveDataExtension
import com.transferwise.banks.util.TextProvider
import com.transferwise.banks.util.fake.FakeConfiguration
import com.transferwise.banks.util.fake.FakeTextProvider
import com.transferwise.dynamicform.api.ErrorConverter
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import retrofit2.Response

@ExtendWith(MockitoExtension::class, TestCoroutineExtension::class, TestLiveDataExtension::class)
class QuoteViewModelTest {

    @Mock
    private lateinit var webService: QuoteWebService

    @Nested
    @DisplayName("When screen opens")
    inner class ScreenOpens {

        @Test
        internal fun `show fetching with integer source amount when fixed source`() {
            val viewModel = viewModel(quote = sourceQuote(100f, "", ""))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).sourceAmount).isEqualTo("100")
        }

        @Test
        internal fun `show fetching with two decimal source amount when fixed source`() {
            val viewModel = viewModel(quote = sourceQuote(100.2f, "", ""))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).sourceAmount).isEqualTo("100.20")
        }

        @Test
        internal fun `show fetching with empty source amount when fixed target`() {
            val viewModel = viewModel(quote = targetQuote(100f, "", ""))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).sourceAmount).isEqualTo("")
        }

        @Test
        internal fun `show fetching with source currency`() {
            val viewModel = viewModel(quote = sourceQuote(0f, "GBP", ""))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).sourceCurrency).isEqualTo("GBP")
        }

        @Test
        internal fun `show fetching with source currency flag`() {
            val viewModel = viewModel(quote = sourceQuote(0f, "GBP", ""))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).sourceFlagUri).isEqualTo("file:///android_asset/gbp.png")
        }

        @Test
        internal fun `show fetching with target currency`() {
            val viewModel = viewModel(quote = sourceQuote(0f, "", "EUR"))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).targetCurrency).isEqualTo("EUR")
        }

        @Test
        internal fun `show fetching with no target amount when fixed source`() {
            val viewModel = viewModel(quote = sourceQuote(100f, "", "EUR"))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).targetAmount).isEqualTo("")
        }

        @Test
        internal fun `show fetching with integer target amount when fixed target`() {
            val viewModel = viewModel(quote = targetQuote(100f, "", "EUR"))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).targetAmount).isEqualTo("100")
        }

        @Test
        internal fun `show fetching with two decimal target amount when fixed target`() {
            val viewModel = viewModel(quote = targetQuote(100.2f, "", "EUR"))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).targetAmount).isEqualTo("100.20")
        }

        @Test
        internal fun `show fetching with target currency flag`() {
            val viewModel = viewModel(quote = sourceQuote(0f, "", "EUR"))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).targetFlagUri).isEqualTo("file:///android_asset/eur.png")
        }

        @Test
        internal fun `show fetching with unknown fee`() {
            val viewModel = viewModel(quote = sourceQuote(0f, "", "EUR"), textProvider = FakeTextProvider())

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).fee).isEqualTo("unknown fee")
        }

        @Test
        internal fun `show fetching with unknown rate`() {
            val viewModel = viewModel(quote = sourceQuote(0f, "", "EUR"), textProvider = FakeTextProvider())

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).rate).isEqualTo("unknown rate")
        }

        @Test
        internal fun `show fetching with no arrival time`() {
            val viewModel = viewModel(quote = sourceQuote(0f, "", "EUR"))

            assertThat((viewModel.uiState.value as QuoteUiState.Fetching).arrivalTime).isEqualTo("")
        }
    }

    @Nested
    inner class OnStart {

        @Test
        internal fun `should get quote and update ui`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote())
            val viewModel = viewModel(webService)

            viewModel.doOnStart()

            assertThat(viewModel.uiState.value).isInstanceOf(Available::class.java)
        }
    }

    @Nested
    inner class FixedSourceQuote {

        @Test
        internal fun `should contact webserver with source amount`() = runBlockingTest {
            viewModel(webService).doOnNewQuote(100f)

            verify(webService).getQuote(any(), check { assertThat(it.sourceAmount).isEqualTo(100f) })
        }

        @Test
        internal fun `should contact webserver with no target amount`() = runBlockingTest {
            viewModel(webService).doOnNewQuote(100f)

            verify(webService).getQuote(any(), check { assertThat(it.targetAmount).isNull() })
        }

        @Test
        internal fun `should contact webserver with customer id`() = runBlockingTest {
            viewModel(webService, customerId = 5).doOnNewQuote(0f)

            verify(webService).getQuote(eq(5), any())
        }

        @Test
        internal fun `should contact webserver with source currency`() = runBlockingTest {
            viewModel(webService, quote = sourceQuote(0f, "EUR", "")).doOnNewQuote(0f)

            verify(webService).getQuote(any(), check { assertThat(it.sourceCurrency).isEqualTo("EUR") })
        }

        @Test
        internal fun `should contact webserver with target currency`() = runBlockingTest {
            viewModel(webService, quote = sourceQuote(0f, "", "GBP")).doOnNewQuote(0f)

            verify(webService).getQuote(any(), check { assertThat(it.targetCurrency).isEqualTo("GBP") })
        }

        @Test
        internal fun `updates UI with integer source amount`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(sourceAmount = 100f))
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Available>().sourceAmount).isEqualTo("100")
        }

        @Test
        internal fun `updates UI with two decimal source amount`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(sourceAmount = 100.2f))
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Available>().sourceAmount).isEqualTo("100.20")
        }

        @Test
        internal fun `updates UI with source currency`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(sourceCurrency = "EUR"))
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Available>().sourceCurrency).isEqualTo("EUR")
        }

        @Test
        internal fun `updates UI with source currency flag`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(sourceCurrency = "EUR"))
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Available>().sourceFlagUri).isEqualTo("file:///android_asset/eur.png")
        }

        @Test
        internal fun `updates UI with target currency`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(targetCurrency = "USD"))
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Available>().targetCurrency).isEqualTo("USD")
        }

        @Test
        internal fun `updates UI with target currency flag`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(targetCurrency = "USD"))
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Available>().targetFlagUri).isEqualTo("file:///android_asset/usd.png")
        }

        @Test
        internal fun `updates UI with rate of four decimals accuracy`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(rate = 0.5024f))
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Available>().rate).isEqualTo("0.5024 guaranteed rate")
        }

        @Test
        internal fun `updates UI with target amount of two decimals accuracy`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(targetAmount = 33.3333333f))
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Available>().targetAmount).isEqualTo("33.33")
        }

        @Test
        internal fun `updates UI with fee of two decimal accuracy`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(sourceCurrency = "GBP", fee = 0.75f))
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Available>().fee).isEqualTo("0.75 GBP fee")
        }

        @Test
        internal fun `updates UI with arrival time`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(arrivalTime = "by November 4th"))
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Available>().arrivalTime).isEqualTo("Should arrive by November 4th")
        }

        @Test
        internal fun `show error when getting quote fails`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().errorMessage).isEqualTo("Not connected to network")
        }

        @Test
        internal fun `network unavailable error has integer source amount`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().sourceAmount).isEqualTo("100")
        }

        @Test
        internal fun `network unavailable error has two decimal source amount`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100.2f)

            assertThat(viewModel.lastState<Failed>().sourceAmount).isEqualTo("100.20")
        }

        @Test
        internal fun `network unavailable error has source currency`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService, quote = sourceQuote(100f, "GBP", ""))

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().sourceCurrency).isEqualTo("GBP")
        }

        @Test
        internal fun `network unavailable error has source flag uri`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService, quote = sourceQuote(100f, "GBP", ""))

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().sourceFlagUri).isEqualTo("file:///android_asset/gbp.png")
        }

        @Test
        internal fun `network unavailable error has no target amount`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().targetAmount).isEqualTo("")
        }

        @Test
        internal fun `network unavailable error has target currency`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService, quote = sourceQuote(100f, "", "EUR"))

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().targetCurrency).isEqualTo("EUR")
        }

        @Test
        internal fun `network unavailable error has target flag uri`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService, quote = sourceQuote(100f, "", "EUR"))

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().targetFlagUri).isEqualTo("file:///android_asset/eur.png")
        }

        @Test
        internal fun `network unavailable error has unknown fee`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().fee).isEqualTo("unknown fee")
        }

        @Test
        internal fun `network unavailable error has unknown rate`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().rate).isEqualTo("unknown rate")
        }

        @Test
        internal fun `network unavailable error has no arrival time`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().arrivalTime).isEqualTo("")
        }

        @Test
        internal fun `show error when quote amount null`() = runBlockingTest {
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(null)

            assertThat(viewModel.lastState<Failed>().errorMessage).isEqualTo("Invalid amount")
        }

        @Test
        internal fun `amount null error has no source amount`() = runBlockingTest {
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(null)

            assertThat(viewModel.lastState<Failed>().sourceAmount).isEqualTo("")
        }

        @Test
        internal fun `amount null error has source currency`() = runBlockingTest {
            val viewModel = viewModel(webService, quote = sourceQuote(100f, "GBP", ""))

            viewModel.doOnNewQuote(null)

            assertThat(viewModel.lastState<Failed>().sourceCurrency).isEqualTo("GBP")
        }

        @Test
        internal fun `amount null error has source flag uri`() = runBlockingTest {
            val viewModel = viewModel(webService, quote = sourceQuote(100f, "GBP", ""))

            viewModel.doOnNewQuote(null)

            assertThat(viewModel.lastState<Failed>().sourceFlagUri).isEqualTo("file:///android_asset/gbp.png")
        }

        @Test
        internal fun `amount null error has no target amount`() = runBlockingTest {
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(null)

            assertThat(viewModel.lastState<Failed>().targetAmount).isEqualTo("")
        }

        @Test
        internal fun `amount null error has target currency`() = runBlockingTest {
            val viewModel = viewModel(webService, quote = sourceQuote(100f, "", "EUR"))

            viewModel.doOnNewQuote(null)

            assertThat(viewModel.lastState<Failed>().targetCurrency).isEqualTo("EUR")
        }

        @Test
        internal fun `amount null error has target flag uri`() = runBlockingTest {
            val viewModel = viewModel(webService, quote = sourceQuote(100f, "", "EUR"))

            viewModel.doOnNewQuote(null)

            assertThat(viewModel.lastState<Failed>().targetFlagUri).isEqualTo("file:///android_asset/eur.png")
        }

        @Test
        internal fun `amount null error has unknown fee`() = runBlockingTest {
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(null)

            assertThat(viewModel.lastState<Failed>().fee).isEqualTo("unknown fee")
        }

        @Test
        internal fun `amount null error has unknown rate`() = runBlockingTest {
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(null)

            assertThat(viewModel.lastState<Failed>().rate).isEqualTo("unknown rate")
        }

        @Test
        internal fun `amount null error has no arrival time`() = runBlockingTest {
            val viewModel = viewModel(webService)

            viewModel.doOnNewQuote(null)

            assertThat(viewModel.lastState<Failed>().arrivalTime).isEqualTo("")
        }

        @Test
        internal fun `server error has message`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenReturn(Response.error(400, "fake".toResponseBody()))
            val errorConverter = mock<ErrorConverter>()
            whenever(errorConverter.convert("fake")).thenReturn(mapOf("key" to "my server error"))
            val viewModel = viewModel(webService, errorConverter = errorConverter)

            viewModel.doOnNewQuote(100f)

            assertThat(viewModel.lastState<Failed>().errorMessage).isEqualTo("my server error")
        }
    }

    @Nested
    inner class FixedTargetQuote {

        @Test
        internal fun `should contact webserver with no source amount`() = runBlockingTest {
            viewModel(webService).doOnNewFixedTargetQuote(100f)

            verify(webService).getQuote(any(), check { assertThat(it.sourceAmount).isNull() })
        }

        @Test
        internal fun `should contact webserver with target amount`() = runBlockingTest {
            viewModel(webService).doOnNewFixedTargetQuote(100f)

            verify(webService).getQuote(any(), check { assertThat(it.targetAmount).isEqualTo(100f) })
        }

        @Test
        internal fun `network unavailable error has no source amount`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService)

            viewModel.doOnNewFixedTargetQuote(100f)

            assertThat(viewModel.lastState<Failed>().sourceAmount).isEqualTo("")
        }

        @Test
        internal fun `network unavailable error has integer target amount`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService)

            viewModel.doOnNewFixedTargetQuote(100f)

            assertThat(viewModel.lastState<Failed>().targetAmount).isEqualTo("100")
        }

        @Test
        internal fun `network unavailable error has two decimal target amount`() = runBlockingTest {
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            val viewModel = viewModel(webService)

            viewModel.doOnNewFixedTargetQuote(100.2f)

            assertThat(viewModel.lastState<Failed>().targetAmount).isEqualTo("100.20")
        }
    }

    @Nested
    @DisplayName("When continue button pressed")
    inner class Continue {

        @Test
        internal fun `don't navigate to recipients when error quote`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val viewModel = viewModel(webService, sharedViewModel)
            whenever(webService.getQuote(any(), any())).thenThrow(RuntimeException::class.java)
            viewModel.doOnNewQuote(100f)

            viewModel.doOnContinue()

            assertThat(sharedViewModel.navigationAction.value).isNull()
        }

        @Test
        internal fun `navigate to recipients when valid quote`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val viewModel = viewModel(webService, sharedViewModel, navigation = DefaultQuoteNavigationFactory())
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(id = "quoteId"))
            viewModel.doOnNewQuote(100f)

            viewModel.doOnContinue()

            assertThat((sharedViewModel.navigationAction.value as ToRecipients).quoteId).isEqualTo("quoteId")
        }

        @Test
        internal fun `navigate to recipients with customer id`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val viewModel = viewModel(webService, sharedViewModel, 5, navigation = DefaultQuoteNavigationFactory())
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(id = "quoteId"))
            viewModel.doOnNewQuote(100f)

            viewModel.doOnContinue()

            assertThat((sharedViewModel.navigationAction.value as ToRecipients).customerId).isEqualTo(5)
        }

        @Test
        internal fun `navigate to recipients with target currency`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val quote = sourceQuote(0f, "", "GBP")
            val viewModel = viewModel(webService, sharedViewModel, quote = quote, navigation = DefaultQuoteNavigationFactory())
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(id = "quoteId"))
            viewModel.doOnNewQuote(0f)

            viewModel.doOnContinue()

            assertThat((sharedViewModel.navigationAction.value as ToRecipients).targetCurrency).isEqualTo("GBP")
        }

        @Test
        internal fun `navigate to connect account with source amount`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val viewModel = viewModel(webService, sharedViewModel, 5, navigation = AnonymousQuoteNavigationFactory())
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(id = "quoteId", sourceAmount = 100f))
            viewModel.doOnNewQuote(100f)

            viewModel.doOnContinue()

            assertThat((sharedViewModel.navigationAction.value as ToConnectAccount).quote.amount).isEqualTo(100f)
        }
    }

    @Nested
    @DisplayName("When select currency")
    inner class Currency {

        @Test
        internal fun `navigate to select currency with customer id`() {
            val sharedModel = SharedViewModel(FakeConfiguration)

            viewModel(sharedModel = sharedModel, customerId = 5, navigation = DefaultQuoteNavigationFactory())
                .doOnSelectCurrency()

            assertThat((sharedModel.navigationAction.value as ToSelectCurrency).customerId).isEqualTo(5)
        }

        @Test
        internal fun `navigate to select currency with latest amount`() = runBlockingTest {
            val sharedModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = sourceQuote(100f, "GBP", "EUR")
            val viewModel =
                viewModel(webService, sharedModel, quote = fakeQuote, navigation = DefaultQuoteNavigationFactory())
            whenever(webService.getQuote(any(), any())).thenReturn(testQuote(id = "quoteId", sourceAmount = 200f))
            viewModel.doOnNewQuote(200f)

            viewModel.doOnSelectCurrency()

            assertThat((sharedModel.navigationAction.value as ToSelectCurrency).quote.amount).isEqualTo(200f)
        }
    }

    private fun sourceQuote(amount: Float, sourceCurrency: String, targetCurrency: String) =
        QuoteDescription.FixedSource(amount, sourceCurrency, targetCurrency)

    private fun targetQuote(amount: Float, sourceCurrency: String, targetCurrency: String) =
        QuoteDescription.FixedTarget(amount, sourceCurrency, targetCurrency)

    private fun viewModel(
        webService: QuoteWebService = mock(),
        sharedModel: SharedViewModel = mock(),
        customerId: Int = 0,
        quote: QuoteDescription = sourceQuote(0f, "", ""),
        navigation: QuoteNavigationFactory = mock(),
        textProvider: TextProvider = FakeTextProvider(),
        errorConverter: ErrorConverter = mock()
    ) = QuoteViewModel(webService, sharedModel, customerId, quote, navigation, textProvider, errorConverter)

    internal fun <T> QuoteViewModel.lastState() = uiState.value as T
}

private fun testQuote(
    id: String = "",
    sourceCurrency: String = "",
    targetCurrency: String = "",
    sourceAmount: Float = 0f,
    rate: Float = 0f,
    arrivalTime: String = "",
    fee: Float = 0f,
    targetAmount: Float = 0f
) = Response.success(Quote(id, sourceCurrency, targetCurrency, sourceAmount, targetAmount, rate, fee, arrivalTime))
