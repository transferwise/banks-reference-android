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
package com.banks.demo.settings

import android.content.Context
import androidx.core.content.edit
import com.banks.demo.BuildConfig
import com.banks.demo.R

/*
* ⚠️ This is only part of the repository for demo purposes, but has no value as reference code. ⚠️
*/
internal class Preferences(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("com.banks.demo.preference", Context.MODE_PRIVATE)

    fun setCustomerId(customerId: Int) = sharedPreferences.edit { putInt("customerId", customerId) }
    fun getCustomerId() = sharedPreferences.getInt("customerId", DEFAULT_CUSTOMER_ID)

    fun setBackendUrl(backendUrl: String) = sharedPreferences.edit { putString("backendUrl", backendUrl) }
    fun getBackendUrl() = sharedPreferences.getString("backendUrl", DEFAULT_BACKEND_URL)!!

    fun setSourceCurrency(currency: String) = sharedPreferences.edit { putString("sourceCurrency", currency) }
    fun getSourceCurrency() = sharedPreferences.getString("sourceCurrency", DEFAULT_SOURCE_CURRENCY)!!

    fun setTargetCurrency(currency: String) = sharedPreferences.edit { putString("targetCurrency", currency) }
    fun getTargetCurrency() = sharedPreferences.getString("targetCurrency", DEFAULT_TARGET_CURRENCY)!!

    fun setQuoteAmount(amount: Int) = sharedPreferences.edit { putInt("quoteAmount", amount) }
    fun getQuoteAmount() = sharedPreferences.getInt("quoteAmount", DEFAULT_QUOTE_AMOUNT)

    fun setDemoMode(enabled: Boolean) = sharedPreferences.edit { putBoolean("demo_mode", enabled) }
    fun getDemoMode() = sharedPreferences.getBoolean("demo_mode", false)

    fun setTheme(theme: Int) = sharedPreferences.edit { putInt("theme", theme) }
    fun getTheme() = sharedPreferences.getInt("theme", R.style.BanksTheme)

    companion object {
        const val DEFAULT_CUSTOMER_ID = 44
        const val DEFAULT_BACKEND_URL = BuildConfig.DEFAULT_SERVER_URL
        const val DEFAULT_SOURCE_CURRENCY = "GBP"
        const val DEFAULT_TARGET_CURRENCY = "EUR"
        const val DEFAULT_QUOTE_AMOUNT = 1000
    }
}
