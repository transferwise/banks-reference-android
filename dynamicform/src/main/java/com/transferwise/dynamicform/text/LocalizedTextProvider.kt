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
package com.transferwise.dynamicform.text

import android.content.res.Resources
import com.transferwise.dynamicform.R

internal class LocalizedTextProvider(private val resources: Resources) : TextProvider {

    override fun requiredError() = resources.getString(R.string.error_input_required)

    override fun minimumLengthError(minimum: Int) = resources.getString(R.string.error_input_tooshort, minimum)

    override fun maximumLengthError(maximum: Int) = resources.getString(R.string.error_input_toolong, maximum)

    override fun validationFailedError(fieldName: String) =
        resources.getString(R.string.error_input_validationfailed, fieldName)

    override fun exampleHint(example: String) = resources.getString(R.string.hint_example, example)
}
