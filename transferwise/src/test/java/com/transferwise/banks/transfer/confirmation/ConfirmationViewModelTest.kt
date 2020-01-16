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
package com.transferwise.banks.transfer.confirmation

import android.graphics.Color
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.data.TransferReferenceValidation
import com.transferwise.banks.api.data.TransferSummary
import com.transferwise.banks.api.request.TransferRequest
import com.transferwise.banks.shared.NavigationAction.ToPaymentSent
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.shared.TransferDetails
import com.transferwise.banks.testutils.TestCoroutineExtension
import com.transferwise.banks.testutils.TestLiveDataExtension
import com.transferwise.banks.transfer.confirmation.ConfirmationUiState.SaveFailed
import com.transferwise.banks.transfer.confirmation.ConfirmationUiState.Saving
import com.transferwise.banks.transfer.confirmation.ConfirmationUiState.Summary
import com.transferwise.banks.transfer.confirmation.ConfirmationUiState.SummaryWithReference
import com.transferwise.banks.util.RecipientHelper
import com.transferwise.banks.util.TextProvider
import com.transferwise.banks.util.fake.FakeConfiguration
import com.transferwise.banks.util.fake.FakeRecipientsHelper
import com.transferwise.banks.util.fake.FakeTextProvider
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(TestLiveDataExtension::class, MockitoExtension::class, TestCoroutineExtension::class)
internal class ConfirmationViewModelTest {

    @Nested
    @DisplayName("When screen opens")
    inner class ScreenOpen {

        @Test
        internal fun `show transfer summary`() {
            val viewModel = viewModel()

            assertThat(viewModel.uiState.value).isInstanceOf(Summary::class.java)
        }

        @Nested
        @DisplayName("show transfer summary")
        inner class ShowSummary {

            @Test
            internal fun `with integer source amount`() {
                val viewModel = viewModel(summary = summary(sourceAmount = 100f))

                assertThat(viewModel.summaryUiState.summary.sourceAmount).isEqualTo("100")
            }

            @Test
            internal fun `with two decimal source amount`() {
                val viewModel = viewModel(summary = summary(sourceAmount = 100.2f))

                assertThat(viewModel.summaryUiState.summary.sourceAmount).isEqualTo("100.20")
            }

            @Test
            internal fun `with source currency`() {
                val viewModel = viewModel(summary = summary(sourceCurrency = "USD"))

                assertThat(viewModel.summaryUiState.summary.sourceCurrency).isEqualTo("USD")
            }

            @Test
            internal fun `with integer target amount`() {
                val viewModel = viewModel(summary = summary(targetAmount = 110f))

                assertThat(viewModel.summaryUiState.summary.targetAmount).isEqualTo("110")
            }

            @Test
            internal fun `with two decimal target amount`() {
                val viewModel = viewModel(summary = summary(targetAmount = 110.2f))

                assertThat(viewModel.summaryUiState.summary.targetAmount).isEqualTo("110.20")
            }

            @Test
            internal fun `with target currency`() {
                val viewModel = viewModel(summary = summary(targetCurrency = "EUR"))

                assertThat(viewModel.summaryUiState.summary.targetCurrency).isEqualTo("EUR")
            }

            @Test
            internal fun `with recipient initials`() {
                val helper = mock<RecipientHelper>()
                whenever(helper.initials("Amy")).thenReturn("A")

                val viewModel = viewModel(summary = summary(recipientName = "Amy"), helper = helper)

                assertThat(viewModel.summaryUiState.summary.recipientInitials).isEqualTo("A")
            }

            @Test
            internal fun `with recipient name`() {
                val viewModel = viewModel(summary = summary(recipientName = "Amy Blaine"))

                assertThat(viewModel.summaryUiState.summary.recipientName).isEqualTo("Amy Blaine")
            }

            @Test
            internal fun `with recipient account`() {
                val viewModel = viewModel(summary = summary(accountSummary = "BE00 0000 0000 0000"))

                assertThat(viewModel.summaryUiState.summary.recipientAccount).isEqualTo("BE00 0000 0000 0000")
            }

            @Test
            internal fun `with recipient color`() {
                val helper = mock<RecipientHelper>()
                whenever(helper.initials("Amy")).thenReturn("")
                whenever(helper.color("Amy")).thenReturn(Color.GREEN)

                val viewModel = viewModel(summary = summary(recipientName = "Amy"), helper = helper)

                assertThat(viewModel.summaryUiState.summary.recipientColor).isEqualTo(Color.GREEN)
            }

            @Test
            internal fun `with arrival time`() {
                val viewModel = viewModel(summary = summary(formattedEstimatedDelivery = "in 5 hours"))

                assertThat(viewModel.summaryUiState.summary.arrivalTime).isEqualTo("in 5 hours")
            }

            @Test
            internal fun `with arrival fee and conversion rate`() {
                val viewModel = viewModel(summary = summary(sourceCurrency = "GBP", fee = 0.78f, rate = 1.2233f))

                assertThat(viewModel.summaryUiState.summary.costs).isEqualTo("0.78 GBP TransferWise fee included with an exchange rate of 1.2233")
            }

            @Test
            internal fun `has reference`() {
                val viewModel = viewModel(details = details("transfer reference"))

                assertThat(viewModel.summaryUiState.hasReference).isTrue()
            }

            @Test
            internal fun `has no reference`() {
                val viewModel = viewModel()

                assertThat(viewModel.summaryUiState.hasReference).isFalse()
            }

            private val ConfirmationViewModel.summaryUiState get() = lastState<Summary>()
        }
    }

