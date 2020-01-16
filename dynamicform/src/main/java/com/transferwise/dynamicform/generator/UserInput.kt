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
package com.transferwise.dynamicform.generator

sealed class UserInput {
    abstract val title: String
    abstract val currentValue: String?
    abstract val uniqueKey: UniqueKey
    abstract val error: String

    data class Text(
        override val title: String,
        override val currentValue: String?,
        override val uniqueKey: UniqueKey,
        val example: String,
        override val error: String
    ) : UserInput() {

        companion object {
            const val NO_VALUE = ""
        }
    }

    data class Select(
        override val title: String,
        override val currentValue: String?,
        override val uniqueKey: UniqueKey,
        override val error: String,
        val options: List<Selection>,
        val refreshRequirements: Boolean
    ) : UserInput()

    data class Tabs(
        override val title: String,
        override val currentValue: String,
        override val uniqueKey: UniqueKey,
        val options: List<Selection>
    ) : UserInput() {
        override val error: String = ""

        companion object {
            val TABS_KEY = UniqueKey.create("type")
        }
    }
}

data class Selection(val key: String, val name: String)
