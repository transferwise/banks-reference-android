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
package com.transferwise.dynamicform.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.textfield.TextInputEditText

class SingleTextWatcherEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {

    private var textWatcher: TextWatcher? = null

    fun doAfterEdit(action: (String) -> Unit) {
        removeTextChangedListener(textWatcher)
        textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = action.invoke(s.toString())

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        }
        addTextChangedListener(textWatcher)
    }

    fun doAfterFocusChange(action: () -> Unit) {
        setOnFocusChangeListener { _, _ ->
            action.invoke()
            onFocusChangeListener = null
        }
    }

    fun setTextWithNoTextChangeCallback(text: String?) {
        removeTextChangedListener(textWatcher)
        setText(text)
        addTextChangedListener(textWatcher)
    }
}
