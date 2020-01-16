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

import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.request.TransferRequest
import com.transferwise.dynamicform.api.DynamicFormsWebService

internal class ExtraDetailsWebService(
    private val webService: BanksWebService,
    private val customerId: Int,
    private val quoteId: String,
    private val recipientId: Int
) : DynamicFormsWebService {

    override suspend fun getForm() =
        webService.getTransferRequirements(customerId, TransferRequest(quoteId, recipientId, emptyMap()))

    override suspend fun refreshForm(attributes: Map<String, String?>, details: Map<String, Any?>) =
        webService.getTransferRequirements(customerId, TransferRequest(quoteId, recipientId, details))
}
