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
package com.transferwise.banks.util.fake

import com.transferwise.banks.util.TextProvider

internal class FakeTextProvider : TextProvider {

    override fun minimumLengthError(minimum: Int) = "Minimum length is $minimum"

    override fun maximumLengthError(maximum: Int) = "Maximum length is $maximum"

    override fun validationFailedError(fieldName: String) = "Please enter valid $fieldName"

    override fun requiredError() = "Required"

    override fun transferCosts(fee: Float, currency: String, rate: Float) =
        "$fee $currency TransferWise fee included with an exchange rate of $rate"

    override fun paymentSummary(amount: Float, currency: String, receiver: String) = "You sent $amount $currency to $receiver"

    override fun quoteFee(fee: Float, currency: String) = "$fee $currency fee"

    override fun quoteRate(rate: Float) = "$rate guaranteed rate"

    override fun quoteRateUnknown() = "unknown rate"

    override fun quoteFeeUnknown() = "unknown fee"

    override fun quoteArrivalTime(arrivalTime: String) = "Should arrive $arrivalTime"

    override fun quoteInvalidAmount() = "Invalid amount"

    override fun createTransferError() = "Failed to create transfer"

    override fun networkError() = "Not connected to network"
}
