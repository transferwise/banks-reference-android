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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.request.CreateRecipientRequest
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class CreateRecipientsWebServiceTest {

    @Mock
    lateinit var webService: BanksWebService

    @Nested
    inner class GetForm {

        @Test
        internal fun `should get recipient requirements with customer id`() = runBlockingTest {
            CreateRecipientsWebService(webService, 5, "", "").getForm()

            verify(webService).getRecipientRequirements(eq(5), any())
        }

        @Test
        internal fun `should get recipient requirements with quote id`() = runBlockingTest {
            CreateRecipientsWebService(webService, 0, "quoteId", "").getForm()

            verify(webService).getRecipientRequirements(any(), eq("quoteId"))
        }
    }

    @Nested
    @DisplayName("Refresh form update recipient requirements")
    inner class RefreshForm {

        private val fakeAttrsWithType = mapOf("type" to "tab")

        @Test
        internal fun `with customer id`() = runBlockingTest {
            CreateRecipientsWebService(webService, 5, "", "").refreshForm(fakeAttrsWithType, emptyMap())

            verify(webService).updateRecipientRequirements(eq(5), any(), any())
        }

        @Test
        internal fun `with quote id`() = runBlockingTest {
            CreateRecipientsWebService(webService, 0, "quoteId", "").refreshForm(fakeAttrsWithType, emptyMap())

            verify(webService).updateRecipientRequirements(any(), eq("quoteId"), any())
        }

        @Test
        internal fun `with currency`() = runBlockingTest {
            CreateRecipientsWebService(webService, 0, "", "GBP").refreshForm(fakeAttrsWithType, emptyMap())

            argumentCaptor<CreateRecipientRequest> {
                verify(webService).updateRecipientRequirements(any(), any(), capture())

                assertThat(firstValue.currency).isEqualTo("GBP")
            }
        }

        @Test
        internal fun `with recipient not owned by customer`() = runBlockingTest {
            CreateRecipientsWebService(webService, 0, "", "GBP").refreshForm(fakeAttrsWithType, emptyMap())

            argumentCaptor<CreateRecipientRequest> {
                verify(webService).updateRecipientRequirements(any(), any(), capture())

                assertThat(firstValue.ownedByCustomer).isFalse()
            }
        }

        @Test
        internal fun `with recipient name`() = runBlockingTest {
            val attrs = mapOf("type" to "tab", "accountHolderName" to "name")
            CreateRecipientsWebService(webService, 0, "", "GBP").refreshForm(attrs, emptyMap())

            argumentCaptor<CreateRecipientRequest> {
                verify(webService).updateRecipientRequirements(any(), any(), capture())

                assertThat(firstValue.accountHolderName).isEqualTo("name")
            }
        }

        @Test
        internal fun `with empty recipient name when no provided`() = runBlockingTest {
            val attrs = mapOf("type" to "tab")
            CreateRecipientsWebService(webService, 0, "", "GBP").refreshForm(attrs, emptyMap())

            argumentCaptor<CreateRecipientRequest> {
                verify(webService).updateRecipientRequirements(any(), any(), capture())

                assertThat(firstValue.accountHolderName).isEmpty()
            }
        }

        @Test
        internal fun `with recipient type`() = runBlockingTest {
            val attrs = mapOf("type" to "iban")
            CreateRecipientsWebService(webService, 0, "", "GBP").refreshForm(attrs, emptyMap())

            argumentCaptor<CreateRecipientRequest> {
                verify(webService).updateRecipientRequirements(any(), any(), capture())

                assertThat(firstValue.type).isEqualTo("iban")
            }
        }

        @Test
        internal fun `with recipient details`() = runBlockingTest {
            CreateRecipientsWebService(webService, 0, "", "GBP").refreshForm(fakeAttrsWithType, mapOf("address" to "value"))

            argumentCaptor<CreateRecipientRequest> {
                verify(webService).updateRecipientRequirements(any(), any(), capture())

                assertThat(firstValue.details).isEqualTo(mapOf("address" to "value"))
            }
        }
    }
}
