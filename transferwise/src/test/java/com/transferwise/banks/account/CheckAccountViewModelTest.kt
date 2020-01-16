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
package com.transferwise.banks.account

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.banks.account.CheckAccountUiState.Checking
import com.transferwise.banks.account.CheckAccountUiState.Failed
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.data.Customer
import com.transferwise.banks.shared.NavigationAction.ToAnonymousQuote
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
internal class CheckAccountViewModelTest {

    @Mock
    lateinit var webService: BanksWebService

    @Nested
    @DisplayName("When screen opens")
    inner class ScreenOpen {

        @Test
        internal fun `show checking ui state`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            whenever(webService.getCustomer(any())).thenReturn(customer(true))
            val viewModel = viewModel(webService, sharedViewModel)

            assertThat(viewModel.uiState.value).isEqualTo(Checking)
        }

        @Test
        internal fun `get customer from web service with customer id`() = runBlockingTest {
            viewModel(webService, customerId = 5)

            verify(webService).getCustomer(5)
        }

        @Test
        internal fun `go to quote when connected account`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            whenever(webService.getCustomer(any())).thenReturn(customer(true))

            viewModel(webService, sharedViewModel)

            assertThat(sharedViewModel.navigationAction.value).isInstanceOf(ToQuote::class.java)
        }

        @Test
        internal fun `go to quote with customer id`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            whenever(webService.getCustomer(any())).thenReturn(customer(true))

            viewModel(webService, sharedViewModel, 5)

            assertThat((sharedViewModel.navigationAction.value as ToQuote).customerId).isEqualTo(5)
        }

        @Test
        internal fun `go to quote with quote`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")
            whenever(webService.getCustomer(any())).thenReturn(customer(true))

            viewModel(webService, sharedViewModel, quote = fakeQuote)

            assertThat((sharedViewModel.navigationAction.value as ToQuote).quote).isEqualTo(fakeQuote)
        }

        @Test
        internal fun `go to anonymous quote when no connected account`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            whenever(webService.getCustomer(any())).thenReturn(customer(false))

            viewModel(webService, sharedViewModel)

            assertThat(sharedViewModel.navigationAction.value).isInstanceOf(ToAnonymousQuote::class.java)
        }

        @Test
        internal fun `go to anonymous quote with customer id`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            whenever(webService.getCustomer(any())).thenReturn(customer(false))

            viewModel(webService, sharedViewModel, customerId = 5)

            assertThat(sharedViewModel.toAnonymousQuoteAction.customerId).isEqualTo(5)
        }

        @Test
        internal fun `go to anonymous quote with quote`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")
            whenever(webService.getCustomer(any())).thenReturn(customer(false))

            viewModel(webService, sharedViewModel, quote = fakeQuote)

            assertThat(sharedViewModel.toAnonymousQuoteAction.quote).isEqualTo(fakeQuote)
        }

        @Test
        internal fun `show failed ui state when network error`() = runBlockingTest {
            val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")
            whenever(webService.getCustomer(any())).thenThrow(RuntimeException())

            val viewModel = viewModel(webService, mock(), quote = fakeQuote)

            assertThat(viewModel.uiState.value).isEqualTo(Failed)
        }
    }

    private fun viewModel(
        service: BanksWebService = mock(),
        sharedViewModel: SharedViewModel = mock(),
        customerId: Int = 0,
        quote: QuoteDescription = mock()
    ) = CheckAccountViewModel(service, sharedViewModel, customerId, quote)
}

private val SharedViewModel.toAnonymousQuoteAction get() = navigationAction.value as ToAnonymousQuote

private fun customer(transferWiseAccountLinked: Boolean) = Customer(transferWiseAccountLinked)
