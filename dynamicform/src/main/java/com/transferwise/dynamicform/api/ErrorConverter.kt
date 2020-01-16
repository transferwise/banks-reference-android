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

import com.squareup.moshi.Moshi

/**
 * Parses a raw error response from the backend into a [Map] of errors.
 */
class ErrorConverter {

    fun convert(errorMessage: String?): Map<String, String> = try {
        val moshi = Moshi.Builder().build()
        val error = moshi.adapter(ErrorResponse::class.java).fromJson(errorMessage)
        val errors = moshi.adapter(ErrorMessage::class.java).fromJson(error!!.message)
        errors!!.errors!!.map { it.path to it.message }.toMap()
    } catch (e: Exception) {
        emptyMap()
    }
}