    @Nested
    @DisplayName("When press save")
    inner class Save {

        @Mock
        lateinit var webService: BanksWebService

        @Nested
        @DisplayName("create transfer")
        inner class CreateTransfer {

            @Test
            internal fun `with customer id`() = runBlockingTest {
                viewModel(webService, customerId = 5, summary = summary()).doTransfer()

                verify(webService).createTransfer(eq(5), any())
            }

            @Test
            internal fun `with quote id`() = runBlockingTest {
                viewModel(webService, customerId = 0, summary = summary(quoteId = "quoteId")).doTransfer()

                argumentCaptor<TransferRequest>().apply {
                    verify(webService).createTransfer(any(), capture())

                    assertThat(firstValue.quoteId).isEqualTo("quoteId")
                }
            }

            @Test
            internal fun `with recipient id`() = runBlockingTest {
                viewModel(webService, customerId = 0, summary = summary(recipientId = 7)).doTransfer()

                argumentCaptor<TransferRequest>().apply {
                    verify(webService).createTransfer(any(), capture())

                    assertThat(firstValue.recipientId).isEqualTo(7)
                }
            }

            @Test
            internal fun `with details`() = runBlockingTest {
                val fakeDetails = mapOf("key" to "value")
                val fakeTransferDetails = mock<TransferDetails>()
                whenever(fakeTransferDetails.asMap()).thenReturn(fakeDetails)

                viewModel(
                    webService,
                    customerId = 0,
                    summary = summary(recipientId = 7),
                    details = fakeTransferDetails
                ).doTransfer()

                argumentCaptor<TransferRequest>().apply {
                    verify(webService).createTransfer(any(), capture())

                    assertThat(firstValue.details).isEqualTo(fakeDetails)
                }
            }
        }

        @Nested
        @DisplayName("Navigate to next screen")
        inner class Navigate {

            @Test
            internal fun `when success`() = runBlockingTest {
                val sharedViewModel = SharedViewModel(FakeConfiguration)

                viewModel(webService, sharedViewModel).doTransfer()

                assertThat(sharedViewModel.navigationAction.value).isInstanceOf(ToPaymentSent::class.java)
            }

            @Test
            internal fun `with source amount`() = runBlockingTest {
                val sharedViewModel = SharedViewModel(FakeConfiguration)

                viewModel(webService, sharedViewModel, summary = summary(sourceAmount = 50f)).doTransfer()

                assertThat((sharedViewModel.navigationAction.value as ToPaymentSent).amount).isEqualTo(50f)
            }

            @Test
            internal fun `with source currency`() = runBlockingTest {
                val sharedViewModel = SharedViewModel(FakeConfiguration)

                viewModel(webService, sharedViewModel, summary = summary(sourceCurrency = "EUR")).doTransfer()

                assertThat((sharedViewModel.navigationAction.value as ToPaymentSent).currency).isEqualTo("EUR")
            }

            @Test
            internal fun `with recipient`() = runBlockingTest {
                val sharedViewModel = SharedViewModel(FakeConfiguration)

                viewModel(webService, sharedViewModel, summary = summary(recipientName = "Amy Blaine")).doTransfer()

                assertThat((sharedViewModel.navigationAction.value as ToPaymentSent).recipient).isEqualTo("Amy Blaine")
            }
        }

        @Nested
        @DisplayName("show error")
        inner class ShowError {

            @Test
            internal fun `when failed`() = runBlockingTest {
                val viewModel = viewModel(webService)
                whenever(webService.createTransfer(any(), any())).thenThrow(RuntimeException())

                viewModel.doTransfer()

                assertThat(viewModel.uiState.value).isInstanceOf(SaveFailed::class.java)
            }

            @Test
            internal fun `with error message`() = runBlockingTest {
                val viewModel = viewModel(webService)
                whenever(webService.createTransfer(any(), any())).thenThrow(RuntimeException())

                viewModel.doTransfer()

                assertThat(viewModel.lastState<SaveFailed>().error).isEqualTo("Failed to create transfer")
            }

            @Test
            internal fun `with has reference`() = runBlockingTest {
                val viewModel = viewModel(webService, details = details("reference"))
                whenever(webService.createTransfer(any(), any())).thenThrow(RuntimeException())

                viewModel.doTransfer()

                assertThat(viewModel.lastState<SaveFailed>().hasReference).isTrue()
            }
        }

        @Nested
        @DisplayName("show saving")
        inner class ShowSaving {

            @Test
            internal fun `when transfer start`() = runBlockingTest {
                val viewModel = viewModel(webService, SharedViewModel(FakeConfiguration))

                viewModel.doTransfer()

                assertThat(viewModel.uiState.value).isInstanceOf(Saving::class.java)
            }

            @Test
            internal fun `with has reference`() = runBlockingTest {
                val viewModel = viewModel(webService, SharedViewModel(FakeConfiguration), details = details("reference"))

                viewModel.doTransfer()

                assertThat(viewModel.lastState<Saving>().hasReference).isTrue()
            }
        }
    }

