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
package com.transferwise.banks.shared

import com.transferwise.banks.shared.QuoteDescription.FixedSource
import com.transferwise.banks.shared.QuoteDescription.FixedTarget
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class QuoteDescriptionTest {

    @Nested
    @DisplayName("Conversion to fixed source")
    inner class FixedSourceQuote {
        @Test
        internal fun `is a FixedSource QuoteDescription`() {
            val quote = FixedTarget(0f, "", "").toFixedSource(0f)

            assertThat(quote).isInstanceOf(FixedSource::class.java)
        }

        @Test
        internal fun `has updated amount`() {
            val quote = FixedTarget(0f, "", "").toFixedSource(100f)

            assertThat(quote.amount).isEqualTo(100f)
        }

        @Test
        internal fun `has source currency`() {
            val quote = FixedTarget(0f, "GBP", "").toFixedSource(0f)

            assertThat(quote.sourceCurrency).isEqualTo("GBP")
        }

        @Test
        internal fun `has target currency`() {
            val quote = FixedTarget(0f, "", "EUR").toFixedSource(0f)

            assertThat(quote.targetCurrency).isEqualTo("EUR")
        }

        @Test
        internal fun `has no quote id (quote not yet fetched from backend)`() {
            val quote = FixedTarget(0f, "", "", "myid").toFixedSource(0f)

            assertThat(quote.id).isNull()
        }
    }

    @Nested
    @DisplayName("Conversion to fixed target")
    inner class FixedTargetQuote {
        @Test
        internal fun `is a FixedTarget QuoteDescription`() {
            val quote = FixedSource(0f, "", "").toFixedTarget(0f)

            assertThat(quote).isInstanceOf(FixedTarget::class.java)
        }

        @Test
        internal fun `has updated amount`() {
            val quote = FixedSource(0f, "", "").toFixedTarget(100f)

            assertThat(quote.amount).isEqualTo(100f)
        }

        @Test
        internal fun `has source currency`() {
            val quote = FixedSource(0f, "GBP", "").toFixedTarget(0f)

            assertThat(quote.sourceCurrency).isEqualTo("GBP")
        }

        @Test
        internal fun `has target currency`() {
            val quote = FixedSource(0f, "", "EUR").toFixedTarget(0f)

            assertThat(quote.targetCurrency).isEqualTo("EUR")
        }

        @Test
        internal fun `has no quote id (quote not yet fetched from backend)`() {
            val quote = FixedSource(0f, "", "", "myid").toFixedTarget(0f)

            assertThat(quote.id).isNull()
        }
    }

    @Nested
    @DisplayName("Copy quote with")
    inner class Copy {
        @Test
        internal fun `target currency updates target currency for fixed source`() {
            val quote = FixedSource(0f, "", "").copyWithTargetCurrency("EUR")

            assertThat(quote.targetCurrency).isEqualTo("EUR")
        }

        @Test
        internal fun `target currency updates target currency for fixed target`() {
            val quote = FixedTarget(0f, "", "").copyWithTargetCurrency("EUR")

            assertThat(quote.targetCurrency).isEqualTo("EUR")
        }

        @Test
        internal fun `target currency removes quote id for fixed source`() {
            val quote = FixedSource(0f, "", "", "quoteid").copyWithTargetCurrency("EUR")

            assertThat(quote.id).isNull()
        }

        @Test
        internal fun `target currency removes quote id for fixed target`() {
            val quote = FixedTarget(0f, "", "", "quoteid").copyWithTargetCurrency("EUR")

            assertThat(quote.id).isNull()
        }

        @Test
        internal fun `quote id updates quote id for fixed source`() {
            val quote = FixedSource(0f, "", "", "quoteid").copyWithId("newQuoteId")

            assertThat(quote.id).isEqualTo("newQuoteId")
        }

        @Test
        internal fun `quote id updates quote id for fixed target`() {
            val quote = FixedTarget(0f, "", "", "quoteid").copyWithId("newQuoteId")

            assertThat(quote.id).isEqualTo("newQuoteId")
        }
    }
}
