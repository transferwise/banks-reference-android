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
package com.transferwise.banks.api

import com.transferwise.banks.api.data.Currency
import com.transferwise.banks.api.data.Customer
import com.transferwise.banks.api.data.Quote
import com.transferwise.banks.api.data.Recipient
import com.transferwise.banks.api.data.TransferSummary
import com.transferwise.banks.api.request.CreateRecipientRequest
import com.transferwise.banks.api.request.QuoteRequest
import com.transferwise.banks.api.request.TransferRequest
import com.transferwise.dynamicform.api.DynamicSection
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

internal interface BanksApi {

    @GET("customers")
    suspend fun getCustomer(@Query("id") customerId: Int): Customer

    @POST("user-credentials/sign-up")
    suspend fun signUp(@Query("customerId") customerId: Int): Response<ResponseBody>

    @POST("user-credentials/existing")
    suspend fun logIn(@Query("customerId") customerId: Int, @Query("code") code: String): Response<ResponseBody>

    @GET("currencies")
    suspend fun getCurrencies(): List<Currency>

    @POST("anonymous-quotes")
    suspend fun getAnonymousQuote(@Body quoteRequest: QuoteRequest): Response<Quote>

    @POST("quotes")
    suspend fun getQuote(@Query("customerId") customerId: Int, @Body quoteRequest: QuoteRequest): Response<Quote>

    @GET("recipient/requirements")
    suspend fun getRecipientRequirements(@Query("customerId") customerId: Int, @Query("quoteId") quoteId: String): List<DynamicSection>

    @POST("recipient/requirements")
    suspend fun updateRecipientRequirements(
        @Query("customerId") customerId: Int,
        @Query("quoteId") quoteId: String,
        @Body createRecipientRequest: CreateRecipientRequest
    ): List<DynamicSection>

    @POST("recipients")
    suspend fun createRecipient(@Query("customerId") customerId: Int, @Body createRecipientRequest: CreateRecipientRequest): Response<ResponseBody>

    @GET("recipients")
    suspend fun getRecipients(@Query("customerId") customerId: Int, @Query("currencyCode") targetCurrency: String): List<Recipient>

    @POST("transfers/summary")
    suspend fun getTransferSummary(@Query("customerId") customerId: Int, @Query("quoteId") quoteId: String, @Query("recipientId") recipientAccountId: Int): TransferSummary

    @POST("/transfers/requirements")
    suspend fun getTransferRequirements(@Query("customerId") customerId: Int, @Body transferRequest: TransferRequest): List<DynamicSection>

    @POST("transfers")
    suspend fun createTransfer(@Query("customerId") customerId: Int, @Body transferRequest: TransferRequest): Response<ResponseBody>
}
