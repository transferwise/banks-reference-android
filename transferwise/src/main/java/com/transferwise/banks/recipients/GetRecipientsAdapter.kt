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
package com.transferwise.banks.recipients

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.transferwise.banks.databinding.ItemRecipientBinding

internal typealias SelectRecipientListener = (RecipientItem) -> Unit

internal class GetRecipientsAdapter(val listener: SelectRecipientListener) :
    ListAdapter<RecipientItem, RecipientViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipientViewHolder {
        val binding = ItemRecipientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipientViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    object DiffCallback : DiffUtil.ItemCallback<RecipientItem>() {
        override fun areItemsTheSame(oldItem: RecipientItem, newItem: RecipientItem): Boolean = (oldItem.id == newItem.id)

        override fun areContentsTheSame(oldItem: RecipientItem, newItem: RecipientItem): Boolean = (oldItem == newItem)
    }
}

internal class RecipientViewHolder(private val binding: ItemRecipientBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(recipient: RecipientItem, listener: SelectRecipientListener) {
        binding.initial.text = recipient.initial
        binding.initial.backgroundTintList = ColorStateList.valueOf(recipient.color)
        binding.name.text = recipient.name
        binding.account.text = recipient.account
        binding.root.setOnClickListener { listener.invoke(recipient) }
    }
}
