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

import android.graphics.Color
import androidx.annotation.ColorInt

internal interface RecipientHelper {
    fun initials(name: String): String

    @ColorInt
    fun color(name: String): Int
}

internal class DefaultRecipientHelper : RecipientHelper {

    override fun initials(name: String) = name.split(" ").take(2).fold("", { a, b -> a + b.first() })

    @ColorInt
    override fun color(name: String): Int = backgroundColors[Math.abs(name.hashCode()) % backgroundColors.size]
}

private val backgroundColors = intArrayOf(
    Color.parseColor("#d9dde6"),
    Color.parseColor("#8ad5ae"),
    Color.parseColor("#bbbcfc"),
    Color.parseColor("#f9c47e"),
    Color.parseColor("#f8dc93"),
    Color.parseColor("#ffa1ac"),
    Color.parseColor("#89d5ae"),
    Color.parseColor("#98d0f5"),
    Color.parseColor("#fdb3ee")
)
