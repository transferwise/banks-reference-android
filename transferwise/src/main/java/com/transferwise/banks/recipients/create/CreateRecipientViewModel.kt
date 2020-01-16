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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.shared.NavigationAction
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.util.TextProvider
import com.transferwise.dynamicform.DynamicFormController
import com.transferwise.dynamicform.DynamicFormState
import com.transferwise.dynamicform.DynamicFormState.Complete
import com.transferwise.dynamicform.DynamicFormState.Loading
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
internal class CreateRecipientViewModel(
    private val webservice: BanksWebService,
    private val sharedViewModel: SharedViewModel,
    private val dynamicFormController: DynamicFormController,
    private val customerId: Int,
    private val quoteId: String,
    private val targetCurrency: String,
    textProvider: TextProvider
) : ViewModel() {

    val uiState = MutableLiveData<CreateRecipientUiState>()

    private var currentInput: DynamicFormState = Loading
    private val recipientsWebService = CreateRecipientsWebService(webservice, customerId, quoteId, targetCurrency)
    private val staticFormGenerator = CreateRecipientStaticFormGenerator(textProvider)

    init {
        uiState.value = CreateRecipientUiState.Idle
    }

    fun doOnStart() {
        dynamicFormController.showForm(viewModelScope, recipientsWebService, staticFormGenerator)
        viewModelScope.launch { dynamicFormController.uiState.consumeEach { doOnFormUpdate(it) } }
    }

    fun saveRecipient() = viewModelScope.launch {
        if (currentInput is Complete) {
            // error formstate cannot happen (save disabled)
            uiState.value = CreateRecipientUiState.Saving
            val createRecipient = webservice.createRecipient(customerId, createRecipientRequest())
            if (createRecipient.isSuccessful) {
                navigateToRecipients()
            } else {
                dynamicFormController.showServerErrors(createRecipient.errorBody()?.string())
                uiState.value = CreateRecipientUiState.Error
            }
        } else {
            dynamicFormController.showLocalErrors()
        }
    }

    private fun navigateToRecipients() =
        sharedViewModel.navigationAction.setValue(NavigationAction.ToRecipients(customerId, quoteId, targetCurrency))

    private fun createRecipientRequest() = recipientsWebService.createRecipientRequest(
        dynamicFormController.currentAttributes(),
        dynamicFormController.currentDetails()
    )

    private fun doOnFormUpdate(formState: DynamicFormState) {
        currentInput = formState
    }
}
