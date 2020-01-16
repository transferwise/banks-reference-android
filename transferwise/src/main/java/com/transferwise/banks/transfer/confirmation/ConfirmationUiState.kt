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
package com.transferwise.banks.transfer.confirmation

import androidx.annotation.ColorInt

internal sealed class ConfirmationUiState {

    abstract val summary: FormattedSummary
    abstract val hasReference: Boolean

    data class Summary(
        override val summary: FormattedSummary,
        override val hasReference: Boolean
    ) : ConfirmationUiState()

    data class SummaryWithReference(
        override val summary: FormattedSummary,
        val reference: String,
        val referenceError: String,
        override val hasReference: Boolean = true
    ) : ConfirmationUiState()

    data class SaveFailed(
        override val summary: FormattedSummary,
        override val hasReference: Boolean,
        val error: String
    ) : ConfirmationUiState()

    data class Saving(override val summary: FormattedSummary, override val hasReference: Boolean) : ConfirmationUiState()
}

data class FormattedSummary(
    val sourceAmount: String,
    val sourceCurrency: String,
    val targetAmount: String,
    val targetCurrency: String,
    val recipientInitials: String,
    val recipientName: String,
    val recipientAccount: String,
    val arrivalTime: String,
    val costs: String,
    @ColorInt val recipientColor: Int
)
