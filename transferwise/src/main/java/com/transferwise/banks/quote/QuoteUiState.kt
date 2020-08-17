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
package com.transferwise.banks.quote

internal sealed class QuoteUiState {
    abstract val sourceAmount: String
    abstract val sourceCurrency: String
    abstract val sourceFlagUri: String
    abstract val targetAmount: String
    abstract val targetCurrency: String
    abstract val targetFlagUri: String
    abstract val fee: String
    abstract val rate: String
    abstract val arrivalTime: String
    abstract val rateType: String
    abstract val notice: String

    data class Fetching(
        override val sourceAmount: String,
        override val sourceCurrency: String,
        override val sourceFlagUri: String,
        override val targetAmount: String,
        override val targetCurrency: String,
        override val targetFlagUri: String,
        override val fee: String,
        override val rate: String,
        override val arrivalTime: String,
        override val rateType: String,
        override val notice: String
    ) : QuoteUiState()

    data class Available(
        override val sourceAmount: String,
        override val sourceCurrency: String,
        override val sourceFlagUri: String,
        override val targetAmount: String,
        override val targetCurrency: String,
        override val targetFlagUri: String,
        override val fee: String,
        override val rate: String,
        override val arrivalTime: String,
        override val rateType: String,
        override val notice: String
    ) : QuoteUiState()

    data class Failed(
        override val sourceAmount: String,
        override val sourceCurrency: String,
        override val sourceFlagUri: String,
        override val targetAmount: String,
        override val targetCurrency: String,
        override val targetFlagUri: String,
        override val fee: String,
        override val rate: String,
        override val arrivalTime: String,
        override val rateType: String,
        override val notice: String,
        val errorMessage: String
    ) : QuoteUiState()
}
