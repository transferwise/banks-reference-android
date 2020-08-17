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
package com.transferwise.banks.util

internal interface TextProvider {
    fun minimumLengthError(minimum: Int): String
    fun maximumLengthError(maximum: Int): String
    fun requiredError(): String
    fun validationFailedError(fieldName: String): String
    fun transferCosts(fee: Float, currency: String, rate: Float): String
    fun paymentSummary(amount: Float, currency: String, receiver: String): String
    fun quoteFee(fee: Float, currency: String): String
    fun quoteRate(rate: Float, rateType: String): String
    fun quoteRateUnknown(): String
    fun quoteFeeUnknown(): String
    fun quoteArrivalTime(arrivalTime: String): String
    fun quoteInvalidAmount(): String
    fun createTransferError(): String
    fun networkError(): String
}
