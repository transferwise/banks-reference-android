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
package com.transferwise.banks.recipients.create

import com.transferwise.banks.util.fake.FakeTextProvider
import com.transferwise.dynamicform.generator.UniqueKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CreateRecipientStaticFormGeneratorTest {

    @Nested
    inner class NameField {
        @Test
        internal fun `has title`() {
            val items = generate()

            assertThat(items[0].title).isEqualTo("Name")
        }

        @Test
        internal fun `has unique key`() {
            val items = generate()

            assertThat(items[0].uniqueKey).isEqualTo(UniqueKey.create("accountHolderName"))
        }

        @Test
        internal fun `has no sample`() {
            val items = generate()

            assertThat(items[0].example).isEmpty()
        }

        @Test
        internal fun `has no error when no input`() {
            val items = generate(userInput = emptyMap())

            assertThat(items[0].error).isEmpty()
        }

        @Test
        internal fun `has error when no input and show required`() {
            val items = generate(userInput = emptyMap(), showRequired = true)

            assertThat(items[0].error).isEqualTo("Required")
        }

        @Test
        internal fun `has no current value`() {
            val items = generate()

            assertThat(items[0].currentValue).isNull()
        }

        @Test
        internal fun `has previous value`() {
            val items = generate(userInput = mapOf(UniqueKey.create("accountHolderName") to "previousvalue"))

            assertThat(items[0].currentValue).isEqualTo("previousvalue")
        }

        @Test
        internal fun `has error when previous value empty (user already focussed on field)`() {
            val items = generate(userInput = mapOf(UniqueKey.create("accountHolderName") to ""))

            assertThat(items[0].error).isEqualTo("Required")
        }

        @Test
        internal fun `has error when less than one character`() {
            val items = generate(userInput = mapOf(UniqueKey.create("accountHolderName") to "j"))

            assertThat(items[0].error).isEqualTo("Please enter valid Name")
        }

        @Test
        internal fun `has error when less than two word`() {
            val items = generate(userInput = mapOf(UniqueKey.create("accountHolderName") to "dj"))

            assertThat(items[0].error).isEqualTo("Please enter valid Name")
        }

        @Test
        internal fun `has error when first word less than one character`() {
            val items = generate(userInput = mapOf(UniqueKey.create("accountHolderName") to "j jj"))

            assertThat(items[0].error).isEqualTo("Please enter valid Name")
        }

        @Test
        internal fun `has error when second word less than one character`() {
            val items = generate(userInput = mapOf(UniqueKey.create("accountHolderName") to "jj j"))

            assertThat(items[0].error).isEqualTo("Please enter valid Name")
        }

        @Test
        internal fun `has no error when more than two words`() {
            val items = generate(userInput = mapOf(UniqueKey.create("accountHolderName") to "jj jj jj"))

            assertThat(items[0].error).isEmpty()
        }

        @Test
        internal fun `has error when number`() {
            val items = generate(userInput = mapOf(UniqueKey.create("accountHolderName") to "jj jj1"))

            assertThat(items[0].error).isEqualTo("Please enter valid Name")
        }

        @Test
        internal fun `has server error`() {
            val items = generate(
                userInput = mapOf(UniqueKey.create("accountHolderName") to "jj jj"),
                serverErrors = mapOf(UniqueKey.create("accountHolderName") to "Server error")
            )

            assertThat(items[0].error).isEqualTo("Server error")
        }
    }

    private fun generate(
        userInput: Map<UniqueKey, String> = emptyMap(),
        serverErrors: Map<UniqueKey, String> = emptyMap(),
        showRequired: Boolean = false
    ) = CreateRecipientStaticFormGenerator(FakeTextProvider()).generate(userInput, serverErrors, showRequired)
}
