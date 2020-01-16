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
package com.transferwise.banks.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.shared.NavigationAction.ToAnonymousQuote
import com.transferwise.banks.shared.NavigationAction.ToQuote
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import kotlinx.coroutines.launch

internal class CheckAccountViewModel(
    private val webService: BanksWebService,
    private val sharedViewModel: SharedViewModel,
    private val customerId: Int,
    private val quote: QuoteDescription
) : ViewModel() {

    val uiState = MutableLiveData<CheckAccountUiState>()

    init {
        checkAccount()
    }

    fun checkAccount() =
        viewModelScope.launch {
            uiState.value = CheckAccountUiState.Checking
            try {
                val customer = webService.getCustomer(customerId)
                if (customer.transferWiseAccountLinked) {
                    sharedViewModel.navigationAction.setValue(ToQuote(customerId, quote))
                } else {
                    sharedViewModel.navigationAction.setValue(ToAnonymousQuote(customerId, quote))
                }
            } catch (e: Exception) {
                uiState.value = CheckAccountUiState.Failed
            }
        }
}

sealed class CheckAccountUiState {
    object Checking : CheckAccountUiState()
    object Failed : CheckAccountUiState()
}
