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
package com.transferwise.banks.recipients.create

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.util.LocalizedTextProvider
import com.transferwise.dynamicform.DynamicFormController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class CreateRecipientViewModelFactory(
    private val sharedViewModel: SharedViewModel,
    private val dynamicFormController: DynamicFormController,
    private val resources: Resources,
    private val customerId: Int,
    private val quoteId: String,
    private val targetCurrency: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRecipientViewModel::class.java)) {
            return CreateRecipientViewModel(
                sharedViewModel.webService, sharedViewModel, dynamicFormController,
                customerId, quoteId, targetCurrency, LocalizedTextProvider(resources)
            ) as T
        }
        throw RuntimeException("Can't create ViewModel - Unsupported Viewmodel class")
    }
}
