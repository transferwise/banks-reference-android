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

import android.content.Context
import com.transferwise.dynamicform.api.DynamicFormsWebService
import com.transferwise.dynamicform.api.DynamicSection
import com.transferwise.dynamicform.api.ErrorConverter
import com.transferwise.dynamicform.generator.DynamicFormGenerator
import com.transferwise.dynamicform.generator.NoStaticFormGenerator
import com.transferwise.dynamicform.generator.StaticFormGenerator
import com.transferwise.dynamicform.generator.SubmitFormAttributeGenerator
import com.transferwise.dynamicform.generator.UniqueKey
import com.transferwise.dynamicform.generator.UserInput
import com.transferwise.dynamicform.text.LocalizedTextProvider
import com.transferwise.dynamicform.text.TextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch

/**
 * The [DynamicFormController] contains all of the business logic to load, display, refresh, validate
 * and process a dynamic form. It doesn't contain any reference to the [DynamicFormView] making it
 * completely safe to use in any business logic without risking to leak the view.
 *
 * In order to start displaying a form on the [DynamicFormView], you need to call [showForm] on the
 * controller. This method will then get the dynamic form from the [DynamicFormsWebService], combine
 * it with the static elements created by the [StaticFormGenerator] and convert all elements to
 * [UserInput] that can be displayed by the [DynamicFormView].
 *
 * To simplify the integration of dynamic forms into your application, the [DynamicFormController]
 * will process and validate all user input, combine them with input errors returned by the server
 * and expose a [uiState] that indicates the [DynamicFormState] of the form.
 *
 * The [DynamicFormController] doesn't handle any kind of form submission. This is because not every
 * form needs to be submitted for validation to the backend:
 *
 * - creating a recipient: form is submitted after completion and the server can reject the form
 *     causing server errors to pop up (e.g. if IBAN is invalid)
 * - extra transfer details: the provided input is only locally validated and submitted in a later step
 *     when the full transfer is confirmed by the user.
 *
 * So instead of form submission it exposes two maps:
 *
 * 1. [currentAttibutes]: contains all top level input (everything excluding content in a tab)
 * 2. [currentDetails]: contains all content within a tab, rendered as nested objects when needed
 *
 * While the [DynamicFormController] might seem similar to a [RecyclerViewAdapter], it is actually
 * much more that just a component that renders elements to the screen. Therefore it was designed
 * not to hold any reference to the [DynamicFormView] so it can safely be used in business logic
 * without having to worry about leaking the view.
 *
 * These are some of the key problems the [DynamicFormController] solves:
 *
 * - Converting dynamic form sections to separate UI tabs if there is more than one
 * - Rendering text input fields with local error validations (min/max length, regex,... )
 * - Refreshing the complete form when a selection input requires that
 *    (e.g. choosing the USA as the country adds a "state" input field to the form)
 * - Summarizing the current form state into either InComplete, ValidationError or Complete
 * - Allowing to add static components on top of the dynamic ones (e.g. recipient names)
 * - Processes the form output to collections that can be sent back to the backend
 *
 */
@ExperimentalCoroutinesApi
class DynamicFormController internal constructor(textProvider: TextProvider) {

    internal constructor(context: Context) : this(LocalizedTextProvider(context.resources))

    // DynamicFormState exposed using Kotlin Flow, so it can be observed from non lifecycle aware
    // components. Similar to [LiveData], the [ConflatedBroadcastChannel] will reemit the latest
    // value after subscribing.
    val uiState = ConflatedBroadcastChannel<DynamicFormState>()

    private var requirements: List<DynamicSection> = emptyList()
    private val userInput = mutableMapOf<UniqueKey, String>()
    private val serverErrors = mutableMapOf<UniqueKey, String>()

    private val dynamicFormGenerator: DynamicFormGenerator = DynamicFormGenerator(textProvider)

    private lateinit var webService: DynamicFormsWebService
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var factory: DynamicFormStateFactory

    init {
        uiState.offer(DynamicFormState.Loading)
    }

