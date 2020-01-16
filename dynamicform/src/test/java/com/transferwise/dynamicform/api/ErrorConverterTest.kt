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
package com.transferwise.dynamicform.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ErrorConverterTest {

    @Test
    internal fun `empty map when null error message`() {
        val errors = ErrorConverter().convert(null)

        assertThat(errors).isEmpty()
    }

    @Test
    internal fun `error map when error message`() {
        val errors = ErrorConverter().convert(ERROR_JSON)

        assertThat(errors).containsEntry("accountHolderName", "Please enter the recipients first and last name.")
    }

    @Test
    internal fun `empty map when invalid error message`() {
        val errors = ErrorConverter().convert("not a json")

        assertThat(errors).isEmpty()
    }
}

private val ERROR_JSON = """{
    "timestamp": "05-11-2019 01:10:44",
    "status": "UNPROCESSABLE_ENTITY",
    "message": "{
        \"timestamp\":\"2019-11-05T13:10:44.496175Z\",
        \"errors\":[
            {
                \"code\":\"NOT_VALID\",
                \"message\":\"Name needs to be between 2 and 70 characters long, and contain both first and last name.\",
                \"path\":\"accountHolderName\",
                \"arguments\":[\"accountHolderName\",\"A\",\"2\",\"70\"]
            },
            {
                \"code\":\"NOT_VALID\",
                \"message\":\"Please enter the recipients first and last name.\",
                \"path\":\"accountHolderName\",
                \"arguments\":[\"accountHolderName\",\"A\"]
            }
        ]
    }"
}"""
