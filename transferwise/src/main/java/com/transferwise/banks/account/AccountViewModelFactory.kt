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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.transferwise.banks.account.connect.ConnectAccountViewModel
import com.transferwise.banks.account.login.HandleLoginViewModel
import com.transferwise.banks.shared.QuoteDescription
import com.transferwise.banks.shared.SharedViewModel

internal class AccountViewModelFactory(
    private val sharedViewModel: SharedViewModel,
    private val customerId: Int,
    private val quote: QuoteDescription,
    private val code: String = ""
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectAccountViewModel::class.java)) {
            return ConnectAccountViewModel(webService, sharedViewModel, customerId, quote, loginUrl, loginClientId) as T
        } else if (modelClass.isAssignableFrom(HandleLoginViewModel::class.java)) {
            return HandleLoginViewModel(webService, sharedViewModel, customerId, quote, code) as T
        } else if (modelClass.isAssignableFrom(CheckAccountViewModel::class.java)) {
            return CheckAccountViewModel(webService, sharedViewModel, customerId, quote) as T
        }
        throw RuntimeException("Can't create ViewModel - Unsupported Viewmodel class")
    }

    private val webService = sharedViewModel.webService
    private val loginUrl = sharedViewModel.loginUrl
    private val loginClientId = sharedViewModel.loginClientId
}
