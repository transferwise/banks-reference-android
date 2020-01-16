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
package com.transferwise.banks.recipients

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.data.Recipient
import com.transferwise.banks.recipients.GetRecipientsUiState.Failed
import com.transferwise.banks.recipients.GetRecipientsUiState.Loading
import com.transferwise.banks.recipients.GetRecipientsUiState.Select
import com.transferwise.banks.shared.NavigationAction.ToCreateRecipient
import com.transferwise.banks.shared.NavigationAction.ToExtraDetails
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.util.DefaultRecipientHelper
import com.transferwise.banks.util.RecipientHelper
import kotlinx.coroutines.launch

internal class GetRecipientsViewModel(
    private val webService: BanksWebService,
    private val sharedViewModel: SharedViewModel,
    private val customerId: Int,
    private val quoteId: String,
    private val targetCurrency: String,
    private val helper: RecipientHelper = DefaultRecipientHelper()
) : ViewModel() {

    val uiState = MutableLiveData<GetRecipientsUiState>().apply { value = Loading }

    fun doOnStart() = viewModelScope.launch {
        val recipients = webService.getRecipients(customerId, targetCurrency)
        uiState.value = Select(recipients.toItems().sortedBy { it.name })
    }

    fun doOnRecipientSelected(recipient: RecipientItem) {
        viewModelScope.launch {
            uiState.value = Loading
            try {
                val transferSummary = webService.getTransferSummary(customerId, quoteId, recipient.id)
                sharedViewModel.navigationAction.setValue(ToExtraDetails(customerId, transferSummary))
            } catch (e: Exception) {
                uiState.value = Failed
            }
        }
    }

    private fun List<Recipient>.toItems(): List<RecipientItem> = map {
        RecipientItem(
            it.id,
            it.name.fullName,
            helper.initials(it.name.fullName),
            it.accountSummary,
            helper.color(it.name.fullName)
        )
    }

    fun doOnAddRecipient() = sharedViewModel.navigationAction.setValue(ToCreateRecipient(customerId, quoteId, targetCurrency))
}
