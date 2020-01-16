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
package com.transferwise.banks.recipients

import android.graphics.Color
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.data.Name
import com.transferwise.banks.api.data.Recipient
import com.transferwise.banks.api.data.TransferSummary
import com.transferwise.banks.recipients.GetRecipientsUiState.Failed
import com.transferwise.banks.recipients.GetRecipientsUiState.Loading
import com.transferwise.banks.recipients.GetRecipientsUiState.Select
import com.transferwise.banks.shared.NavigationAction.ToCreateRecipient
import com.transferwise.banks.shared.NavigationAction.ToExtraDetails
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.testutils.TestCoroutineExtension
import com.transferwise.banks.testutils.TestLiveDataExtension
import com.transferwise.banks.util.RecipientHelper
import com.transferwise.banks.util.fake.FakeConfiguration
import com.transferwise.banks.util.fake.FakeRecipientsHelper
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class, TestCoroutineExtension::class, TestLiveDataExtension::class)
internal class GetRecipientsViewModelTest {

    @Mock
    lateinit var webService: BanksWebService

    @Test
    internal fun `show loading when opening screen`() {
        val viewModel = GetRecipientsViewModel(webService, mock(), 0, "", "")

        assertThat(viewModel.uiState.value).isEqualTo(Loading)
    }

    @Nested
    @DisplayName("When onStart")
    inner class OnStart {

        @Test
        internal fun `get recipients`() = runBlockingTest {
            GetRecipientsViewModel(webService, mock(), 0, "", "").doOnStart()

            verify(webService).getRecipients(any(), any())
        }

        @Test
        internal fun `get recipients for customer id`() = runBlockingTest {
            GetRecipientsViewModel(webService, mock(), 1, "", "").doOnStart()

            verify(webService).getRecipients(eq(1), any())
        }

        @Test
        internal fun `get recipients for target currency`() = runBlockingTest {
            GetRecipientsViewModel(webService, mock(), 0, "", "EUR").doOnStart()

            verify(webService).getRecipients(any(), eq("EUR"))
        }

        @Nested
        inner class UpdateUi {

            @Test
            internal fun `with recipient id`() = runBlockingTest {
                val recipient = Recipient(7, Name(""), "")
                whenever(webService.getRecipients(any(), any())).thenReturn(listOf(recipient))
                val viewModel = GetRecipientsViewModel(webService, mock(), 0, "", "", FakeRecipientsHelper())

                viewModel.doOnStart()

                assertThat((viewModel.uiState.value as Select).recipients[0].id).isEqualTo(7)
            }

            @Test
            internal fun `with recipient name`() = runBlockingTest {
                val recipient = Recipient(0, Name("name"), "")
                whenever(webService.getRecipients(any(), any())).thenReturn(listOf(recipient))
                val viewModel = GetRecipientsViewModel(webService, mock(), 0, "", "", FakeRecipientsHelper())

                viewModel.doOnStart()

                assertThat((viewModel.uiState.value as Select).recipients[0].name).isEqualTo("name")
            }

            @Test
            internal fun `with recipient initials`() = runBlockingTest {
                val helper = mock<RecipientHelper>()
                whenever(helper.initials("Amy Blake")).thenReturn("AB")
                whenever(helper.color(any())).thenReturn(0)
                val recipient = Recipient(0, Name("Amy Blake"), "")
                whenever(webService.getRecipients(any(), any())).thenReturn(listOf(recipient))
                val viewModel = GetRecipientsViewModel(webService, mock(), 0, "", "", helper)

                viewModel.doOnStart()

                assertThat((viewModel.uiState.value as Select).recipients[0].initial).isEqualTo("AB")
            }

            @Test
            internal fun `with recipient color`() = runBlockingTest {
                val helper = mock<RecipientHelper>()
                whenever(helper.initials(any())).thenReturn("")
                whenever(helper.color("Amy")).thenReturn(Color.GREEN)
                val recipient = Recipient(0, Name("Amy"), "")
                whenever(webService.getRecipients(any(), any())).thenReturn(listOf(recipient))
                val viewModel = GetRecipientsViewModel(webService, mock(), 0, "", "", helper)

                viewModel.doOnStart()

                assertThat((viewModel.uiState.value as Select).recipients[0].color).isEqualTo(Color.GREEN)
            }

            @Test
            internal fun `with recipient account`() = runBlockingTest {
                val recipient = Recipient(0, Name(""), "BE00 0000 0000")
                whenever(webService.getRecipients(any(), any())).thenReturn(listOf(recipient))
                val viewModel = GetRecipientsViewModel(webService, mock(), 0, "", "", FakeRecipientsHelper())

                viewModel.doOnStart()

                assertThat((viewModel.uiState.value as Select).recipients[0].account).isEqualTo("BE00 0000 0000")
            }

            @Test
            internal fun `with sorted recipients`() = runBlockingTest {
                val recipient1 = Recipient(0, Name("Will Davies"), "")
                val recipient2 = Recipient(0, Name("Amy Blaine"), "")
                whenever(webService.getRecipients(any(), any())).thenReturn(listOf(recipient1, recipient2))
                val viewModel = GetRecipientsViewModel(webService, mock(), 0, "", "", FakeRecipientsHelper())

                viewModel.doOnStart()

                assertThat((viewModel.uiState.value as Select).recipients[0].name).isEqualTo("Amy Blaine")
            }
        }
    }

