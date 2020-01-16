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
package com.banks.demo.transfer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.banks.demo.R
import com.banks.demo.databinding.ItemSectionBinding
import com.banks.demo.databinding.ItemTransferBinding
import com.banks.demo.transfer.TransferItem.Section
import com.banks.demo.transfer.TransferItem.Transaction

/*
 *  ⚠️ This is only part of the repository for demo purposes, but has no value as reference code. ⚠️
 */
internal class DemoTransferAdapter : ListAdapter<TransferItem, TransferViewHolder<*>>(AlwaysRefreshCallback) {

    init {
        submitList(
            listOf(
                Section("Today"),
                Transaction("London cab", "#f8dc93", R.drawable.transfer_taxi, 8.75f),
                Transaction("ATM", "#89d5ae", R.drawable.transfer_misc, 20f),

                Section("Yesterday"),
                Transaction("Joe & The juice", "#8ad5ae", R.drawable.transfer_food, 11.20f),
                Transaction("Tom's supermarket", "#98d0f5", R.drawable.transfer_groceries, 7.30f),
                Transaction("H&M", "#fdb3ee", R.drawable.transfer_groceries, 15.00f),
                Transaction("Starbucks", "#bbbcfc", R.drawable.transfer_food, 4.30f),
                Transaction("Uber", "#f9c47e", R.drawable.transfer_taxi, 6.75f),
                Transaction("Nando's", "#d9dde6", R.drawable.transfer_food, 22.00f),

                Section("Last week"),
                Transaction("Tube", "#ffa1ac", R.drawable.transfer_taxi, 2.50f),
                Transaction("Sainsbury's", "#d9dde6", R.drawable.transfer_groceries, 32.15f),
                Transaction("ATM", "#8ad5ae", R.drawable.transfer_misc, 30f)
            )
        )
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Transaction -> 1
        is Section -> 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder<*> = when (viewType) {
        1 -> TransactionViewHolder(ItemTransferBinding.inflate(parent.layoutInflater, parent, false))
        2 -> SectionViewHolder(ItemSectionBinding.inflate(parent.layoutInflater, parent, false))
        else -> throw RuntimeException("Unsupported view type")
    }

    override fun onBindViewHolder(holder: TransferViewHolder<*>, position: Int) = when (holder) {
        is TransactionViewHolder -> holder.bind(getItem(position) as Transaction)
        is SectionViewHolder -> holder.bind(getItem(position) as Section)
    }

    private val ViewGroup.layoutInflater get() = LayoutInflater.from(context)

    object AlwaysRefreshCallback : DiffUtil.ItemCallback<TransferItem>() {
        override fun areItemsTheSame(oldItem: TransferItem, newItem: TransferItem): Boolean = false

        override fun areContentsTheSame(oldItem: TransferItem, newItem: TransferItem): Boolean = false
    }
}
