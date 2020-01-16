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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TransferDetailsTest {

    @Test
    internal fun `should get details as map`() {
        val detailsMap = TransferDetails(mapOf("key" to "value")).asMap()

        assertThat(detailsMap).isEqualTo(mapOf("key" to "value"))
    }

    @Test
    internal fun `should get reference`() {
        val reference = TransferDetails(mapOf("reference" to "my transfer reference")).reference

        assertThat(reference).isEqualTo("my transfer reference")
    }

    @Test
    internal fun `reference is empty when no reference`() {
        val reference = TransferDetails(mapOf("detail" to "value")).reference

        assertThat(reference).isEmpty()
    }

    @Test
    internal fun `has reference`() {
        val hasReference = TransferDetails(mapOf("reference" to "my transfer reference")).hasReference

        assertThat(hasReference).isTrue()
    }

    @Test
    internal fun `has no reference when no reference`() {
        val hasReference = TransferDetails(mapOf("detail" to "value")).hasReference

        assertThat(hasReference).isFalse()
    }

    @Test
    internal fun `has no reference when reference empty`() {
        val hasReference = TransferDetails(mapOf("reference" to "")).hasReference

        assertThat(hasReference).isFalse()
    }

    @Test
    internal fun `copy updates reference`() {
        val details = TransferDetails(mapOf("reference" to "reference one")).copyWithReference("reference two")

        assertThat(details.reference).isEqualTo("reference two")
    }
}
