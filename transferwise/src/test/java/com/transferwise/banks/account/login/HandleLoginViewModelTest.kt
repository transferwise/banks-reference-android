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
package com.transferwise.banks.account.login

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.banks.account.login.HandleLoginUiState.Failed
import com.transferwise.banks.account.login.HandleLoginUiState.LoggingIn
import com.transferwise.banks.account.login.HandleLoginUiState.NoNetwork
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.shared.NavigationAction.ToConnectAccount
import com.transferwise.banks.shared.NavigationAction.ToQuote
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.testutils.TestCoroutineExtension
import com.transferwise.banks.testutils.TestLiveDataExtension
import com.transferwise.banks.util.fake.FakeConfiguration
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.internal.EMPTY_RESPONSE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import retrofit2.Response

@ExtendWith(MockitoExtension::class, TestCoroutineExtension::class, TestLiveDataExtension::class)
internal class HandleLoginViewModelTest {

    @Mock
    lateinit var webService: BanksWebService

    @Nested
    @DisplayName("When screen open")
    inner class Open {

        @Test
        internal fun `show login state`() = runBlockingTest {
            whenever(webService.logIn(any(), any())).thenReturn(Response.success(null))
            val viewModel = viewModel(webService, SharedViewModel(FakeConfiguration), code = "uniqueCode")

            assertThat(viewModel.uiState.value).isEqualTo(LoggingIn)
        }

        @Test
        internal fun `login with code`() = runBlockingTest {
            viewModel(webService, code = "uniqueCode")

            verify(webService).logIn(any(), eq("uniqueCode"))
        }

        @Test
        internal fun `login with customer id`() = runBlockingTest {
            viewModel(webService, customerId = 5)

            verify(webService).logIn(eq(5), any())
        }

        @Test
        internal fun `navigate to quote when login success`() = runBlockingTest {
            val sharedModel = SharedViewModel(FakeConfiguration)
            whenever(webService.logIn(any(), any())).thenReturn(Response.success(null))

            viewModel(webService, sharedModel)

            assertThat(sharedModel.navigationAction.value).isInstanceOf(ToQuote::class.java)
        }

        @Test
        internal fun `navigate to quote with customer id`() = runBlockingTest {
            val sharedModel = SharedViewModel(FakeConfiguration)
            whenever(webService.logIn(any(), any())).thenReturn(Response.success(null))

            viewModel(webService, sharedModel, 5)

            assertThat((sharedModel.navigationAction.value as ToQuote).customerId).isEqualTo(5)
        }

        @Test
        internal fun `navigate to quote with source amount`() = runBlockingTest {
            val sharedModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")
            whenever(webService.logIn(any(), any())).thenReturn(Response.success(null))

            viewModel(webService, sharedModel, quote = fakeQuote)

            assertThat((sharedModel.navigationAction.value as ToQuote).quote).isEqualTo(fakeQuote)
        }

        @Test
        internal fun `don't navigate to quote when login failed`() = runBlockingTest {
            val sharedModel = SharedViewModel(FakeConfiguration)
            whenever(webService.logIn(any(), any())).thenReturn(Response.error(400, EMPTY_RESPONSE))

            viewModel(webService, sharedModel)

            assertThat(sharedModel.navigationAction.value).isNull()
        }

        @Test
        internal fun `show error when login failed`() = runBlockingTest {
            whenever(webService.logIn(any(), any())).thenReturn(Response.error(400, EMPTY_RESPONSE))

            val viewModel = viewModel(webService)

            assertThat(viewModel.uiState.value).isEqualTo(Failed)
        }

        @Test
        internal fun `show no network when network error`() = runBlockingTest {
            whenever(webService.logIn(any(), any())).thenThrow(RuntimeException())

            val viewModel = viewModel(webService)

            assertThat(viewModel.uiState.value).isEqualTo(NoNetwork)
        }
    }

    @Nested
    @DisplayName("When go back")
    inner class Back {

        @Test
        internal fun `navigate to connect account`() {
            val sharedModel = SharedViewModel(FakeConfiguration)

            viewModel(webService, sharedModel).doGoBack()

            assertThat(sharedModel.navigationAction.value).isInstanceOf(ToConnectAccount::class.java)
        }

        @Test
        internal fun `navigate to connect account with customer id`() {
            val sharedModel = SharedViewModel(FakeConfiguration)

            viewModel(webService, sharedModel, 5).doGoBack()

            assertThat((sharedModel.navigationAction.value as ToConnectAccount).customerId).isEqualTo(5)
        }

        @Test
        internal fun `navigate to connect account with source amount`() {
            val sharedModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")

            viewModel(webService, sharedModel, quote = fakeQuote).doGoBack()

            assertThat((sharedModel.navigationAction.value as ToConnectAccount).quote).isEqualTo(fakeQuote)
        }
    }

    private fun viewModel(
        service: BanksWebService = mock(),
        sharedViewModel: SharedViewModel = mock(),
        customerId: Int = 0,
        quote: QuoteDescription = mock(),
        code: String = ""
    ) = HandleLoginViewModel(service, sharedViewModel, customerId, quote, code)
}
