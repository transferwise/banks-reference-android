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
package com.transferwise.banks.transfer.confirmation

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.transferwise.banks.R
import com.transferwise.banks.databinding.FragmentConfirmationBinding
import com.transferwise.banks.transfer.confirmation.ConfirmationUiState.Summary
import com.transferwise.banks.util.sharedViewModel
import com.transferwise.banks.util.toast
import com.transferwise.dynamicform.view.SingleTextWatcherEditText

/**
 * Please refer to the design guide for more information about each screen:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
internal class ConfirmationFragment : Fragment() {

    private val viewModel by viewModels<ConfirmationViewModel> { getFactory() }

    private lateinit var binding: FragmentConfirmationBinding
    private val args: ConfirmationFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentConfirmationBinding.inflate(inflater, container, false)

        viewModel.uiState.observe(viewLifecycleOwner, Observer(::updateUi))
        binding.buttonSend.setOnClickListener { viewModel.doTransfer() }
        binding.referenceButton.setOnClickListener { viewModel.showReference() }
        binding.referenceLayout.setEndIconOnClickListener { viewModel.hideReference() }
        binding.reference.doAfterEdit { viewModel.changeReference(it) }

        return binding.root
    }

    private fun updateUi(uiState: ConfirmationUiState) = when (uiState) {
        is Summary -> {
            showNotSaving()
            applyStateToAllViews(uiState)
            hideReference(uiState.hasReference)
            binding.buttonSend.isEnabled = true
        }
        is ConfirmationUiState.SummaryWithReference -> {
            showNotSaving()
            applyStateToAllViews(uiState)
            showReference(uiState.reference, uiState.referenceError)
            binding.buttonSend.isEnabled = false
        }
        is ConfirmationUiState.SaveFailed -> {
            showNotSaving()
            applyStateToAllViews(uiState)
            hideReference(uiState.hasReference)
            toast(R.string.confirmation_failed)
        }
        is ConfirmationUiState.Saving -> {
            showSaving()
            applyStateToAllViews(uiState)
            hideReference(uiState.hasReference)
        }
    }

    private fun showReference(reference: String, referenceError: String) {
        binding.referenceButton.isVisible = false
        binding.referenceLayout.isVisible = true
        binding.reference.setTextSafe(reference)
        binding.referenceLayout.error = referenceError
    }

    private fun hideReference(hasReference: Boolean) {
        binding.referenceButton.isVisible = true
        binding.referenceLayout.isVisible = false
        binding.referenceButton.setText(if (hasReference) R.string.confirmation_edit_reference else R.string.confirmation_add_reference)
    }

    private fun showSaving() {
        binding.changeSummaryAlpha(0.5f)
        binding.buttonSend.isEnabled = false
        binding.saving.isVisible = true
    }

    private fun showNotSaving() {
        binding.changeSummaryAlpha(1f)
        binding.buttonSend.isEnabled = true
        binding.saving.isVisible = false
    }

    private fun applyStateToAllViews(uiState: ConfirmationUiState) {
        binding.sourceAmount.text = uiState.summary.sourceAmount
        binding.sourceCurrency.text = uiState.summary.sourceCurrency
        binding.targetAmount.text = uiState.summary.targetAmount
        binding.targetCurrency.text = uiState.summary.targetCurrency
        binding.recipientInitial.text = uiState.summary.recipientInitials
        binding.recipientInitial.backgroundTintList = ColorStateList.valueOf(uiState.summary.recipientColor)
        binding.recipientName.text = uiState.summary.recipientName
        binding.recipientAccount.text = uiState.summary.recipientAccount
        binding.arrivalTime.text = uiState.summary.arrivalTime
        binding.costs.text = uiState.summary.costs
    }

    private fun FragmentConfirmationBinding.changeSummaryAlpha(alpha: Float) {
        listOf(
            saving, sourceAmount, sourceCurrency, targetAmount, targetCurrency, recipientInitial,
            recipientName, recipientAccount, arrivalTime, costs, arrow, arrivalTitle
        ).forEach { it.animate().alpha(alpha) }
    }

    private fun getFactory() =
        ConfirmationViewModelFactory(sharedViewModel, args.customerId, args.transferSummary, args.extraDetails, resources)
}

private fun SingleTextWatcherEditText.setTextSafe(reference: String) {
    if (!hasFocus()) {
        this.setTextWithNoTextChangeCallback(reference)
    }
}
