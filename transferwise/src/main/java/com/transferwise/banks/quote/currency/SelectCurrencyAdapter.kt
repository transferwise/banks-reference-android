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
package com.transferwise.banks.quote.currency

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.transferwise.banks.databinding.ItemCurrencyBinding

internal typealias SelectCurrencyListener = (CurrencyItem) -> Unit

internal class SelectCurrencyAdapter(val listener: SelectCurrencyListener) :
    ListAdapter<CurrencyItem, SelectCurrencyViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCurrencyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SelectCurrencyViewHolder(ItemCurrencyBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: SelectCurrencyViewHolder, position: Int) = holder.bind(getItem(position), listener)

    private object DiffCallback : DiffUtil.ItemCallback<CurrencyItem>() {
        override fun areItemsTheSame(oldItem: CurrencyItem, newItem: CurrencyItem): Boolean = (oldItem.code == newItem.code)

        override fun areContentsTheSame(oldItem: CurrencyItem, newItem: CurrencyItem): Boolean = (oldItem == newItem)
    }
}

internal class SelectCurrencyViewHolder(private val binding: ItemCurrencyBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CurrencyItem, listener: SelectCurrencyListener) {
        binding.name.text = item.name
        binding.code.text = item.code
        binding.flag.load(item.imageUri)
        binding.root.setOnClickListener { listener.invoke(item) }
    }
}
