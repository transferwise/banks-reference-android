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
package com.transferwise.dynamicform.api

/**
 * The [DynamicFormsWebService] informs the [DynamicFormController] on how to load and refresh
 * a dynamic form. This class must be implemented by every client of the dynamic forms component.
 */
interface DynamicFormsWebService {
    /**
     * Get the dynamic form from the TransferWise backend and parse it into a list of [DynamicSection].
     * This method will be invoked by the [DynamicFormController] when the form is loaded for the first
     * time.
     */
    suspend fun getForm(): List<DynamicSection>

    /**
     * Refresh the form based on the current user input as represented by [attributes] and [details].
     * This method will be invoked by the [DynamicFormController] any time when the user provides
     * input to a [UserInput] that has [refershRequirementsOnChange] to true.
     *
     * e.g. When selecting the country to be the USA, an extra address field will appear to enter the
     *      state the recipient lives in.
     */
    suspend fun refreshForm(attributes: Map<String, String?>, details: Map<String, Any?>): List<DynamicSection>
}
