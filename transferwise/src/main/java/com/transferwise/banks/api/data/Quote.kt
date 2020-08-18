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
package com.transferwise.banks.api.data

internal data class Quote(
    val id: String?,
    val sourceCurrency: String,
    val targetCurrency: String,
    val sourceAmount: Float,
    val targetAmount: Float,
    val rate: Float,
    val fee: Float,
    val formattedEstimatedDelivery: String,
    val rateType: String,
    val notices: List<Notice>
)

internal data class Notice(
    val text: String,
    val link: String?,
    val type: String
)