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
package com.banks.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.banks.demo.databinding.ActivityMainBinding
import com.banks.demo.offline.MockBanksWebServer
import com.banks.demo.settings.Preferences
import com.banks.demo.settings.SettingsActivity
import com.banks.demo.transfer.CreateTransferDialogFragment
import com.banks.demo.transfer.DemoTransferAdapter
import com.transferwise.banks.BackendConfiguration
import com.transferwise.banks.InternationalTransferActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Demo Activity that mimics a bank application and integrates the TransferWise for banks reference implementation.
 *
 *  ⚠️ This is only part of the repository for demo purposes, but has no value as reference code. ⚠️
 */
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val preferences by lazy { Preferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(preferences.getTheme())
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.transfers.adapter = DemoTransferAdapter()
        binding.transfers.layoutManager = LinearLayoutManager(this)

        binding.internationalTransfer.setOnClickListener {
            supportFragmentManager.beginTransaction().add(CreateTransferDialogFragment(), "").commit()
        }
        binding.settings.setOnClickListener {
            finish()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    internal fun showInternationalTransfer() {
        lifecycleScope.launch {
            val intent = InternationalTransferActivity.createIntent(
                this@MainActivity,
                backendConfiguration(),
                preferences.getCustomerId(),
                preferences.getSourceCurrency(),
                preferences.getTargetCurrency(),
                preferences.getQuoteAmount(),
                preferences.getTheme()
            )
            startActivity(intent)
        }
    }

    private suspend fun backendConfiguration(): BackendConfiguration {
        val backendUrl = if (preferences.getDemoMode()) {
            withContext(Dispatchers.IO) { MockBanksWebServer() }.baseUrl
        } else {
            preferences.getBackendUrl()
        }

        return BackendConfiguration(
            backendUrl,
            BuildConfig.LOGIN_SERVER_URL,
            BuildConfig.LOGIN_CLIENT_ID
        )
    }
}
