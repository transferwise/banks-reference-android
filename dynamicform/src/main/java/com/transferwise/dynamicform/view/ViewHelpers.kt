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

import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout

internal fun AutoCompleteTextView.doOnItemSelected(listener: (position: Int) -> Unit) {
    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ -> listener.invoke(position) }
}

internal fun TabLayout.doOnTabSelected(listener: (text: String) -> Unit) {
    this.clearOnTabSelectedListeners()
    this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) = Unit

        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

        override fun onTabSelected(tab: TabLayout.Tab?) = listener.invoke(tab!!.text.toString())
    })
}

/**
 * Ensures TextInputLayout is properly collapsed when error disappears.
 * Note that [helperText] and [error] share the same TextView in the TextInput layout, therefore the error should only
 * be cleared when there is also no helper text
 */
internal fun TextInputLayout.updateError(error: String) {
    this.error = error
    this.isErrorEnabled = error.isNotEmpty() || !helperText.isNullOrEmpty()
}
