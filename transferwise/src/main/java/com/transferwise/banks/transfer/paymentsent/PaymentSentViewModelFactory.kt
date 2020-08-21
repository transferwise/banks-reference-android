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
package com.transferwise.banks.transfer.paymentsent

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.util.LocalizedTextProvider

internal class PaymentSentViewModelFactory(
    private val sharedViewModel: SharedViewModel,
    private val resources: Resources,
    private val amount: Float,
    private val currency: String,
    private val recipient: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentSentViewModel::class.java)) {
            return PaymentSentViewModel(sharedViewModel, amount, currency, recipient, LocalizedTextProvider(resources)) as T
        }
        throw RuntimeException("Unsupported ViewModel")
    }
}
