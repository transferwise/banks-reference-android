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

import com.transferwise.dynamicform.api.AllowedValue
import com.transferwise.dynamicform.api.DynamicSection
import com.transferwise.dynamicform.api.Field
import com.transferwise.dynamicform.api.Group
import com.transferwise.dynamicform.api.Type
import com.transferwise.dynamicform.fake.FakeTextProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DynamicFormGeneratorTest {

    @Nested
    inner class TextInputs {

        @Test
        internal fun `have title`() {
            val section = section(field(name = "field", type = Type.Text))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Text>().title).isEqualTo("field")
        }

        @Test
        internal fun `have unique key with section`() {
            val section = section(field(type = Type.Text), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Text>().uniqueKey.parentKey).endsWith("tab")
        }

        @Test
        internal fun `have unique key with field key`() {
            val section = section(field("field", Type.Text), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Text>().uniqueKey.childKey).endsWith("field")
        }

        @Test
        internal fun `have empty current value`() {
            val section = section(field(type = Type.Text))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Text>().currentValue).isEmpty()
        }

        @Test
        internal fun `have value provided by user`() {
            val section = section(field("field", Type.Text), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), mapOf(key("tab", "field") to "test"))

            assertThat(items.first<UserInput.Text>().currentValue).isEqualTo("test")
        }

        @Test
        internal fun `have example`() {
            val section = section(field(type = Type.Text, example = "12345678"))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Text>().example).isEqualTo("Example: 12345678")
        }

        @Test
        internal fun `have empty example when empty example`() {
            val section = section(field(type = Type.Text, example = ""))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Text>().example).isEmpty()
        }

        @Test
        internal fun `have empty example when no example`() {
            val section = section(field(type = Type.Text, example = null))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Text>().example).isEmpty()
        }

        @Test
        internal fun `have error when user value exceeds maximum`() {
            val section = section(field(key = "field", type = Type.Text, maxLength = 1), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), mapOf(key("tab", "field") to "test"))

            assertThat(items.first<UserInput.Text>().error).isEqualTo("Maximum length is 1")
        }

        @Test
        internal fun `have error when user value falls short of minimum`() {
            val section = section(field(key = "field", type = Type.Text, minLength = 10), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), mapOf(key("tab", "field") to "test"))

            assertThat(items.first<UserInput.Text>().error).isEqualTo("Minimum length is 10")
        }

        @Test
        internal fun `have no error when no user value`() {
            val section = section(field(key = "field", type = Type.Text, minLength = 10))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Text>().error).isEmpty()
        }

        @Test
        internal fun `have error when no user value, required and forced`() {
            val section = section(field(key = "field", type = Type.Text, required = true))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap(), emptyMap(), true)

            assertThat(items.first<UserInput.Text>().error).isEqualTo("Required")
        }

        @Test
        internal fun `have no error when no user value, not required and forced`() {
            val section = section(field(key = "field", type = Type.Text, required = false))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap(), emptyMap(), true)

            assertThat(items.first<UserInput.Text>().error).isEmpty()
        }

        @Test
        internal fun `have error when empty user value and required`() {
            val section = section(field(key = "field", type = Type.Text, required = true))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap(), emptyMap(), true)

            assertThat(items.first<UserInput.Text>().error).isEqualTo("Required")
        }

        @Test
        internal fun `have no error when empty user value and not required`() {
            val section = section(field(key = "field", type = Type.Text, required = false), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), mapOf(key("tab", "field") to ""))

            assertThat(items.first<UserInput.Text>().error).isEmpty()
        }

        @Test
        internal fun `have empty error message when no minimum`() {
            val section = section(field(key = "field", type = Type.Text), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), mapOf(key("tab", "field") to "_"))

            assertThat(items.first<UserInput.Text>().error).isEmpty()
        }

        @Test
        internal fun `have error when invalid input`() {
            val section = section(field(key = "field", name = "field", type = Type.Text, validationRegexp = "[0-9]"), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), mapOf(key("tab", "field") to "NaN"))

            assertThat(items.first<UserInput.Text>().error).isEqualTo("Please enter valid field")
        }

        @Test
        internal fun `have server error`() {
            val section = section(field(key = "field", name = "field", type = Type.Text), "tab")

            val items = dynamicFormGenerator()
                .generate(listOf(section), mapOf(key("field") to "value"), mapOf(key("tab", "field") to "Server error"))

            assertThat(items.first<UserInput.Text>().error).isEqualTo("Server error")
        }

        @Test
        internal fun `have server error when input null`() {
            val section = section(field(key = "field", type = Type.Text), "tab")

            val items = dynamicFormGenerator()
                .generate(listOf(section), emptyMap(), mapOf(key("tab", "field") to "Server error"))

            assertThat(items.first<UserInput.Text>().error).isEqualTo("Server error")
        }
    }

    @Nested
    inner class SelectInputs {
        @Test
        internal fun `have name`() {
            val section = section(field(name = "field", type = Type.Select))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>().title).isEqualTo("field")
        }

        @Test
        internal fun `have unique key with section`() {
            val section = section(field(type = Type.Select), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>().uniqueKey.parentKey).endsWith("tab")
        }

        @Test
        internal fun `have unique key with field key`() {
            val section = section(field(type = Type.Select, key = "field"), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>().uniqueKey.childKey).endsWith("field")
        }

        @Test
        internal fun `have selections with name`() {
            val section = section(field("field", Type.Select, valuesAllowed = AllowedValue("", "name")))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>().options[0].name).isEqualTo("name")
        }

        @Test
        internal fun `have selections with key`() {
            val section = section(field(type = Type.Select, valuesAllowed = AllowedValue("key", "")))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>().options[0].key).isEqualTo("key")
        }

        @Test
        internal fun `have empty selections when no allowed values`() {
            val section = section(field(type = Type.Select, valuesAllowed = null))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>().options).isEmpty()
        }

        @Test
        internal fun `have error when nothing selected and required`() {
            val section = section(field("field", type = Type.Select, required = true), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), mapOf(key("tab", "field") to ""))

            assertThat(items.first<UserInput.Select>().error).isEqualTo("Required")
        }

        @Test
        internal fun `have no error when no user input and required`() {
            val section = section(field(type = Type.Select, required = true))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>().error).isEqualTo("")
        }

        @Test
        internal fun `have error when no user input, required and forced`() {
            val section = section(field(type = Type.Select, required = true))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap(), emptyMap(), true)

            assertThat(items.first<UserInput.Select>().error).isEqualTo("Required")
        }

        @Test
        internal fun `have no error when no user input, not required and forced`() {
            val section = section(field(type = Type.Select, required = false))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap(), emptyMap(), true)

            assertThat(items.first<UserInput.Select>().error).isEmpty()
        }

        @Test
        internal fun `have no error when user input and required`() {
            val section = section(field("field", Type.Select, required = true), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), mapOf(key("tab", "field") to "value"))

            assertThat(items.first<UserInput.Select>().error).isEqualTo("")
        }

        @Test
        internal fun `have server error`() {
            val section = section(field("field", Type.Select), "tab")

            val items = dynamicFormGenerator().generate(
                listOf(section),
                mapOf(key("field") to " value"),
                mapOf(key("tab", "field") to "Server error")
            )

            assertThat(items.first<UserInput.Select>().error).isEqualTo("Server error")
        }

        @Test
        internal fun `have server error when no current value`() {
            val section = section(field("field", Type.Select), "tab")

            val items = dynamicFormGenerator()
                .generate(listOf(section), emptyMap(), mapOf(key("tab", "field") to "Server error"))

            assertThat(items.first<UserInput.Select>().error).isEqualTo("Server error")
        }

        @Test
        internal fun `have no current value when no user input provided`() {
            val section = section(field(type = Type.Select))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>().currentValue).isNull()
        }

        @Test
        internal fun `have value provided by user`() {
            val section = section(field("field", type = Type.Select), "tab")

            val items = dynamicFormGenerator().generate(listOf(section), mapOf(key("tab", "field") to "test"))

            assertThat(items.first<UserInput.Select>().currentValue).isEqualTo("test")
        }

        @Test
        internal fun `have refresh requirements indicator`() {
            val section = section(field(type = Type.Select, refreshRequirementsOnChange = true))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>().refreshRequirements).isEqualTo(true)
        }

        @Test
        internal fun `have no refresh requirements indicator when not present`() {
            val section = section(field(type = Type.Select, refreshRequirementsOnChange = false))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>().refreshRequirements).isEqualTo(false)
        }
    }

    @Nested
    inner class RadioInputs {

        @Test
        internal fun `should be a select input`() {
            val section = section(field(type = Type.Radio))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items.first<UserInput.Select>()).isNotNull()
        }
    }

    @Nested
    inner class TabsInput {

        @Test
        internal fun `should not contain tabs if only one section`() {
            val section = section(field(type = Type.Radio))

            val items = dynamicFormGenerator().generate(listOf(section), emptyMap())

            assertThat(items[0]).isNotInstanceOf(UserInput.Tabs::class.java)
        }

        @Test
        internal fun `should contain tabs if more than one section`() {
            val section = section(field(type = Type.Radio))

            val items = dynamicFormGenerator().generate(listOf(section, section), emptyMap())

            assertThat(items.first<UserInput.Tabs>()).isNotNull()
        }

        @Test
        internal fun `should have option with title for section`() {
            val section = section(field(), "type", "title")

            val items = dynamicFormGenerator().generate(listOf(section, section), emptyMap())

            assertThat(items.first<UserInput.Tabs>().options[0]).isEqualTo(Selection("type", "title"))
        }

        @Test
        internal fun `should have first section selected by default`() {
            val recipient1 = section(field(), "title1")
            val recipient2 = section(field(), "title2")

            val items = dynamicFormGenerator().generate(listOf(recipient1, recipient2), emptyMap())

            assertThat(items.first<UserInput.Tabs>().currentValue).isEqualTo("title1")
        }

        @Test
        internal fun `should only have items for default tab`() {
            val recipient1 = section(field(type = Type.Text), "title1")
            val recipient2 = section(field(type = Type.Text), "title2")

            val items = dynamicFormGenerator().generate(listOf(recipient1, recipient2), emptyMap())

            assertThat(items).hasSize(2)
        }

        @Test
        internal fun `should only have items with name for default tab`() {
            val recipient1 = section(field(name = "field1", type = Type.Text), "title1")
            val recipient2 = section(field(name = "field2", type = Type.Text), "title2")

            val items = dynamicFormGenerator().generate(listOf(recipient1, recipient2), emptyMap())

            assertThat(items.second<UserInput.Text>().title).isEqualTo("field1")
        }

        @Test
        internal fun `should have selection for currently selected tab`() {
            val recipient1 = section(field(type = Type.Text), "title1")
            val recipient2 = section(field(type = Type.Text), "title2")

            val items = dynamicFormGenerator().generate(listOf(recipient1, recipient2), mapOf(key("type") to "title2"))

            assertThat(items.first<UserInput.Tabs>().currentValue).isEqualTo("title2")
        }

        @Test
        internal fun `should only have items with name for currently selected tab`() {
            val recipient1 = section(field(name = "field1", type = Type.Text), "title1")
            val recipient2 = section(field(name = "field2", type = Type.Text), "title2")

            val items = dynamicFormGenerator().generate(listOf(recipient1, recipient2), mapOf(key("type") to "title2"))

            assertThat(items.second<UserInput.Text>().title).isEqualTo("field2")
        }

        @Test
        internal fun `should have key (necessary to store user input)`() {
            val section = section(field(type = Type.Radio))

            val items = dynamicFormGenerator().generate(listOf(section, section), emptyMap())

            assertThat(items.first<UserInput.Tabs>().uniqueKey).isEqualTo(UniqueKey("type"))
        }
    }

    private fun dynamicFormGenerator() = DynamicFormGenerator(FakeTextProvider())

    private fun <T> List<UserInput>.first() = this[0] as T
    private fun <T> List<UserInput>.second() = this[1] as T

    private fun key(parent: String, child: String = "") =
        UniqueKey.create(parent, child)
}

