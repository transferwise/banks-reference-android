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

import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.request.CreateRecipientRequest
import com.transferwise.banks.recipients.create.CreateRecipientStaticFormGenerator.Companion.NAME_KEY
import com.transferwise.dynamicform.api.DynamicFormsWebService
import com.transferwise.dynamicform.generator.UserInput

internal class CreateRecipientsWebService(
    private val webservice: BanksWebService,
    private val customerId: Int,
    private val quoteId: String,
    private val currency: String
) : DynamicFormsWebService {

    override suspend fun getForm() = webservice.getRecipientRequirements(customerId, quoteId)

    override suspend fun refreshForm(attributes: Map<String, String?>, details: Map<String, Any?>) =
        webservice.updateRecipientRequirements(customerId, quoteId, createRecipientRequest(attributes, details))

    fun createRecipientRequest(
        attributes: Map<String, String?>,
        details: Map<String, Any?>
    ) = CreateRecipientRequest(currency, attributes.accountHolderName, attributes.type, details, false)

    private val Map<String, String?>.accountHolderName get() = this[NAME_KEY.value] ?: ""
    private val Map<String, String?>.type get() = this[UserInput.Tabs.TABS_KEY.value]!!
}
