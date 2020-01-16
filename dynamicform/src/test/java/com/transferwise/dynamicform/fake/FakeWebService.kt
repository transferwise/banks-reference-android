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
package com.transferwise.dynamicform.fake

import com.transferwise.dynamicform.api.AllowedValue
import com.transferwise.dynamicform.api.DynamicFormsWebService
import com.transferwise.dynamicform.api.DynamicSection
import com.transferwise.dynamicform.api.Field
import com.transferwise.dynamicform.api.Group
import com.transferwise.dynamicform.api.Type

class FakeWebService : DynamicFormsWebService {

    private val FAKE_TEXT = field("Account number", "accountNumber", Type.Text, false, true)
    private val FAKE_TEXT_NOT_REQUIRED = field("BIC", "bic", Type.Text, false, false)
    private val FAKE_SELECT = field("Recipient", "legalType", Type.Select, true, false, listOf(AllowedValue("PRIVATE", "Person")))

    override suspend fun getForm(): List<DynamicSection> =
        listOf(DynamicSection("", "iban", listOf(FAKE_SELECT, FAKE_TEXT, FAKE_TEXT_NOT_REQUIRED)))

    override suspend fun refreshForm(attributes: Map<String, String?>, details: Map<String, Any?>) =
        listOf(DynamicSection("", "iban", listOf(FAKE_SELECT)))

    private fun field(name: String, key: String, type: Type, refresh: Boolean, required: Boolean, values: List<AllowedValue>? = null) =
        Field(name, listOf(Group("", key, null, null, name, refresh, required, type, null, values)))
}
