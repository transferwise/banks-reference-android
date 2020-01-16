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
package com.transferwise.dynamicform.generator

import com.transferwise.dynamicform.api.DynamicSection
import com.transferwise.dynamicform.api.Field
import com.transferwise.dynamicform.api.Group
import com.transferwise.dynamicform.api.Type
import com.transferwise.dynamicform.generator.UserInput.Tabs.Companion.TABS_KEY
import com.transferwise.dynamicform.generator.UserInput.Text.Companion.NO_VALUE
import com.transferwise.dynamicform.text.TextProvider

/**
 * Takes a raw TransferWise dynamic form and renders it into a list of [UserInput] that can be
 * displayed by the [DynamicFormView]. Note that it also relies on the previous input and
 * previously returned server validation errors to properly create each [UserInput].
 */
internal class DynamicFormGenerator(private val textProvider: TextProvider) {

    /*
    * @param forceRequired when true, treats no user input on required fields as an error
    */
    fun generate(
        sections: List<DynamicSection>,
        previousInput: Map<UniqueKey, String>,
        serverErrors: Map<UniqueKey, String> = emptyMap(),
        forceRequired: Boolean = false
    ): List<UserInput> = convertToUserInput(sections, previousInput, serverErrors, forceRequired)

    private fun convertToUserInput(
        sections: List<DynamicSection>,
        previousInput: Map<UniqueKey, String>,
        serverErrors: Map<UniqueKey, String>,
        forceRequired: Boolean
    ) = mutableListOf<UserInput>().apply {
        val selectedTab = previousInput[TABS_KEY] ?: sections[0].type
        if (sections.size > 1) {
            add(UserInput.Tabs("", selectedTab, TABS_KEY, sections.map { Selection(it.type, it.title) }))
        }
        addAll(sections.first { it.type == selectedTab }.fields
            .map { it.toElement(it.uniqueKey(selectedTab), previousInput, serverErrors, forceRequired) })
    }

    private fun Field.uniqueKey(selectedTab: String) =
        UniqueKey.create(selectedTab, group[0].key)

    private fun Field.toElement(
        key: UniqueKey,
        previousInput: Map<UniqueKey, String>,
        serverErrors: Map<UniqueKey, String>,
        forceRequired: Boolean
    ) = when (group[0].type) {
        Type.Text -> toTextInput(key, previousInput, serverErrors, forceRequired)
        Type.Select, Type.Radio -> toSelectInput(key, previousInput, serverErrors, forceRequired)
    }

    private fun Field.toTextInput(
        key: UniqueKey,
        previousInput: Map<UniqueKey, String>,
        serverErrors: Map<UniqueKey, String>,
        forceRequired: Boolean
    ) = UserInput.Text(
        name,
        previousInput[key] ?: NO_VALUE,
        key,
        getTextInputExample(group[0].example),
        getTextInputError(previousInput[key], serverErrors[key], group[0], forceRequired)
    )

    private fun getTextInputExample(example: String?) =
        if (example != null && example.isNotEmpty()) textProvider.exampleHint(example) else NO_VALUE

    private fun getTextInputError(previousInput: String?, serverError: String?, group: Group, forceRequired: Boolean) =
        inputErrorOrNull(previousInput, serverError, group.required, forceRequired)
            ?: requiredErrorOrNull(previousInput!!, group.required)
            ?: validationErrorOrNull(previousInput!!, group) ?: serverError ?: NO_VALUE

    private fun Field.toSelectInput(
        key: UniqueKey,
        previousInput: Map<UniqueKey, String>,
        serverErrors: Map<UniqueKey, String>,
        forceRequired: Boolean
    ) =
        UserInput.Select(
            name,
            previousInput[key],
            key,
            getSelectInputError(previousInput[key], serverErrors[key], forceRequired),
            group[0].valuesAllowed?.map { Selection(it.key, it.name) } ?: emptyList(),
            group[0].refreshRequirementsOnChange
        )

    private fun Field.getSelectInputError(previousInput: String?, serverError: String?, forceRequired: Boolean) =
        inputErrorOrNull(previousInput, serverError, group[0].required, forceRequired)
            ?: requiredErrorOrNull(previousInput!!, group[0].required) ?: serverError ?: NO_VALUE

    private fun inputErrorOrNull(
        previousInput: String?,
        serverError: String?,
        required: Boolean,
        forceRequired: Boolean
    ) = if (previousInput == null) {
        if (required && forceRequired) textProvider.requiredError() else serverError ?: NO_VALUE
    } else null

    private fun requiredErrorOrNull(previousInput: String, required: Boolean) =
        if (previousInput.isEmpty() && required) textProvider.requiredError() else null

    private fun validationErrorOrNull(previousInput: String, group: Group) =
        if (previousInput.length < group.minLength ?: 0) {
            textProvider.minimumLengthError(group.minLength!!)
        } else if (previousInput.length > group.maxLength ?: Int.MAX_VALUE) {
            textProvider.maximumLengthError(group.maxLength!!)
        } else if (!previousInputIsValid(previousInput, group)) {
            textProvider.validationFailedError(group.name)
        } else null

    private fun previousInputIsValid(previousInput: String, group: Group) =
        group.validationRegexp.isNullOrEmpty() || previousInput.matches(group.validationRegexp.toRegex())
}
