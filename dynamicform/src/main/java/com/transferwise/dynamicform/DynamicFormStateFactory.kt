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
package com.transferwise.dynamicform

import com.transferwise.dynamicform.api.DynamicSection
import com.transferwise.dynamicform.generator.DynamicFormGenerator
import com.transferwise.dynamicform.generator.StaticFormGenerator
import com.transferwise.dynamicform.generator.UniqueKey
import com.transferwise.dynamicform.generator.UserInput

/**
 * Uses the [DynamicFormGenerator] and [StaticFormGenerator] to generate the items for the form,
 * combines them with existing [userInput] and [serverErrors] and wraps them into the corresponding
 * [DynamicFormState].
 */
internal class DynamicFormStateFactory(
    private val dynamicFormGenerator: DynamicFormGenerator,
    private val staticFormGenerator: StaticFormGenerator
) {

    /*
    * @param forceRequired when true, treats no user input on required fields as an error
     */
    fun nextState(
        sections: List<DynamicSection>,
        userInput: Map<UniqueKey, String>,
        serverErrors: Map<UniqueKey, String>,
        forceRequired: Boolean = false
    ): DynamicFormState {
        val allItems = allItems(sections, userInput, serverErrors, forceRequired)
        return when {
            allItems.hasErrors() -> DynamicFormState.ValidationError(allItems)
            hasRequiredErrors(userInput, serverErrors, sections) -> DynamicFormState.Incomplete(allItems)
            else -> DynamicFormState.Complete(allItems)
        }
    }

    private fun allItems(
        sections: List<DynamicSection>,
        input: Map<UniqueKey, String>,
        errors: Map<UniqueKey, String>,
        forceRequired: Boolean
    ) =
        mutableListOf<UserInput>().apply {
            addAll(staticFormGenerator.generate(input, errors, forceRequired))
            addAll(dynamicFormGenerator.generate(sections, input, errors, forceRequired))
        }

    private fun hasRequiredErrors(input: Map<UniqueKey, String>, errors: Map<UniqueKey, String>, sections: List<DynamicSection>) =
        allItems(sections, input, errors, true).hasErrors()

    private fun List<UserInput>.hasErrors() = filterNot { it.error.isNullOrEmpty() }.any()
}
