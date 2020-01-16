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
package com.transferwise.banks.util

import android.text.InputFilter
import android.widget.EditText

internal fun EditText.limitDecimalsTo(numDecimals: Int) {
    filters += InputFilter { source, start, end, dest, dstart, dend ->
        val newInput = StringBuilder(dest).replace(dstart, dend, source.subSequence(start, end).toString()).toString()
        if ("[0-9]*((\\.[0-9]{0,$numDecimals})?)||(\\.)?".toRegex().matches(newInput)) null else ""
    }
}
