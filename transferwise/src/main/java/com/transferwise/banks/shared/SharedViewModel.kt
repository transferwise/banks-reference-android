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

import androidx.lifecycle.ViewModel
import com.transferwise.banks.BackendConfiguration
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.util.SingleLiveEvent

/**
 * The [SharedViewModel] ensures that all fragments are decoupled from the [InternationalTransferActivity]
 * and also from each other. It provides access to the static configuration of the transferwise module and
 * allows different screens to navigate to a destination.
 *
 * Note that is doesn't hold any state! All state is stored in arguments of the [Fragments] or [Activity]
 */
internal class SharedViewModel(
    backendConfiguration: BackendConfiguration
) : ViewModel() {

    val webService = BanksWebService(backendConfiguration.url)
    val navigationAction = SingleLiveEvent<NavigationAction>()
    val loginUrl = backendConfiguration.loginUrl
    val loginClientId = backendConfiguration.loginClientId
}
