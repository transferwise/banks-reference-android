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

import android.view.View
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.transferwise.dynamicform.R
import com.transferwise.dynamicform.databinding.ItemSelectInputBinding
import com.transferwise.dynamicform.databinding.ItemTabsInputBinding
import com.transferwise.dynamicform.databinding.ItemTextInputBinding
import com.transferwise.dynamicform.generator.Selection
import com.transferwise.dynamicform.generator.UserInput

/**
 * [InputViewHolder] defines a [RecyclerView.ViewHolder] for each and every [UserInput]
 * element. These [InputViewHolder] contain quite a few workarounds to optimize the user
 * experience of the form. (e.g. only show errors when focus is lost)
 */
internal sealed class InputViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    abstract val listener: InputListener
    abstract fun bind(item: T)
}

internal class TabsInputHolder(
    private val binding: ItemTabsInputBinding,
    override val listener: InputListener
) :
    InputViewHolder<UserInput.Tabs>(binding.root) {
    override fun bind(item: UserInput.Tabs) {
        binding.tablayout.removeAllTabs()
        item.options.forEach { binding.tablayout.addTab(binding.tablayout.newTab().setText(it.name)) }
        binding.tablayout.getTabAt(item.options.indexOfFirst { it.key == item.currentValue })!!.select()
        binding.tablayout.doOnTabSelected { tabText -> listener.invoke(item, item.options.first { it.name == tabText }.key) }
    }
}

internal class TextInputHolder(
    private val binding: ItemTextInputBinding,
    override val listener: InputListener
) :
    InputViewHolder<UserInput.Text>(binding.root) {

    override fun bind(item: UserInput.Text) {
        binding.textInputLayout.hint = item.title
        binding.textInputLayout.helperText = item.example
        binding.textInputEdittext.setTextWithNoTextChangeCallback(item.currentValue)
        binding.textInputEdittext.doAfterEdit {
            val text = binding.textInputEdittext.editableText.toString()
            listener.invoke(item, text)
        }
        binding.textInputLayout.updateError(item.error)
    }

    fun updateError(error: String) =
        if (binding.textInputEdittext.hasFocus()) updateErrorWhileFocus(error) else showError(error)

    /**
     * Don't show new errors while typing (e.g. would cause "min length" error to pop up after typing the first character), but
     * postpone showing these to when the focus is lost.
     *
     * For existing errors: clear the error when the user resolves it or update an existing error when it changes.
     */
    private fun updateErrorWhileFocus(error: String) =
        if (isShowingError() && clearError(error)) {
            showError("")
        } else if (isShowingError() && differentError(error)) {
            showError(error)
        } else binding.textInputEdittext.doAfterFocusChange {
            showError(error)
        }

    private fun showError(error: String) {
        binding.textInputEdittext.onFocusChangeListener = null
        binding.textInputLayout.updateError(error)
    }

    private fun differentError(error: String) = error != binding.textInputLayout.error.toString()

    private fun clearError(error: String) = error.isEmpty()

    private fun isShowingError() = !binding.textInputLayout.error.isNullOrEmpty()
}

internal class SelectInputHolder(
    private val binding: ItemSelectInputBinding,
    override val listener: InputListener
) :
    InputViewHolder<UserInput.Select>(binding.root) {

    private lateinit var options: List<Selection>

    override fun bind(item: UserInput.Select) {
        updateOptions(item.options)
        binding.textInputLayout.hint = item.title
        binding.textInputAutocomplete.doOnItemSelected { listener.invoke(item, options[it].key) }
        binding.textInputAutocomplete.setText(options.firstOrNull { it.key == item.currentValue }?.name, false)
        binding.textInputLayout.updateError(item.error)
    }

    fun updateValue(item: UserInput.Select) {
        if (binding.textInputAutocomplete.text.toString() != item.currentValue) {
            binding.textInputAutocomplete.setText(item.options.firstOrNull { it.key == item.currentValue }?.name, false)
        }
        if (binding.textInputLayout.error.toString() != item.error) {
            binding.textInputLayout.updateError(item.error)
        }
        if (options != item.options) {
            updateOptions(item.options)
        }
    }

    private fun updateOptions(list: List<Selection>) {
        options = list
        val adapter = ArrayAdapter(
            binding.root.context,
            R.layout.item_select_popup,
            list.map { it.name }
        )
        binding.textInputAutocomplete.setAdapter(adapter)
    }
}
