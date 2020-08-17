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

import com.transferwise.banks.util.TextProvider
import com.transferwise.dynamicform.generator.StaticFormGenerator
import com.transferwise.dynamicform.generator.UniqueKey
import com.transferwise.dynamicform.generator.UserInput

/**
 * Recipient creation screens requires to render a static "name" [UserInput.Text] field on top of the
 * dynamic form.
 */
internal class CreateRecipientStaticFormGenerator(private val errorProvider: TextProvider) :
    StaticFormGenerator {

    override fun generate(userInput: Map<UniqueKey, String>, serverErrors: Map<UniqueKey, String>, showRequired: Boolean) =
        listOf(createNameInput(userInput, serverErrors, showRequired))

    private fun createNameInput(userInput: Map<UniqueKey, String>, serverErrors: Map<UniqueKey, String>, showRequired: Boolean) =
        UserInput.Text(
            NAME_TITLE,
            userInput[NAME_KEY],
            NAME_KEY,
            "",
            getError(userInput[NAME_KEY], serverErrors, showRequired)
        )

    private fun getError(previousInput: String?, serverErrors: Map<UniqueKey, String>, forceRequired: Boolean) = when {
        previousInput == null -> if (forceRequired) errorProvider.requiredError() else ""
        previousInput.isEmpty() -> errorProvider.requiredError()
        previousInput.notMatches(NAME_VALIDATION) -> errorProvider.validationFailedError(NAME_ERROR_MESSAGE)
        serverErrors[NAME_KEY] != null -> serverErrors[NAME_KEY]!!
        else -> ""
    }

    private fun String.notMatches(regex: String) = !matches(regex.toRegex())

    companion object {
        val NAME_KEY = UniqueKey("accountHolderName")
        private const val NAME_TITLE = "Account holder's full name"
        private const val NAME_VALIDATION = "^([a-zA-Z]{2,}[ ]{0,}){2,}\$"
        private const val NAME_ERROR_MESSAGE = "full name of the person or business you are sending to"
    }
}
