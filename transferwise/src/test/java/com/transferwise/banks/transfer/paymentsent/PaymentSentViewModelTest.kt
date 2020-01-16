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
package com.transferwise.banks.transfer.paymentsent

import com.nhaarman.mockitokotlin2.mock
import com.transferwise.banks.shared.NavigationAction
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.testutils.TestLiveDataExtension
import com.transferwise.banks.util.fake.FakeConfiguration
import com.transferwise.banks.util.fake.FakeTextProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestLiveDataExtension::class)
class PaymentSentViewModelTest {

    @Test
    internal fun `can instantiate`() {
        PaymentSentViewModel(mock(), 0f, "", "", FakeTextProvider())
    }

    @Nested
    @DisplayName("When screen opens")
    inner class ScreenOpen {
        @Test
        internal fun `show payment success`() {
            val viewModel = PaymentSentViewModel(mock(), 0f, "", "", FakeTextProvider())

            assertThat(viewModel.uiState.value).isInstanceOf(PaymentSentUiState::class.java)
        }

        @Test
        internal fun `show recipient summary`() {
            val viewModel = PaymentSentViewModel(mock(), 50f, "GBP", "Will Davies", FakeTextProvider())

            assertThat(viewModel.uiState.value!!.summary).isEqualTo("You sent 50.0 GBP to Will Davies")
        }
    }

    @Nested
    @DisplayName("When tap screen")
    inner class TapScreen {
        @Test
        internal fun `close international payments`() {
            val sharedViewModel = SharedViewModel(FakeConfiguration)

            PaymentSentViewModel(sharedViewModel, 0f, "", "", FakeTextProvider()).doOnNextClicked()

            assertThat(sharedViewModel.navigationAction.value).isEqualTo(NavigationAction.Done)
        }
    }
}
