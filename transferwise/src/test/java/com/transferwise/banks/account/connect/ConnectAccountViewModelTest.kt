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
package com.transferwise.banks.account.connect

import androidx.lifecycle.viewModelScope
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.banks.account.connect.ConnectAccountUiState.AccountExists
import com.transferwise.banks.account.connect.ConnectAccountUiState.CreatingAccount
import com.transferwise.banks.account.connect.ConnectAccountUiState.Failed
import com.transferwise.banks.account.connect.ConnectAccountUiState.Idle
import com.transferwise.banks.account.connect.ConnectAccountUiState.NoNetwork
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.shared.NavigationAction.ToLogin
import com.transferwise.banks.shared.NavigationAction.ToQuote
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.testutils.TestCoroutineExtension
import com.transferwise.banks.testutils.TestLiveDataExtension
import com.transferwise.banks.util.fake.FakeConfiguration
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import retrofit2.Response

@ExtendWith(MockitoExtension::class, TestCoroutineExtension::class, TestLiveDataExtension::class)
class ConnectAccountViewModelTest {

    @Mock
    private lateinit var webService: BanksWebService

    @Test
    internal fun `When screen created state is idle`() {
        assertThat(viewModel().uiState.value).isEqualTo(Idle)
    }

    @Nested
    @DisplayName("When create account")
    inner class Create {

        @Test
        internal fun `sign up user with customer id`() = runBlockingTest {
            viewModel(webService, customerId = 5).doOnCreateAccount()

            verify(webService).signUp(5)
        }

        @Test
        internal fun `navigate to quotes with customer id`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val error = Response.success(mock<ResponseBody>())
            whenever(webService.signUp(any())).thenReturn(error)

            viewModel(webService, sharedViewModel, 5).doOnCreateAccount()

            assertThat((sharedViewModel.navigationAction.value as ToQuote).customerId).isEqualTo(5)
        }

        @Test
        internal fun `navigate to quotes with quote`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")
            val error = Response.success(mock<ResponseBody>())
            whenever(webService.signUp(any())).thenReturn(error)

            viewModel(webService, sharedViewModel, quote = fakeQuote).doOnCreateAccount()

            assertThat((sharedViewModel.navigationAction.value as ToQuote).quote).isEqualTo(fakeQuote)
        }

        @Test
        internal fun `don't navigate to quotes when failed`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val error = Response.error<ResponseBody>(400, mock())
            whenever(webService.signUp(any())).thenReturn(error)

            viewModel(webService, sharedViewModel).doOnCreateAccount()

            assertThat(sharedViewModel.navigationAction.value).isNull()
        }

        @Test
        internal fun `show account exists when conflict`() = runBlockingTest {
            val error = Response.error<ResponseBody>(409, mock())
            whenever(webService.signUp(any())).thenReturn(error)
            val viewModel = viewModel(webService)

            viewModel.doOnCreateAccount()

            assertThat(viewModel.uiState.value).isEqualTo(AccountExists)
        }

        @Test
        internal fun `show failed when failed`() = runBlockingTest {
            val error = Response.error<ResponseBody>(400, mock())
            whenever(webService.signUp(any())).thenReturn(error)
            val viewModel = viewModel(webService)

            viewModel.doOnCreateAccount()

            assertThat(viewModel.uiState.value).isEqualTo(Failed)
        }

        @Test
        internal fun `show no network`() = runBlockingTest {
            whenever(webService.signUp(any())).thenThrow(RuntimeException())
            val viewModel = viewModel(webService)

            viewModel.doOnCreateAccount()

            assertThat(viewModel.uiState.value).isEqualTo(NoNetwork)
        }

        @Test
        internal fun `show loading spinner`() = runBlockingTest {
            val viewModel = viewModel(webService)
            viewModel.viewModelScope.cancel("Don't execute coroutine for test")

            viewModel.doOnCreateAccount()

            assertThat(viewModel.uiState.value).isEqualTo(CreatingAccount)
        }
    }

    @Nested
    @DisplayName("When login to Transferwise")
    inner class Login {
        @Test
        internal fun `navigate to login`() {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            viewModel(sharedViewModel = sharedViewModel).doOnLogin()

            assertThat(sharedViewModel.navigationAction.value).isInstanceOf(ToLogin::class.java)
        }

        @Test
        internal fun `navigate to login with url`() {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            viewModel(sharedViewModel = sharedViewModel).doOnLogin()

            assertThat((sharedViewModel.navigationAction.value as ToLogin).url).startsWith("https://sandbox.transferwise.tech/oauth/authorize/")
        }

        @Test
        internal fun `navigate to login with client id`() {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            viewModel(sharedViewModel = sharedViewModel).doOnLogin()

            assertThat((sharedViewModel.navigationAction.value as ToLogin).url).contains("?client_id=referenceTest")
        }

        @Test
        internal fun `navigate to login with redirect uri`() {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            viewModel(sharedViewModel = sharedViewModel).doOnLogin()

            assertThat((sharedViewModel.navigationAction.value as ToLogin).url).endsWith("&redirect_uri=banksdemo://login/")
        }

        @Test
        internal fun `navigate to login with quote`() {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val fakeQuote = QuoteDescription.FixedSource(100f, "GBP", "EUR")
            viewModel(sharedViewModel = sharedViewModel, quote = fakeQuote).doOnLogin()

            assertThat((sharedViewModel.navigationAction.value as ToLogin).quote).isEqualTo(fakeQuote)
        }
    }

    private fun viewModel(
        service: BanksWebService = mock(),
        sharedViewModel: SharedViewModel = mock(),
        customerId: Int = 0,
        quote: QuoteDescription = mock()
    ) = ConnectAccountViewModel(
        service,
        sharedViewModel,
        customerId,
        quote,
        "https://sandbox.transferwise.tech/oauth/authorize/",
        "referenceTest"
    )
}
