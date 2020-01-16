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

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.banks.demo.BuildConfig
import com.banks.demo.MainActivity
import com.banks.demo.R
import com.banks.demo.databinding.ActivitySettingsBinding

/**
 * Offers dynamic configuration to make it easier to demo the reference integration to potential customers.
 *
 * ⚠️ This is only part of the repository for demo purposes, but has no value as reference code. ⚠️
 */
class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding
    private val preferences by lazy { Preferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(preferences.getTheme())

        binding = ActivitySettingsBinding.inflate(layoutInflater)

        binding.customerId.setText("${preferences.getCustomerId()}")
        binding.customerId.doAfterTextChanged { preferences.setCustomerId(it.toString().toIntOrNull() ?: 0) }

        binding.demoMode.isChecked = preferences.getDemoMode()
        binding.demoMode.setOnCheckedChangeListener { _, isChecked -> preferences.setDemoMode(isChecked) }

        binding.sourceCurrency.setText(preferences.getSourceCurrency())
        binding.sourceCurrency.doAfterTextChanged { preferences.setSourceCurrency(it.toString()) }

        binding.targetCurrency.setText(preferences.getTargetCurrency())
        binding.targetCurrency.doAfterTextChanged { preferences.setTargetCurrency(it.toString()) }

        binding.quoteAmount.setText("${preferences.getQuoteAmount()}")
        binding.quoteAmount.doAfterTextChanged { preferences.setQuoteAmount(it.toString().toIntOrNull() ?: 0) }

        binding.backendUrl.setText(preferences.getBackendUrl())
        binding.backendUrl.doAfterTextChanged { preferences.setBackendUrl(it.toString()) }

        val adapter = ArrayAdapter(
            binding.root.context,
            com.transferwise.dynamicform.R.layout.item_select_popup,
            options.keys.toList()
        )
        binding.theme.setAdapter(adapter)
        binding.theme.doOnItemSelected {
            preferences.setTheme(options.get(options.keys.toList()[it])!!)
            finish()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.theme.setText(options.filter { it.value == preferences.getTheme() }.keys.firstOrNull(), false)

        binding.appVersion.text = String.format("App version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    internal fun AutoCompleteTextView.doOnItemSelected(listener: (position: Int) -> Unit) {
        onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ -> listener.invoke(position) }
    }

    companion object {
        private val options =
            mapOf(
                "Default" to R.style.BanksTheme,
                "Red" to R.style.BanksThemeRed,
                "Green" to R.style.BanksThemeGreen
            )
    }
}
