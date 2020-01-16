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

import android.util.Log
import com.squareup.moshi.Moshi
import com.transferwise.banks.api.request.CreateRecipientRequest
import com.transferwise.banks.api.request.QuoteRequest
import com.transferwise.banks.api.request.TransferRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class BanksWebService(baseUrl: String) {

    private val banksApi: BanksApi

    init {
        val retrofit = Retrofit.Builder()
            .client(createOkHttpClientWithLogging())
            .baseUrl(baseUrl)
            .addConverterFactory(createMoshiWithTypeAdapters())
            .build()
        banksApi = retrofit.create(BanksApi::class.java)
    }

    suspend fun getCustomer(customerId: Int) = banksApi.getCustomer(customerId)

    suspend fun signUp(customerId: Int) = banksApi.signUp(customerId)

    suspend fun logIn(customerId: Int, code: String) = banksApi.logIn(customerId, code)

    suspend fun getAnonymousQuote(quoteRequest: QuoteRequest) = banksApi.getAnonymousQuote(quoteRequest)

    suspend fun getQuote(customerId: Int, quoteRequest: QuoteRequest) = banksApi.getQuote(customerId, quoteRequest)

    suspend fun getCurrencies() = banksApi.getCurrencies()

    suspend fun getRecipientRequirements(customerId: Int, quoteId: String) =
        banksApi.getRecipientRequirements(customerId, quoteId)

    suspend fun updateRecipientRequirements(customerId: Int, quoteId: String, request: CreateRecipientRequest) =
        banksApi.updateRecipientRequirements(customerId, quoteId, request)

    suspend fun createRecipient(customerId: Int, request: CreateRecipientRequest) =
        banksApi.createRecipient(customerId, request)

    suspend fun getRecipients(customerId: Int, targetCurrency: String) = banksApi.getRecipients(customerId, targetCurrency)

    suspend fun getTransferSummary(customerId: Int, quoteId: String, recipientAccountId: Int) =
        banksApi.getTransferSummary(customerId, quoteId, recipientAccountId)

    suspend fun getTransferRequirements(customerId: Int, transferRequest: TransferRequest) =
        banksApi.getTransferRequirements(customerId, transferRequest)

    suspend fun createTransfer(customerId: Int, transferRequest: TransferRequest) =
        banksApi.createTransfer(customerId, transferRequest)
}

private fun createOkHttpClientWithLogging(): OkHttpClient {
    val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            Log.d("BanksWebService", message)
        }
    })
    interceptor.level = HttpLoggingInterceptor.Level.BODY
    return OkHttpClient.Builder().addInterceptor(interceptor).build()
}

private fun createMoshiWithTypeAdapters(): MoshiConverterFactory {
    val moshi = Moshi.Builder().add(TypeAdapter()).build()
    return MoshiConverterFactory.create(moshi)
}