    @Nested
    @DisplayName("When recipient selected")
    inner class RecipientSelected {

        @Test
        internal fun `create transfer summary for customer`() = runBlockingTest {
            GetRecipientsViewModel(webService, SharedViewModel(FakeConfiguration), 5, "", "").doOnRecipientSelected(mock())

            verify(webService).getTransferSummary(eq(5), any(), any())
        }

        @Test
        internal fun `create transfer summary with quote`() = runBlockingTest {
            GetRecipientsViewModel(webService, SharedViewModel(FakeConfiguration), 0, "quoteId", "").doOnRecipientSelected(mock())

            verify(webService).getTransferSummary(any(), eq("quoteId"), any())
        }

        @Test
        internal fun `create transfer summary with account`() = runBlockingTest {
            GetRecipientsViewModel(webService, SharedViewModel(FakeConfiguration), 0, "quoteId", "").doOnRecipientSelected(
                recipient(20)
            )

            verify(webService).getTransferSummary(any(), any(), eq(20))
        }

        @Test
        internal fun `navigate to next screen when recipient selected`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            whenever(webService.getTransferSummary(any(), any(), any())).thenReturn(mock())

            GetRecipientsViewModel(webService, sharedViewModel, 0, "", "").doOnRecipientSelected(mock())

            assertThat(sharedViewModel.navigationAction.value).isInstanceOf(ToExtraDetails::class.java)
        }

        @Test
        internal fun `navigation action has customer id`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            whenever(webService.getTransferSummary(5, "quoteId", 20)).thenReturn(mock())

            GetRecipientsViewModel(webService, sharedViewModel, 5, "quoteId", "").doOnRecipientSelected(recipient(20))

            assertThat(sharedViewModel.extraDetailsAction.customerId).isEqualTo(5)
        }

        @Test
        internal fun `navigation action has transfer summary`() = runBlockingTest {
            val sharedViewModel = SharedViewModel(FakeConfiguration)
            val fakeSummary = TransferSummary("", 1, "USD", "EUR", 100f, 110f, 0.8f, 0.6f, "", "", "", mock())
            whenever(webService.getTransferSummary(5, "quoteId", 20)).thenReturn(fakeSummary)

            GetRecipientsViewModel(webService, sharedViewModel, 5, "quoteId", "").doOnRecipientSelected(recipient(20))

            assertThat(sharedViewModel.extraDetailsAction.transferSummary).isEqualTo(fakeSummary)
        }

        @Test
        internal fun `show progress indicator while creating transfer summary`() = runBlockingTest {
            whenever(webService.getTransferSummary(5, "quoteId", 20)).thenReturn(mock())
            val viewModel = GetRecipientsViewModel(webService, SharedViewModel(FakeConfiguration), 5, "quoteId", "")
            viewModel.uiState.value = mock()

            viewModel.doOnRecipientSelected(recipient(20))

            assertThat(viewModel.uiState.value).isEqualTo(Loading)
        }

        @Test
        internal fun `show error when creating transfer summary fails`() = runBlockingTest {
            whenever(webService.getTransferSummary(5, "quoteId", 20)).thenThrow(RuntimeException())
            val viewModel = GetRecipientsViewModel(webService, SharedViewModel(FakeConfiguration), 5, "quoteId", "")

            viewModel.doOnRecipientSelected(recipient(20))

            assertThat(viewModel.uiState.value).isEqualTo(Failed)
        }
    }

    @Nested
    @DisplayName("When add recipient")
    inner class AddRecipient {

        @Test
        internal fun `navigate to create recipient`() {
            val sharedViewModel = SharedViewModel(FakeConfiguration)

            GetRecipientsViewModel(mock(), sharedViewModel, 0, "", "").doOnAddRecipient()

            assertThat(sharedViewModel.navigationAction.value).isInstanceOf(ToCreateRecipient::class.java)
        }

        @Test
        internal fun `navigate to create recipient with customer id`() {
            val sharedViewModel = SharedViewModel(FakeConfiguration)

            GetRecipientsViewModel(mock(), sharedViewModel, 5, "", "").doOnAddRecipient()

            assertThat((sharedViewModel.navigationAction.value as ToCreateRecipient).customerId).isEqualTo(5)
        }

        @Test
        internal fun `navigate to create recipient with quote id`() {
            val sharedViewModel = SharedViewModel(FakeConfiguration)

            GetRecipientsViewModel(mock(), sharedViewModel, 0, "quote", "").doOnAddRecipient()

            assertThat((sharedViewModel.navigationAction.value as ToCreateRecipient).quoteId).isEqualTo("quote")
        }

        @Test
        internal fun `navigate to create recipient with target currency`() {
            val sharedViewModel = SharedViewModel(FakeConfiguration)

            GetRecipientsViewModel(mock(), sharedViewModel, 0, "", "GBP").doOnAddRecipient()

            assertThat((sharedViewModel.navigationAction.value as ToCreateRecipient).targetCurrency).isEqualTo("GBP")
        }
    }

    private fun recipient(id: Int) = RecipientItem(id, "", "", "", 0)
}

private val SharedViewModel.extraDetailsAction get() = navigationAction.value as ToExtraDetails
