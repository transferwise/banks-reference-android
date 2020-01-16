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

import com.transferwise.dynamicform.DynamicFormState.Loading
import com.transferwise.dynamicform.fake.FakeStaticFormGenerator
import com.transferwise.dynamicform.fake.FakeTextProvider
import com.transferwise.dynamicform.fake.FakeWebService
import com.transferwise.dynamicform.generator.UniqueKey
import com.transferwise.dynamicform.generator.UserInput
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@Tag("Integration tests - doesn't test functionality in isolation, but tests several classes together")
@ExtendWith(MockitoExtension::class)
internal class DynamicFormControllerTest {

    @Test
    internal fun `initial state should be loading`() {
        val controller = DynamicFormController(FakeTextProvider())

        assertThat(controller.uiState.value).isEqualTo(Loading)
    }

    @Nested
    @DisplayName("Show form")
    inner class ShowForm {

        @Test
        internal fun `includes dynamic input elements`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())

            controller.showForm(this, FakeWebService())

            assertThat(controller.items).hasSize(3)
        }

        @Test
        internal fun `includes static input elements`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())

            controller.showForm(this, FakeWebService(), FakeStaticFormGenerator())

            assertThat(controller.items).contains(text("Name", "name", ""))
        }

        @Test
        internal fun `can only be invoked once`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())

            controller.showForm(this, FakeWebService())

            assertThrows<RuntimeException> { controller.showForm(this, FakeWebService()) }
        }
    }

    @Nested
    @DisplayName("When user input")
    inner class UserInputs {

        @Test
        internal fun `refresh ui with new input`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService())

            controller.doOnUserInput(childText("Account number", "accountNumber", null), "7777")

            assertThat(controller.items).contains(childText("Account number", "accountNumber", "7777"))
        }

        @Test
        internal fun `clear server errors for that user input`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService())
            controller.showServerErrors(ERROR_JSON)

            controller.doOnUserInput(childText("BIC", "bic", ""), "")

            assertThat(controller.items).contains(childText("BIC", "bic", "", ""))
        }

        @Test
        internal fun `don't clear server errors for other user input`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService())
            controller.showServerErrors(ERROR_JSON)

            controller.doOnUserInput(childText("Account number", "accountNumber", ""), "")

            assertThat(controller.items).contains(childText("BIC", "bic", "", "Invalid"))
        }

        @Test
        internal fun `refresh requirements when input requires that`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService())

            controller.doOnUserInput(childSelect("", "legalType", true), "7777")

            assertThat(controller.items).hasSize(1)
        }

        @Test
        internal fun `don't refresh requirements when input doesn't requires that`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService())

            controller.doOnUserInput(childSelect("", "legalType", false), "7777")

            assertThat(controller.items).hasSize(3)
        }
    }

    @Nested
    @DisplayName("Show local errors")
    inner class ShowLocalErrors {
        @Test
        internal fun `refreshes ui state with required fields error`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService())

            controller.showLocalErrors()

            assertThat(controller.items).contains(childText("Account number", "accountNumber", "", "Required"))
        }

        @Test
        internal fun `doesn't refresh ui when from not yet loaded`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())

            controller.showLocalErrors()

            assertThat(controller.uiState.value).isEqualTo(Loading)
        }
    }

    @Nested
    @DisplayName("Show server errors")
    inner class ShowServerErrors {

        @Test
        internal fun `for child elements`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService())

            controller.showServerErrors(ERROR_JSON)

            assertThat(controller.items).contains(childText("BIC", "bic", "", "Invalid"))
        }

        @Test
        internal fun `for parent elements`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService(), FakeStaticFormGenerator())

            controller.showServerErrors(ERROR_JSON)

            assertThat(controller.items).contains(text("Name", "name", "", "Invalid"))
        }
    }

    @Nested
    @DisplayName("Filled in form contains")
    inner class FormResult {

        @Test
        internal fun `attributes with top level elements`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService(), FakeStaticFormGenerator())

            assertThat(controller.currentAttributes()).containsEntry("name", "")
        }

        @Test
        internal fun `attributes with form type`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService(), FakeStaticFormGenerator())

            assertThat(controller.currentAttributes()).containsEntry("type", "iban")
        }

        @Test
        internal fun `details with child elements`() = runBlockingTest {
            val controller = DynamicFormController(FakeTextProvider())
            controller.showForm(this, FakeWebService(), FakeStaticFormGenerator())

            assertThat(controller.currentDetails()).containsEntry("accountNumber", "")
        }
    }

    private val DynamicFormController.items get() = uiState.value.items

    companion object {

        fun text(title: String, key: String, currentValue: String?, error: String = "") =
            UserInput.Text(title, currentValue, UniqueKey(key), "", error)

        fun childText(title: String, key: String, currentValue: String?, error: String = "") =
            UserInput.Text(title, currentValue, UniqueKey.create("iban", key), "", error)

        private fun childSelect(title: String, key: String, refresh: Boolean = false) =
            UserInput.Select(title, "", UniqueKey.create("iban", key), "", emptyList(), refresh)

        private val ERROR_JSON = """{
                "timestamp": "05-11-2019 01:10:44",
                "status": "UNPROCESSABLE_ENTITY",
                "message": "{
                    \"timestamp\":\"2019-11-05T13:10:44.496175Z\",
                    \"errors\":[
                        {
                            \"code\":\"NOT_VALID\",
                            \"message\":\"Invalid\",
                            \"path\":\"bic\",
                            \"arguments\":[\"bic\",\"A\",\"2\",\"70\"]
                        },
                        {
                            \"code\":\"NOT_VALID\",
                            \"message\":\"Invalid\",
                            \"path\":\"name\",
                            \"arguments\":[\"name\",\"A\",\"2\",\"70\"]
                        }
                    ]
                }"
            }"""
    }
}
