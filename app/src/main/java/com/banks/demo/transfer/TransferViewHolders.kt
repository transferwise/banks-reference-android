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

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.banks.demo.databinding.ItemSectionBinding
import com.banks.demo.databinding.ItemTransferBinding
import com.banks.demo.transfer.TransferItem.Section
import com.banks.demo.transfer.TransferItem.Transaction

/*
 *  ⚠️ This is only part of the repository for demo purposes, but has no value as reference code. ⚠️
 */
internal sealed class TransferViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: T)
}

internal class TransactionViewHolder(val binding: ItemTransferBinding) : TransferViewHolder<Transaction>(binding.root) {

    override fun bind(item: Transaction) {
        binding.name.text = item.name
        binding.icon.setImageResource(item.icon)
        binding.icon.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.color))
        binding.code.text = String.format("%.2f", item.amount)
    }
}

internal class SectionViewHolder(val binding: ItemSectionBinding) : TransferViewHolder<Section>(binding.root) {

    override fun bind(item: Section) {
        binding.title.text = item.title
    }
}

internal sealed class TransferItem {
    data class Section(val title: String) : TransferItem()
    data class Transaction(val name: String, val color: String, @DrawableRes val icon: Int, val amount: Float) : TransferItem()
}
