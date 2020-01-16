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

import com.transferwise.dynamicform.DynamicFormState.Complete
import com.transferwise.dynamicform.DynamicFormState.Loading
import com.transferwise.dynamicform.DynamicFormState.ValidationError
import com.transferwise.dynamicform.generator.UserInput

/**
 * [DynamicFormState] indicates whether the form is:
 *
 * - [Loading]: form is being fetched from the [DynamicFormsWebService]
 * - [InComplete]: not all required fields are filled in
 * - [ValidationError]: the input didn't pass local validation or a server error is shown
 * - [Complete]: all required fields are filled in and pass local validation
 *
 * Note that a [Complete] form can still become a [ValidationError] form after trying to submit the
 * form to the backend service. The server error response can be parsed by the [showServerErrors]
 * method to apply the returned errors to the corresponding [UserInput].
 */
sealed class DynamicFormState {
    abstract val items: List<UserInput>

    object Loading : DynamicFormState() {
        override val items: List<UserInput> = emptyList()
    }

    data class Incomplete(override val items: List<UserInput>) : DynamicFormState()
    data class ValidationError(override val items: List<UserInput>) : DynamicFormState()
    data class Complete(override val items: List<UserInput>) : DynamicFormState()
}
