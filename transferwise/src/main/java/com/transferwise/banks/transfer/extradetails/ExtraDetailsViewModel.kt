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
package com.transferwise.banks.transfer.extradetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transferwise.banks.api.data.TransferSummary
import com.transferwise.banks.shared.NavigationAction.ToConfirmation
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.shared.TransferDetails
import com.transferwise.dynamicform.DynamicFormController
import com.transferwise.dynamicform.DynamicFormState
import com.transferwise.dynamicform.DynamicFormState.Complete
import com.transferwise.dynamicform.DynamicFormState.Loading
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

internal class ExtraDetailsViewModel(
    private val webService: ExtraDetailsWebService,
    private val sharedViewModel: SharedViewModel,
    private val formController: DynamicFormController,
    private val customerId: Int,
    private val transferSummary: TransferSummary
) : ViewModel() {

    val uiState = MutableLiveData<ExtraDetailsUiState>()

    private var currentInput: DynamicFormState = Loading

    fun doOnStart() {
        formController.showForm(viewModelScope, webService)
        viewModelScope.launch { formController.uiState.consumeEach { doOnFormUpdate(it) } }
    }

    fun doOnSave() {
        if (currentInput is Complete) {
            navigateToConfirmation()
        } else {
            formController.showLocalErrors()
        }
    }

    private fun navigateToConfirmation() =
        sharedViewModel.navigationAction.setValue(ToConfirmation(customerId, transferSummary, getDetails()))

    private fun getDetails() = TransferDetails(formController.currentDetails())

    private fun doOnFormUpdate(formState: DynamicFormState) {
        if (noExtraDetailsRequired(formState)) {
            navigateToConfirmation()
        } else {
            currentInput = formState
            uiState.value = if (formState == Loading) ExtraDetailsUiState.Loading else ExtraDetailsUiState.Idle
        }
    }

    private fun noExtraDetailsRequired(formState: DynamicFormState) = currentInput == Loading && formState is Complete
}