private fun section(field: Field, type: String = "", title: String = "") =
    DynamicSection(title, type, listOf(field))

private fun field(
    key: String = "",
    type: Type = Type.Text,
    name: String = "",
    example: String? = "",
    maxLength: Int? = null,
    minLength: Int? = null,
    required: Boolean = false,
    validationRegexp: String? = "",
    valuesAllowed: AllowedValue? = AllowedValue("", ""),
    refreshRequirementsOnChange: Boolean = false
) = Field(
    name,
    listOf(
        group(
            key = key, name = name, type = type, example = example, maxLength = maxLength, minLength = minLength,
            required = required, validationRegexp = validationRegexp, valuesAllowed = valuesAllowed,
            refreshRequirementsOnChange = refreshRequirementsOnChange
        )
    )
)

private fun group(
    example: String? = "",
    key: String = "",
    maxLength: Int? = null,
    minLength: Int? = null,
    name: String = "",
    refreshRequirementsOnChange: Boolean = false,
    required: Boolean = false,
    type: Type = Type.Select,
    validationRegexp: String? = "",
    valuesAllowed: AllowedValue? = null
) = Group(
    example, key, maxLength, minLength, name, refreshRequirementsOnChange,
    required, type, validationRegexp, if (valuesAllowed != null) listOf(valuesAllowed) else null
)
