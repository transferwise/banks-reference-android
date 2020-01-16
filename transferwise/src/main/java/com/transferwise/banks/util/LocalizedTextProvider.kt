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

import android.content.res.Resources
import com.transferwise.banks.R

internal open class LocalizedTextProvider(private val resources: Resources) : TextProvider {

    override fun minimumLengthError(minimum: Int) = resources.getString(R.string.error_input_tooshort, minimum)

    override fun maximumLengthError(maximum: Int) = resources.getString(R.string.error_input_toolong, maximum)

    override fun requiredError() = resources.getString(R.string.error_input_required)

    override fun validationFailedError(fieldName: String) =
        resources.getString(R.string.error_input_validationfailed, fieldName)

    override fun transferCosts(fee: Float, currency: String, rate: Float) =
        resources.getString(R.string.confirmation_fee_rate, fee, currency, rate)

    override fun paymentSummary(amount: Float, currency: String, receiver: String) =
        resources.getString(R.string.payment_sent_summary, amount, currency, receiver)

    override fun quoteFee(fee: Float, currency: String) = resources.getString(R.string.quote_fee, fee, currency)

    override fun quoteRate(rate: Float) = resources.getString(R.string.quote_guaranteed_rate, rate)

    override fun quoteRateUnknown() = resources.getString(R.string.quote_unknown_rate)

    override fun quoteFeeUnknown() = resources.getString(R.string.quote_unknown_fee)

    override fun quoteArrivalTime(arrivalTime: String) = resources.getString(R.string.quote_arrival_time, arrivalTime)

    override fun quoteInvalidAmount() = resources.getString(R.string.quote_invalid_amount)

    override fun createTransferError() = resources.getString(R.string.confirmation_failed)

    override fun networkError() = resources.getString(R.string.error_no_network)
}
