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

import com.transferwise.dynamicform.api.DynamicSection
import com.transferwise.dynamicform.generator.UserInput.Tabs.Companion.TABS_KEY

/**
 * Internal helper class that allows to prepare form input to be submitted for backend
 * validation. Note that the provided output [Map] are low level data structures and
 * that it's the client's responsibility to process the output and reformat it for further
 * use.
 */
internal class SubmitFormAttributeGenerator {

    /**
     * Dynamic forms have a hierarchical structure: they consist out of several sections where each
     * section has one or more fields (text, select, radio). Because different sections can have the
     * same fields, each field is uniquely identified by a key that consist out of the parent key (section)
     * and a child key (field).
     *
     * This method returns the current parent key, which is:
     * - either the value of the currenly selected tab (which is a section type)
     * - either the type of the first section
     */
    fun toParentKey(userInputs: List<UserInput>, requirements: List<DynamicSection>): String {
        val currentTab = userInputs.firstOrNull { it is UserInput.Tabs }?.currentValue
        return currentTab ?: requirements[0].type
    }

    /**
     * Creates a map with all parent elements.
     */
    fun toAttributes(userInputs: List<UserInput>, allSections: List<DynamicSection>) = userInputs
        .filter { it.uniqueKey.isTopLevelKey }
        .map { it.uniqueKey.value to it.currentValue }
        .toMutableList()
        .apply {
            if (userInputs.filterIsInstance<UserInput.Tabs>().none()) {
                this.add(TABS_KEY.value to allSections[0].type)
            }
        }
        .toMap()

    /**
     * Creates a map with all child elements, grouped into nested objects when their unique
     * child key contains a "." separator. Children are only included when they have a current
     * value.
     * */
    fun toDetails(userInputs: List<UserInput>) = userInputs
        .filterNot { it.uniqueKey.isTopLevelKey }
        .filterNot { it.currentValue == null }
        .map { input -> input.uniqueKey.childKey!! to input.currentValue!! }
        .toMap()
        .groupObjects()

    /**
     * Whenever the key for an item contains a ".", the item should be converted into a nested object.
     * e.g. details should contain: {"address":{"city","london"}}
     *      instead of {address.city":"london"}
     */
    private fun Map<String, String>.groupObjects() = keys
        .groupBy { it.split(SEPARATOR)[0] }
        .map { if (isCompoundEntry(it)) toCompoundEntry(it) else toSingleEntry(it) }
        .toMap()

    private fun isCompoundEntry(it: Map.Entry<String, List<String>>) = it.value[0].contains(SEPARATOR)

    private fun Map<String, String>.toSingleEntry(it: Map.Entry<String, List<String>>) =
        it.key to get(it.value.first())!!

    private fun Map<String, String>.toCompoundEntry(it: Map.Entry<String, List<String>>): Pair<String, Map<String, String>> {
        val attributeMap = it.value.map { it.split(".")[1] to this.get(it)!! }.toMap()
        return it.key to attributeMap
    }

    companion object {
        const val SEPARATOR = "."
    }
}
