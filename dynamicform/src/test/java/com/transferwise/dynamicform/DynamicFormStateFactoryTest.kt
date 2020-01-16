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
package com.transferwise.dynamicform

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.transferwise.dynamicform.api.DynamicSection
import com.transferwise.dynamicform.generator.DynamicFormGenerator
import com.transferwise.dynamicform.generator.StaticFormGenerator
import com.transferwise.dynamicform.generator.UniqueKey
import com.transferwise.dynamicform.generator.UserInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

@ExtendWith(MockitoExtension::class)
internal class DynamicFormStateFactoryTest {

    @Mock
    lateinit var dynamicGenerator: DynamicFormGenerator

    @Mock
    lateinit var staticGenerator: StaticFormGenerator

    @Nested
    @DisplayName("Next state")
    inner class NextState {

        @Test
        internal fun `has dynamic form items`() {
            val item = input("")
            val fakeSections = listOf(DynamicSection("title", "type", emptyList()))
            val fakeInput = mapOf(UniqueKey.create("fake") to "value")
            val fakeErrors = mapOf(UniqueKey.create("fake") to "value")
            whenever(dynamicGenerator.generate(fakeSections, fakeInput, fakeErrors, true)).thenReturn(listOf(item))

            val nextState = nextState(fakeSections, fakeInput, fakeErrors, true)

            assertThat(nextState.items).contains(item)
        }

        @Test
        internal fun `has static form items`() {
            val item = input("")
            val fakeInput = mapOf(UniqueKey.create("fake") to "value")
            val fakeErrors = mapOf(UniqueKey.create("fake") to "value")
            whenever(staticGenerator.generate(fakeInput, fakeErrors, true)).thenReturn(listOf(item))

            val nextState = nextState(mock(), fakeInput, fakeErrors, true)

            assertThat(nextState.items).contains(item)
        }

        @Test
        internal fun `is validation error when dynamic form has error`() {
            whenever(dynamicGenerator.generate(any(), any(), any(), eq(false))).thenReturn(listOf(input("error")))

            val nextState = nextState()

            assertThat(nextState).isInstanceOf(DynamicFormState.ValidationError::class.java)
        }

        @Test
        internal fun `is validation error when static form has error`() {
            whenever(staticGenerator.generate(any(), any(), eq(false))).thenReturn(listOf(input("error")))

            val nextState = nextState()

            assertThat(nextState).isInstanceOf(DynamicFormState.ValidationError::class.java)
        }

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        internal fun `is incomplete when dynamic form has empty required fields`() {
            whenever(dynamicGenerator.generate(any(), any(), any(), eq(true))).thenReturn(listOf(input("required")))

            val nextState = nextState(forceRequired = false)

            assertThat(nextState).isInstanceOf(DynamicFormState.Incomplete::class.java)
        }

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        internal fun `is incomplete when static form  has empty required fields`() {
            whenever(staticGenerator.generate(any(), any(), eq(true))).thenReturn(listOf(input("error")))

            val nextState = nextState(forceRequired = false)

            assertThat(nextState).isInstanceOf(DynamicFormState.Incomplete::class.java)
        }

        @Test
        internal fun `is complete when dynamic form has no errors and no empty required fields`() {
            whenever(dynamicGenerator.generate(any(), any(), any(), eq(false))).thenReturn(listOf(input("")))
            whenever(dynamicGenerator.generate(any(), any(), any(), eq(true))).thenReturn(listOf(input("")))

            val nextState = nextState()

            assertThat(nextState).isInstanceOf(DynamicFormState.Complete::class.java)
        }

        @Test
        internal fun `is complete when static form has no errors and no empty required fields`() {
            whenever(staticGenerator.generate(any(), any(), eq(false))).thenReturn(listOf(input("")))
            whenever(staticGenerator.generate(any(), any(), eq(true))).thenReturn(listOf(input("")))

            val nextState = nextState()

            assertThat(nextState).isInstanceOf(DynamicFormState.Complete::class.java)
        }
    }

    private fun nextState(
        sections: List<DynamicSection> = emptyList(),
        userInput: Map<UniqueKey, String> = emptyMap(),
        serverErrors: Map<UniqueKey, String> = emptyMap(),
        forceRequired: Boolean = false
    ) = DynamicFormStateFactory(dynamicGenerator, staticGenerator).nextState(sections, userInput, serverErrors, forceRequired)

    private fun input(error: String) =
        UserInput.Text("", "", UniqueKey("key"), "", error)
}