    @Nested
    @DisplayName("Transfer reference")
    inner class Reference {

        @Nested
        @DisplayName("is shown")
        inner class Show {
            @Test
            internal fun `on shown`() {
                val viewModel = viewModel()

                viewModel.showReference()

                assertThat(viewModel.uiState.value).isInstanceOf(SummaryWithReference::class.java)
            }

            @Test
            internal fun `with reference`() {
                val viewModel = viewModel(details = details("my reference"))

                viewModel.showReference()

                assertThat(viewModel.lastState<SummaryWithReference>().reference).isEqualTo("my reference")
            }
        }

        @Test
        internal fun `is hidden`() {
            val viewModel = viewModel(details = details(""))
            viewModel.showReference()

            viewModel.hideReference()

            assertThat(viewModel.lastState<Summary>().hasReference).isFalse()
        }

        @Test
        internal fun `is hidden with reference`() {
            val viewModel = viewModel(details = details("my reference"))
            viewModel.showReference()

            viewModel.hideReference()

            assertThat(viewModel.lastState<Summary>().hasReference).isTrue()
        }

        @Nested
        @DisplayName("is updated")
        inner class Update {

            @Test
            internal fun `with new value`() {
                val viewModel = viewModel(details = details("my reference"))

                viewModel.changeReference("my reference 2")

                assertThat(viewModel.lastState<SummaryWithReference>().reference).isEqualTo("my reference 2")
            }

            @Test
            internal fun `with min length error`() {
                val viewModel = viewModel(summary = summary(minLength = 10), details = details("my reference"))

                viewModel.changeReference("short")

                assertThat(viewModel.lastState<SummaryWithReference>().referenceError).isEqualTo("Minimum length is 10")
            }

            @Test
            internal fun `with no min length error if no min length specified`() {
                val viewModel = viewModel(summary = summary(minLength = null), details = details("my reference"))

                viewModel.changeReference("short")

                assertThat(viewModel.lastState<SummaryWithReference>().referenceError).isEmpty()
            }

            @Test
            internal fun `with no min length error if longer than min length`() {
                val viewModel = viewModel(summary = summary(minLength = 5), details = details("my reference"))

                viewModel.changeReference("long enough")

                assertThat(viewModel.lastState<SummaryWithReference>().referenceError).isEmpty()
            }

            @Test
            internal fun `with max length error`() {
                val viewModel = viewModel(summary = summary(maxLength = 5), details = details("my reference"))

                viewModel.changeReference("way too long")

                assertThat(viewModel.lastState<SummaryWithReference>().referenceError).isEqualTo("Maximum length is 5")
            }

            @Test
            internal fun `with no max length error if no max length specified`() {
                val viewModel = viewModel(summary = summary(maxLength = null), details = details("my reference"))

                viewModel.changeReference("way too long and even longer than that")

                assertThat(viewModel.lastState<SummaryWithReference>().referenceError).isEmpty()
            }

            @Test
            internal fun `with no max length error if shorter than max length`() {
                val viewModel = viewModel(summary = summary(maxLength = 10), details = details("my reference"))

                viewModel.changeReference("short")

                assertThat(viewModel.lastState<SummaryWithReference>().referenceError).isEmpty()
            }

            @Test
            internal fun `with validation error`() {
                val viewModel = viewModel(summary = summary(validationRegex = "^[a-z]*\$"), details = details("my reference"))

                viewModel.changeReference("123456")

                assertThat(viewModel.lastState<SummaryWithReference>().referenceError).isEqualTo("Please enter valid transfer reference")
            }

            @Test
            internal fun `with no validation error if no regex specified`() {
                val viewModel = viewModel(summary = summary(validationRegex = null), details = details("my reference"))

                viewModel.changeReference("abcdefg 123456 ;/>,[][p]")

                assertThat(viewModel.lastState<SummaryWithReference>().referenceError).isEmpty()
            }

            @Test
            internal fun `with no validation error if valid reference`() {
                val viewModel = viewModel(summary = summary(validationRegex = "^[a-z]*\$"), details = details("my reference"))

                viewModel.changeReference("abcd")

                assertThat(viewModel.lastState<SummaryWithReference>().referenceError).isEmpty()
            }

            @Test
            internal fun `and stored`() = runBlockingTest {
                val webService = mock<BanksWebService>()
                val viewModel = viewModel(webService, details = details("my reference"))
                viewModel.changeReference("my reference 2")

                viewModel.doTransfer()

                argumentCaptor<TransferRequest>().apply {
                    verify(webService).createTransfer(any(), capture())

                    assertThat(firstValue.details.values.first()).isEqualTo("my reference 2")
                }
            }
        }
    }

