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
package com.transferwise.banks.quote.api

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.data.Quote
import com.transferwise.banks.api.request.QuoteRequest
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import retrofit2.Response

internal class DefaultQuoteWebServiceTest {

    @Test
    internal fun `get quote from quote endpoint`() = runBlockingTest {
        val webService = mock<BanksWebService>()
        val fakeRequest = QuoteRequest(100f, null, "GBP", "EUR")
        val mockQuoteResponse = mock<Response<Quote>>()
        whenever(webService.getQuote(5, fakeRequest)).thenReturn(mockQuoteResponse)

        val quote = DefaultQuoteWebService(webService).getQuote(5, fakeRequest)

        Assertions.assertThat(quote).isEqualTo(mockQuoteResponse)
    }
}
