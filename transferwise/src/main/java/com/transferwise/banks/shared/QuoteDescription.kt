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
package com.transferwise.banks.shared

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class QuoteDescription : Parcelable {

    abstract val amount: Float
    abstract val sourceCurrency: String
    abstract val targetCurrency: String
    abstract val id: String?

    @Parcelize
    data class FixedSource(
        override val amount: Float,
        override val sourceCurrency: String,
        override val targetCurrency: String,
        override val id: String? = null
    ) : QuoteDescription()

    @Parcelize
    data class FixedTarget(
        override val amount: Float,
        override val sourceCurrency: String,
        override val targetCurrency: String,
        override val id: String? = null
    ) : QuoteDescription()

    /*
     * Modifying the quote, always invalidates it's ID
     */
    fun toFixedSource(sourceAmount: Float) = FixedSource(sourceAmount, sourceCurrency, targetCurrency, null)

    fun toFixedTarget(targetAmount: Float) = FixedTarget(targetAmount, sourceCurrency, targetCurrency, null)

    fun copyWithTargetCurrency(targetCurrency: String) = when (this) {
        is FixedSource -> this.copy(targetCurrency = targetCurrency, id = null)
        is FixedTarget -> this.copy(targetCurrency = targetCurrency, id = null)
    }

    fun copyWithId(id: String?) = when (this) {
        is FixedSource -> this.copy(id = id)
        is FixedTarget -> this.copy(id = id)
    }
}
