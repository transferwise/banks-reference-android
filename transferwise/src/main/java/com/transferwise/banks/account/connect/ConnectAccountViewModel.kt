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
package com.transferwise.banks.account.connect

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transferwise.banks.account.connect.ConnectAccountUiState.AccountExists
import com.transferwise.banks.account.connect.ConnectAccountUiState.Failed
import com.transferwise.banks.account.connect.ConnectAccountUiState.Idle
import com.transferwise.banks.account.login.LoginConstants.REDIRECT_URI
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.shared.NavigationAction.ToLogin
import com.transferwise.banks.shared.NavigationAction.ToQuote
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel
import kotlinx.coroutines.launch

internal class ConnectAccountViewModel(
    private val webService: BanksWebService,
    private val sharedViewModel: SharedViewModel,
    private val customerId: Int,
    private val quote: QuoteDescription,
    private val loginUrl: String,
    private val loginClientId: String
) : ViewModel() {

    val uiState = MutableLiveData<ConnectAccountUiState>()

    init {
        uiState.value = Idle
    }

    fun doOnCreateAccount() {
        uiState.value = ConnectAccountUiState.CreatingAccount
        viewModelScope.launch {
            try {
                val response = webService.signUp(customerId)
                when {
                    response.isSuccessful -> sharedViewModel.navigationAction.setValue(ToQuote(customerId, quote))
                    response.code() == ACCOUNT_EXISTS -> uiState.value = AccountExists
                    else -> uiState.value = Failed
                }
            } catch (t: Throwable) {
                uiState.value = ConnectAccountUiState.NoNetwork
            }
        }
    }

    fun doOnLogin() {
        sharedViewModel.navigationAction.setValue(ToLogin("$loginUrl?client_id=$loginClientId&redirect_uri=$REDIRECT_URI", quote))
    }

    companion object {
        private const val ACCOUNT_EXISTS = 409
    }
}
