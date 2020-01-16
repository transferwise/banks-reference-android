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
package com.transferwise.banks.recipients.create

import androidx.lifecycle.viewModelScope
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.request.CreateRecipientRequest
import com.transferwise.banks.shared.NavigationAction
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.testutils.TestCoroutineExtension
import com.transferwise.banks.testutils.TestLiveDataExtension
import com.transferwise.banks.util.fake.FakeConfiguration
import com.transferwise.dynamicform.DynamicFormController
import com.transferwise.dynamicform.DynamicFormState
import com.transferwise.dynamicform.DynamicFormState.Complete
import com.transferwise.dynamicform.DynamicFormState.Incomplete
import com.transferwise.dynamicform.DynamicFormState.Loading
import com.transferwise.dynamicform.DynamicFormState.ValidationError
import com.transferwise.dynamicform.generator.StaticFormGenerator
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import retrofit2.Response

@ExtendWith(MockitoExtension::class, TestCoroutineExtension::class, TestLiveDataExtension::class)
internal class CreateRecipientViewModelTest {

    @Mock
    lateinit var webService: BanksWebService

    @Mock
    lateinit var formController: DynamicFormController

    @Test
    internal fun `initial state is idle`() {
        val viewModel = CreateRecipientViewModel(mock(), mock(), mock(), 0, "", "", mock())

        assertThat(viewModel.uiState.value).isEqualTo(CreateRecipientUiState.Idle)
    }

    @Nested
    @DisplayName("When started")
    inner class OnStart {

        @Nested
        @DisplayName("show dynamic form")
        inner class ShowForm {

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
            internal fun `with customer id`() = runBlockingTest {
                val viewModel = viewModel(webService, formController, customerId = 5)
                viewModel.doOnStart()

                argumentCaptor<CreateRecipientsWebService> {
                    verify(formController).showForm(eq(viewModel.viewModelScope), capture(), any())

                    firstValue.getForm()

                    verify(webService).getRecipientRequirements(eq(5), any())
                }
            }

            @Test
            internal fun `with quote id`() = runBlockingTest {
                val viewModel = viewModel(webService, formController, quoteId = "quoteId")
                viewModel.doOnStart()

                argumentCaptor<CreateRecipientsWebService> {
                    verify(formController).showForm(eq(viewModel.viewModelScope), capture(), any())

                    firstValue.getForm()

                    verify(webService).getRecipientRequirements(any(), eq("quoteId"))
                }
            }

            @Test
            internal fun `with target currency`() = runBlockingTest {
                val viewModel = viewModel(webService, formController, targetCurrency = "GBP")
                viewModel.doOnStart()

                argumentCaptor<CreateRecipientsWebService> {
                    verify(formController).showForm(eq(viewModel.viewModelScope), capture(), any())

                    firstValue.refreshForm(mapOf("type" to "iban"), emptyMap())
                }

                argumentCaptor<CreateRecipientRequest> {
                    verify(webService).updateRecipientRequirements(any(), any(), capture())

                    assertThat(firstValue.currency).isEqualTo("GBP")
                }
            }

            @Test
            internal fun `with static form`() = runBlockingTest {
                val viewModel = viewModel(webService, formController)
                viewModel.doOnStart()

                argumentCaptor<StaticFormGenerator> {
                    verify(formController).showForm(any(), any(), capture())

                    assertThat(firstValue).isInstanceOf(CreateRecipientStaticFormGenerator::class.java)
                }
            }
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
            internal fun `when incomplete`() {
                val viewModel = viewModelWithFormState(Incomplete(emptyList()))

                viewModel.saveRecipient()

                verify(formController).showLocalErrors()
            }

            @Test
            internal fun `when validation error`() {
                val viewModel = viewModelWithFormState(ValidationError(emptyList()))

                viewModel.saveRecipient()

                verify(formController).showLocalErrors()
            }

            @Test
            internal fun `when loading`() {
                val viewModel = viewModelWithFormState(Loading)

                viewModel.saveRecipient()

                verify(formController).showLocalErrors()
            }

            @Test
            internal fun `don't show local errors when complete`() {
                val viewModel = viewModelWithFormState(Complete(emptyList()))

                viewModel.saveRecipient()

                verify(formController, never()).showLocalErrors()
            }
        }

        @Nested
        @DisplayName("create recipient")
        inner class CreateRecipient {

            @BeforeEach
            fun setUp() {
                whenever(formController.currentAttributes()).thenReturn(mapOf("type" to "iban"))
            }

            @Test
            internal fun `and navigate to recipients when success`() = runBlockingTest {
                whenever(webService.createRecipient(eq(5), any())).thenReturn(Response.success(null))
                val viewModel = viewModelWithFormState(Complete(emptyList()))

                viewModel.saveRecipient()

                assertThat(sharedModel.navigationAction.value).isEqualTo(NavigationAction.ToRecipients(5, "quoteId", "GBP"))
            }

            @Test
            internal fun `don't navigate to recipients when recipient creation failed`() = runBlockingTest {
                whenever(webService.createRecipient(eq(5), any())).thenReturn(Response.error(404, "".toResponseBody()))
                val viewModel = viewModelWithFormState(Complete(emptyList()))

                viewModel.saveRecipient()

                assertThat(sharedModel.navigationAction.value).isNull()
            }

            @Test
            internal fun `show server errors when recipient creation failed`() = runBlockingTest {
                whenever(webService.createRecipient(eq(5), any())).thenReturn(Response.error(404, "errors".toResponseBody()))
                val viewModel = viewModelWithFormState(Complete(emptyList()))

                viewModel.saveRecipient()

                verify(formController).showServerErrors("errors")
            }

            @Test
            internal fun `show errors when recipient creation failed`() = runBlockingTest {
                whenever(webService.createRecipient(eq(5), any())).thenReturn(Response.error(404, "errors".toResponseBody()))
                val viewModel = viewModelWithFormState(Complete(emptyList()))

                viewModel.saveRecipient()

                assertThat(viewModel.uiState.value).isEqualTo(CreateRecipientUiState.Error)
            }
        }

        private fun viewModelWithFormState(state: DynamicFormState): CreateRecipientViewModel {
            whenever(formController.uiState).thenReturn(ConflatedBroadcastChannel(state))
            val viewModel = viewModel(webService, formController, sharedModel, 5, "quoteId", "GBP")
            viewModel.doOnStart()
            return viewModel
        }
    }

    private fun viewModel(
        webService: BanksWebService = mock(),
        controller: DynamicFormController = mock(),
        sharedModel: SharedViewModel = mock(),
        customerId: Int = 0,
        quoteId: String = "",
        targetCurrency: String = ""
    ) =
        CreateRecipientViewModel(webService, sharedModel, controller, customerId, quoteId, targetCurrency, mock())
}