    /**
     * Start loading and showing the dynamic form.
     *
     * @param [CoroutineScope] required to auto cancel all resources when no longer needed
     * @param [DynamicFormsWebService] required to know how to load and refresh the form
     * @param [StaticFormGenerator] optionally used to render static elements on top of the dynamic ones.
     */
    fun showForm(
        scope: CoroutineScope,
        dynamicWebService: DynamicFormsWebService,
        staticFormGenerator: StaticFormGenerator = NoStaticFormGenerator
    ) {
        if (::coroutineScope.isInitialized) {
            throw RuntimeException("Show form is not thread safe and can only be invoked once")
        }

        coroutineScope = scope
        webService = dynamicWebService
        factory = DynamicFormStateFactory(dynamicFormGenerator, staticFormGenerator)

        coroutineScope.launch {
            requirements = webService.getForm()
            uiState.offer(factory.nextState(requirements, userInput, serverErrors))
        }
    }

    /**
     * In order to be more user friendly, not all errors are shown to the user immediately. (e.g. every
     * freshly loaded form would otherwise immediately show tons of "required" errors)
     *
     * Calling this method will cause every [Incomplete] form to show all required [UserInput] errors.
     * This is typically used if you detect the form is [Incomplete] or [ValidationError] and the user
     * already tries to submit it.
     */
    fun showLocalErrors() {
        if (::coroutineScope.isInitialized) {
            uiState.offer(factory.nextState(requirements, userInput, serverErrors, true))
        }
    }

    /**
     * When submitting a form to the backend, the server can reject it due to server side validation
     * (e.g. invalid IBAN). Calling this method after such errors with the raw server [errorResponse]
     * will ensure those errors are properly displayed in the form.
     *
     * The [ErrorConverter] parses a raw error response from a backend form validation into a [Map]
     * of errors. Note that the [Map] key is the [UniqueKey] of the [UserInput] when that's a top level
     * element, otherwise it's the child part of the unique key and hence this method will first convert
     * it to a full [UniqueKey].
     *
     * Note that not every form needs to be submitted for server validation, that choice is up to the
     * client.
     */
    fun showServerErrors(errorResponse: String?) {
        val errors = ErrorConverter().convert(errorResponse)
        val errorsWithProperKey = errors.map { error ->
            val isTopLevelKey = uiState.value!!.items.filter { it.uniqueKey.value == error.key }.any()
            if (isTopLevelKey) {
                UniqueKey.create(error.key) to error.value
            } else {
                UniqueKey.create(
                    SubmitFormAttributeGenerator().toParentKey(uiState.value!!.items, requirements),
                    error.key
                ) to error.value
            }
        }

        serverErrors.putAll(errorsWithProperKey)
        uiState.offer(factory.nextState(requirements, userInput, serverErrors, true))
    }

    /**
     * Map of all top level elements in the dynamic form, this includes:
     * - one entry with key `type`, representing the currently selected tab (or the first single section of the form when no tabs)
     * - one key - value entry for each static element in the form as provided by the [StaticFormGenerator]
     */
    fun currentAttributes() = SubmitFormAttributeGenerator().toAttributes(uiState.value.items, requirements)

    /*
     * Map of all elements withing the current tab/section, converted to nested maps when the elements
     * contain "." separators.
     *
     * e.g. details will contain: {"address":{"city","london"}} instead of {address.city":"london"}
     */
    fun currentDetails() = SubmitFormAttributeGenerator().toDetails(uiState.value.items)

    /*
     * Internal method that is called after every interaction of the user with the form (e.g. the UI is stateless!).
     * This ensures that based on the new input, the previous input and the server errors that the correct
     * next state of the form is calculated.
     */
    internal fun doOnUserInput(inputElement: UserInput, input: String) {
        userInput[inputElement.uniqueKey] = input
        serverErrors.remove(inputElement.uniqueKey)
        uiState.offer(factory.nextState(requirements, userInput, serverErrors))

        if (inputElement is UserInput.Select && inputElement.refreshRequirements) {
            refreshRequirements()
        }
    }

    private fun refreshRequirements() = coroutineScope.launch {
        requirements = webService.refreshForm(currentAttributes(), currentDetails())
        uiState.offer(factory.nextState(requirements, userInput, serverErrors))
    }
}
