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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.transferwise.dynamicform.databinding.ItemSelectInputBinding
import com.transferwise.dynamicform.databinding.ItemTabsInputBinding
import com.transferwise.dynamicform.databinding.ItemTextInputBinding
import com.transferwise.dynamicform.generator.UserInput

internal typealias InputListener = (UserInput, String) -> Unit

/**
 * [InputAdapter] is a [RecyclerView.Adapter] that is able to display all kinds of dynamic
 * form elements.
 *
 * Notice how the [InputAdapter] is completely stateless and how every single interaction
 * with an [InputViewHolder] triggers a full UI refresh by the [DynamicFormController]. This
 * has the advantage that:
 *
 * - No state mismatch possible between [DynamicFormController] and [InputAdapter] by design!
 * - Less business logic in the [InputViewHolder] and [InputAdapter]
 * - Makes it possible a form summary as a [DynamicFormState]
 *
 * There are some consequences of this choice however, mainly for text input components as
 * a the text of an [EditText] cannot be set while the user is editing it (that would cause
 * the cursor to jump to the start). Therefore, the flow is as follows:
 *
 * - User enters new character
 * - Android [EditText] updates itself with new input
 * - [InputListener] callback is triggered and passed to [DynamicFormController]
 * - [DynamicFormController] recreates all [UserInput] elements based on new input
 * - [InputAdapter] gets a full new list to display
 * - [DiffCallback] detects a text update and issues a partial element update ignoring
 * the text input (already handled by stateful [EditText]) and only updates the elements
 * error message
 */
internal class InputAdapter(private val listener: InputListener) : ListAdapter<UserInput, InputViewHolder<*>>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> TabsInputHolder(ItemTabsInputBinding.inflate(inflater, parent, false), listener)
            2 -> TextInputHolder(ItemTextInputBinding.inflate(inflater, parent, false), listener)
            3 -> SelectInputHolder(ItemSelectInputBinding.inflate(inflater, parent, false), listener)
            else -> throw RuntimeException("Unsupported view type")
        }
    }

    override fun onBindViewHolder(holder: InputViewHolder<*>, position: Int) = bind(holder, position)

    override fun onBindViewHolder(holder: InputViewHolder<*>, position: Int, payloads: MutableList<Any>) =
        if (holder is TextInputHolder && payloads.isNotEmpty()) {
            holder.updateError(payloads[0] as String)
        } else if (holder is SelectInputHolder && payloads.isNotEmpty()) {
            holder.updateValue(payloads[0] as UserInput.Select)
        } else {
            bind(holder, position)
        }

    private fun bind(holder: InputViewHolder<*>, position: Int) = when (holder) {
        is TabsInputHolder -> holder.bind(getItem(position) as UserInput.Tabs)
        is TextInputHolder -> holder.bind(getItem(position) as UserInput.Text)
        is SelectInputHolder -> holder.bind(getItem(position) as UserInput.Select)
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is UserInput.Tabs -> 1
            is UserInput.Text -> 2
            is UserInput.Select -> 3
            else -> throw RuntimeException("Unsupported view type")
        }

    private object DiffCallback : DiffUtil.ItemCallback<UserInput>() {
        override fun areItemsTheSame(oldItem: UserInput, newItem: UserInput): Boolean =
            oldItem.uniqueKey == newItem.uniqueKey

        override fun areContentsTheSame(oldItem: UserInput, newItem: UserInput): Boolean =
            oldItem == newItem

        override fun getChangePayload(oldItem: UserInput, newItem: UserInput) =
            if (newItem is UserInput.Text) newItem.error
            else if (newItem is UserInput.Select) newItem
            else null
    }
}
