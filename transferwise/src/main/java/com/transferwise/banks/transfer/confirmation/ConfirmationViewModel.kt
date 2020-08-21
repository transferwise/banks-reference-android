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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transferwise.banks.api.BanksWebService
import com.transferwise.banks.api.data.TransferSummary
import com.transferwise.banks.api.request.TransferRequest
import com.transferwise.banks.shared.NavigationAction
import com.transferwise.banks.shared.SharedViewModel
import com.transferwise.banks.shared.TransferDetails
import com.transferwise.banks.transfer.confirmation.ConfirmationUiState.SaveFailed
import com.transferwise.banks.transfer.confirmation.ConfirmationUiState.Saving
import com.transferwise.banks.transfer.confirmation.ConfirmationUiState.Summary
import com.transferwise.banks.transfer.confirmation.ConfirmationUiState.SummaryWithReference
import com.transferwise.banks.util.DefaultRecipientHelper
import com.transferwise.banks.util.RecipientHelper
import com.transferwise.banks.util.TextProvider
import com.transferwise.banks.util.decimalsOrRound
import kotlinx.coroutines.launch

internal class ConfirmationViewModel(
    private val webService: BanksWebService,
    private val sharedViewModel: SharedViewModel,
    private val customerId: Int,
    private val summary: TransferSummary,
    private var details: TransferDetails,
    private val textProvider: TextProvider,
    private val helper: RecipientHelper = DefaultRecipientHelper()
) : ViewModel() {

    val uiState = MutableLiveData<ConfirmationUiState>()
    private val formattedSummary: FormattedSummary

    init {
        formattedSummary = summary.toFormattedSummary()
        uiState.value = Summary(formattedSummary, details.hasReference)
    }

    fun doTransfer() = viewModelScope.launch {
        uiState.value = Saving(formattedSummary, details.hasReference)
        try {
            webService.createTransfer(customerId, TransferRequest(summary.quoteId, summary.recipientId, details.asMap()))
            sharedViewModel.navigationAction.setValue(summary.toNavigationAction())
        } catch (e: Exception) {
            uiState.value = SaveFailed(formattedSummary, details.hasReference, textProvider.createTransferError())
        }
    }

    fun showReference() {
        uiState.value = SummaryWithReference(formattedSummary, details.reference, details.validationErrors)
    }

    fun hideReference() {
        uiState.value = Summary(formattedSummary, details.hasReference)
    }

    fun changeReference(reference: String) {
        details = details.copyWithReference(reference)
        uiState.value = SummaryWithReference(formattedSummary, details.reference, details.validationErrors)
    }

    private fun TransferSummary.toFormattedSummary() =
        FormattedSummary(
            sourceAmount.decimalsOrRound(2),
            sourceCurrency,
            targetAmount.decimalsOrRound(2),
            targetCurrency,
            helper.initials(recipientName),
            recipientName,
            accountSummary,
            formattedEstimatedDelivery,
            textProvider.transferCosts(fee, sourceCurrency, rate),
            helper.color(recipientName)
        )

    private fun TransferSummary.toNavigationAction() = NavigationAction.ToPaymentSent(sourceAmount, sourceCurrency, recipientName)

    private val TransferDetails.validationErrors
        get() =
            if (reference.length < referenceMinLength) textProvider.minimumLengthError(summary.transferReferenceValidation.minLength!!)
            else if (reference.length > referenceMaxLength) textProvider.maximumLengthError(summary.transferReferenceValidation.maxLength!!)
            else if (!reference.isValidReference()) textProvider.validationFailedError("transfer reference")
            else ""

    private fun TransferDetails.validInput() = if (summary.transferReferenceValidation.validationRegexp.isNullOrEmpty()) true else
        reference.matches(summary.transferReferenceValidation.validationRegexp.toRegex())

    private val referenceMinLength get() = summary.transferReferenceValidation.minLength ?: 0
    private val referenceMaxLength get() = summary.transferReferenceValidation.maxLength ?: Int.MAX_VALUE
    private val referenceRegex get() = summary.transferReferenceValidation.validationRegexp ?: ""

    private fun String.isValidReference() =
        referenceRegex.isEmpty() || this.matches(referenceRegex.toRegex())
}