    private fun viewModel(
        webService: BanksWebService = mock(),
        sharedModel: SharedViewModel = mock(),
        customerId: Int = 0,
        summary: TransferSummary = summary(),
        details: TransferDetails = TransferDetails(),
        textProvider: TextProvider = FakeTextProvider(),
        helper: RecipientHelper = FakeRecipientsHelper()
    ) = ConfirmationViewModel(webService, sharedModel, customerId, summary, details, textProvider, helper)

    private fun summary(
        quoteId: String = "",
        recipientId: Int = 0,
        sourceCurrency: String = "",
        targetCurrency: String = "",
        sourceAmount: Float = 0f,
        targetAmount: Float = 0f,
        rate: Float = 0f,
        fee: Float = 0f,
        recipientName: String = "Will",
        accountSummary: String = "",
        formattedEstimatedDelivery: String = "",
        minLength: Int? = null,
        maxLength: Int? = null,
        validationRegex: String? = null
    ) = TransferSummary(
        quoteId,
        recipientId,
        sourceCurrency,
        targetCurrency,
        sourceAmount,
        targetAmount,
        rate,
        fee,
        recipientName,
        accountSummary,
        formattedEstimatedDelivery,
        TransferReferenceValidation(minLength, maxLength, validationRegex)
    )

    private fun details(reference: String) = TransferDetails(mapOf("reference" to reference))

    private fun <T> ConfirmationViewModel.lastState() = this.uiState.value as T
}
