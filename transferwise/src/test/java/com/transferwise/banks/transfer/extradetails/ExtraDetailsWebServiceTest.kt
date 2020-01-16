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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.request.TransferRequest
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class ExtraDetailsWebServiceTest {

    @Mock
    lateinit var webService: BanksWebService

    @Nested
    @DisplayName("Get form gets transfer requirements")
    inner class GetForm {

        @Test
        internal fun `with customer id`() = runBlockingTest {
            ExtraDetailsWebService(webService, 5, "", 0).getForm()

            verify(webService).getTransferRequirements(eq(5), any())
        }

        @Test
        internal fun `with quote id`() = runBlockingTest {
            ExtraDetailsWebService(webService, 0, "quoteId", 0).getForm()

            argumentCaptor<TransferRequest> {
                verify(webService).getTransferRequirements(any(), capture())

                assertThat(firstValue.quoteId).isEqualTo("quoteId")
            }
        }

        @Test
        internal fun `with recipient id`() = runBlockingTest {
            ExtraDetailsWebService(webService, 0, "", 5).getForm()

            argumentCaptor<TransferRequest> {
                verify(webService).getTransferRequirements(any(), capture())

                assertThat(firstValue.recipientId).isEqualTo(5)
            }
        }

        @Test
        internal fun `with no details`() = runBlockingTest {
            ExtraDetailsWebService(webService, 0, "", 5).getForm()

            argumentCaptor<TransferRequest> {
                verify(webService).getTransferRequirements(any(), capture())

                assertThat(firstValue.details).isEmpty()
            }
        }
    }

    @Nested
    @DisplayName("Refresh form gets transfer requirements")
    inner class RefreshForm {

        @Test
        internal fun `with customer id`() = runBlockingTest {
            ExtraDetailsWebService(webService, 5, "", 0).refreshForm(emptyMap(), emptyMap())

            verify(webService).getTransferRequirements(eq(5), any())
        }

        @Test
        internal fun `with quote id`() = runBlockingTest {
            ExtraDetailsWebService(webService, 0, "quoteId", 0).refreshForm(emptyMap(), emptyMap())

            argumentCaptor<TransferRequest> {
                verify(webService).getTransferRequirements(any(), capture())

                assertThat(firstValue.quoteId).isEqualTo("quoteId")
            }
        }

        @Test
        internal fun `with recipient id`() = runBlockingTest {
            ExtraDetailsWebService(webService, 0, "", 5).refreshForm(emptyMap(), emptyMap())

            argumentCaptor<TransferRequest> {
                verify(webService).getTransferRequirements(any(), capture())

                assertThat(firstValue.recipientId).isEqualTo(5)
            }
        }

        @Test
        internal fun `with details`() = runBlockingTest {
            ExtraDetailsWebService(webService, 0, "", 5).refreshForm(emptyMap(), mapOf("key" to "value"))

            argumentCaptor<TransferRequest> {
                verify(webService).getTransferRequirements(any(), capture())

                assertThat(firstValue.details).isEqualTo(mapOf("key" to "value"))
            }
        }
    }
}
