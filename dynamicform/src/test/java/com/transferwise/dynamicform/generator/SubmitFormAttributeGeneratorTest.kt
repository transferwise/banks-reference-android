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
import com.transferwise.dynamicform.api.Field
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SubmitFormAttributeGeneratorTest {

    @Nested
    @DisplayName("Parent key")
    inner class ParentKey {

        @Test
        internal fun `is value of tab user input`() {
            val key = SubmitFormAttributeGenerator()
                .toParentKey(listOf(tab("", "iban")), emptyList())

            assertThat(key).isEqualTo("iban")
        }

        @Test
        internal fun `is type of first section when no of tab user input`() {
            val key = SubmitFormAttributeGenerator()
                .toParentKey(listOf(child("", "")), listOf(section("", "bankgiro")))

            assertThat(key).isEqualTo("bankgiro")
        }
    }

    @Nested
    inner class Attributes {

        @Test
        internal fun `contain entry with key`() {
            val userInputs = listOf(tab("parent", "value"))

            val details = SubmitFormAttributeGenerator()
                .toAttributes(userInputs, emptyList())

            assertThat(details).containsKey("parent")
        }

        @Test
        internal fun `contain entry with value`() {
            val userInputs = listOf(tab("parent", "value"))

            val details = SubmitFormAttributeGenerator()
                .toAttributes(userInputs, emptyList())

            assertThat(details).containsValue("value")
        }

        @Test
        internal fun `contain all entries`() {
            val userInputs = listOf(tab("parent1", "value1"), parent("parent2", "value2"))

            val details = SubmitFormAttributeGenerator()
                .toAttributes(userInputs, emptyList())

            assertThat(details).containsValue("value2")
        }

        @Test
        internal fun `contain entry when no current value`() {
            val userInputs = listOf(tab("parent", "value"), parent("parent2", null))

            val details = SubmitFormAttributeGenerator()
                .toAttributes(userInputs, emptyList())

            assertThat(details).containsEntry("parent2", null)
        }

        @Test
        internal fun `contain type of first section when no tab`() {
            val userInputs = listOf(parent("parent2", null))
            val sections = listOf(DynamicSection("title", "sectionType", emptyList()))

            val details = SubmitFormAttributeGenerator()
                .toAttributes(userInputs, sections)

            assertThat(details).containsEntry("type", "sectionType")
        }
    }

    @Nested
    inner class Details {

        @Test
        internal fun `contain entry with child key`() {
            val details = SubmitFormAttributeGenerator()
                .toDetails(listOf(child("child", "value")))

            assertThat(details).containsKey("child")
        }

        @Test
        internal fun `contain entry with value`() {
            val details = SubmitFormAttributeGenerator()
                .toDetails(listOf(child("child", "value")))

            assertThat(details).containsValue("value")
        }

        @Test
        internal fun `contain all entries`() {
            val details = SubmitFormAttributeGenerator()
                .toDetails(listOf(child("child1", "value1"), child("child2", "value2")))

            assertThat(details).containsValue("value2")
        }

        @Test
        internal fun `shouldn't entry when no current value`() {
            val details = SubmitFormAttributeGenerator()
                .toDetails(listOf(child("child", null)))

            assertThat(details).isEmpty()
        }

        @Test
        internal fun `shouldn't entry when top level element (eg tab, name field)`() {
            val details = SubmitFormAttributeGenerator()
                .toDetails(listOf(parent("parent", "value")))

            assertThat(details).isEmpty()
        }

        @Test
        internal fun `should have nested object when dot separated keys`() {
            val details = SubmitFormAttributeGenerator()
                .toDetails(listOf(child("address.city", "value")))

            assertThat(details).containsEntry("address", mapOf("city" to "value"))
        }

        @Test
        internal fun `should have nested objects with multiple values when dot separated keys`() {
            val details = SubmitFormAttributeGenerator()
                .toDetails(listOf(child("address.city", "value"), child("address.street", "value")))

            assertThat(details).containsEntry("address", mapOf("city" to "value", "street" to "value"))
        }
    }

    private fun child(childKey: String, currentValue: String?) =
        UserInput.Text("", currentValue, UniqueKey.create("parent", childKey), "", "")

    private fun parent(key: String, currentValue: String?) =
        UserInput.Text("", currentValue, UniqueKey.create(key), "", "")

    private fun tab(key: String, currentValue: String) =
        UserInput.Tabs("", currentValue, UniqueKey.create(key), emptyList())

    private fun section(title: String, type: String, vararg field: Field) =
        DynamicSection(title, type, field.asList())
}
