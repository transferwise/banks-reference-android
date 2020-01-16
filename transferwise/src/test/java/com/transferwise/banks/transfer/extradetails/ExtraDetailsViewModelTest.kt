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
package com.transferwise.banks.transfer.extradetails

import androidx.lifecycle.viewModelScope
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.banks.api.data.TransferReferenceValidation
import com.transferwise.banks.api.data.TransferSummary
import com.transferwise.banks.shared.NavigationAction.ToConfirmation
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.shared.TransferDetails
import com.transferwise.banks.testutils.TestCoroutineExtension
import com.transferwise.banks.testutils.TestLiveDataExtension
import com.transferwise.banks.util.fake.FakeConfiguration
import com.transferwise.dynamicform.DynamicFormController
import com.transferwise.dynamicform.DynamicFormState
import com.transferwise.dynamicform.DynamicFormState.Complete
import com.transferwise.dynamicform.DynamicFormState.Incomplete
import com.transferwise.dynamicform.DynamicFormState.Loading
import com.transferwise.dynamicform.DynamicFormState.ValidationError
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class, TestCoroutineExtension::class, TestLiveDataExtension::class)
internal class ExtraDetailsViewModelTest {

    @Mock
    lateinit var formController: DynamicFormController

    @Nested
    @DisplayName("When started show form")
    inner class OnStart {

        @BeforeEach
        fun setUp() {
            whenever(formController.uiState).thenReturn(ConflatedBroadcastChannel())
        }

        @Test
        internal fun `with viewmodel scope`() = runBlockingTest {
            val viewModel = viewModel(controller = formController)

            viewModel.doOnStart()

            verify(formController).showForm(eq(viewModel.viewModelScope), any(), any())
        }

        @Test
        internal fun `with web service`() = runBlockingTest {
            val webService = ExtraDetailsWebService(mock(), 0, "", 0)
            val viewModel = viewModel(webService, controller = formController)

            viewModel.doOnStart()

            verify(formController).showForm(any(), eq(webService), any())
        }
    }

    @Nested
    @DisplayName("When save")
    inner class OnSave {

        private val sharedModel = SharedViewModel(FakeConfiguration)

        @Nested
        @DisplayName("show local errors")
        inner class LocalErrors {

            @Test
            internal fun `when form has validation errors`() {
                val viewModel = viewModelWithFormState(ValidationError(emptyList()))

                viewModel.doOnSave()

                verify(formController).showLocalErrors()
            }

            @Test
            internal fun `when form incomplete`() {
                val viewModel = viewModelWithFormState(Incomplete(emptyList()))

                viewModel.doOnSave()

                verify(formController).showLocalErrors()
            }

            @Test
            internal fun `when form loading`() {
                val viewModel = viewModelWithFormState(Loading)

                viewModel.doOnSave()

                verify(formController).showLocalErrors()
            }
        }

        @Nested
        @DisplayName("navigate to confirmation")
        inner class Navigate {
            @Test
            internal fun `with customer id`() {
                viewModelWithFormState(Complete(emptyList())).doOnSave()

                assertThat(sharedModel.confirmationAction.customerId).isEqualTo(5)
            }

            @Test
            internal fun `with transfer summary`() {
                viewModelWithFormState(Complete(emptyList())).doOnSave()

                assertThat(sharedModel.confirmationAction.transferSummary).isEqualTo(summary())
            }

            @Test
            internal fun `with extra details`() {
                whenever(formController.currentDetails()).thenReturn(mapOf("key" to "value"))

                viewModelWithFormState(Complete(emptyList())).doOnSave()

                assertThat(sharedModel.confirmationAction.details).isEqualTo(TransferDetails(mapOf("key" to "value")))
            }
        }

        private val SharedViewModel.confirmationAction get() = navigationAction.value as ToConfirmation

        private fun viewModelWithFormState(state: DynamicFormState) = viewModelWithFormState(state, sharedModel)
    }

    @Nested
    @DisplayName("When form state")
    inner class OnFormState {

        private val sharedModel = SharedViewModel(FakeConfiguration)

        @Test
        internal fun `is loading then show loading`() {
            val viewModel = viewModelWithFormState(Loading)

            assertThat(viewModel.uiState.value).isEqualTo(ExtraDetailsUiState.Loading)
        }

        @Test
        internal fun `is not loading then show idle`() {
            val viewModel = viewModelWithFormState(Incomplete(emptyList()))

            assertThat(viewModel.uiState.value).isEqualTo(ExtraDetailsUiState.Idle)
        }

        @Test
        internal fun `is complete after loading, then navigate to confirmation`() {
            viewModelWithFormState(Loading)

            formController.uiState.offer(Complete(emptyList()))

            assertThat(sharedModel.navigationAction.value).isEqualTo(ToConfirmation(5, summary()))
        }

        @Test
        internal fun `is incomplete after loading, then stay on extra details`() {
            viewModelWithFormState(Loading)

            formController.uiState.offer(Incomplete(emptyList()))

            assertThat(sharedModel.navigationAction.value).isNull()
        }

        private fun viewModelWithFormState(state: DynamicFormState) = viewModelWithFormState(state, sharedModel)
    }

    private fun viewModelWithFormState(state: DynamicFormState, sharedModel: SharedViewModel): ExtraDetailsViewModel {
        whenever(formController.uiState).thenReturn(ConflatedBroadcastChannel(state))
        val viewModel = viewModel(mock(), sharedModel, formController, 5, summary())
        viewModel.doOnStart()
        return viewModel
    }

    fun viewModel(
        webService: ExtraDetailsWebService = mock(),
        sharedModel: SharedViewModel = mock(),
        controller: DynamicFormController = mock(),
        customerId: Int = 0,
        summar: TransferSummary = summary()
    ) = ExtraDetailsViewModel(webService, sharedModel, controller, customerId, summar)

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
        formattedEstimatedDelivery: String = ""
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
        TransferReferenceValidation(0, 20, "")
    )
}
