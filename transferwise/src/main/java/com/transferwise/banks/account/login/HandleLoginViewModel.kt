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
package com.transferwise.banks.account.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transferwise.banks.account.login.HandleLoginUiState.Failed
import com.transferwise.banks.account.login.HandleLoginUiState.LoggingIn
import com.transferwise.banks.account.login.HandleLoginUiState.NoNetwork
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.shared.NavigationAction
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import kotlinx.coroutines.launch

internal class HandleLoginViewModel(
    webService: BanksWebService,
    private val sharedViewModel: SharedViewModel,
    private val customerId: Int,
    private val quote: QuoteDescription,
    code: String
) : ViewModel() {
    val uiState = MutableLiveData<HandleLoginUiState>()

    init {
        uiState.value = LoggingIn
        viewModelScope.launch {
            try {
                if (webService.logIn(customerId, code).isSuccessful) {
                    sharedViewModel.navigationAction.setValue(NavigationAction.ToQuote(customerId, quote))
                } else {
                    uiState.value = Failed
                }
            } catch (e: Throwable) {
                uiState.value = NoNetwork
            }
        }
    }

    fun doGoBack() = sharedViewModel.navigationAction.setValue(NavigationAction.ToConnectAccount(customerId, quote))
}
