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
package com.transferwise.banks.quote

import android.content.res.Resources
import com.transferwise.banks.R
import com.transferwise.banks.util.LocalizedTextProvider

internal class AnonymousQuoteLocalizedTextProvider(val resources: Resources) : LocalizedTextProvider(resources) {
    override fun quoteRate(rate: Float) = resources.getString(R.string.quote_rate, rate)
}
