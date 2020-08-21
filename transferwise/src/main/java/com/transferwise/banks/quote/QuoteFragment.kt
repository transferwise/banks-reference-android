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

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import coil.api.load
import com.transferwise.banks.R
import com.transferwise.banks.databinding.FragmentQuoteBinding
import com.transferwise.banks.quote.QuoteUiState.Available
import com.transferwise.banks.quote.QuoteUiState.Failed
import com.transferwise.banks.quote.QuoteUiState.Fetching
import com.transferwise.banks.util.isFloating
import com.transferwise.banks.util.limitDecimalsTo
import com.transferwise.banks.util.sharedViewModel
import com.transferwise.banks.util.toast
import com.transferwise.dynamicform.view.SingleTextWatcherEditText

/**
 * Base screen to show Quotes, there is a subclass [AnonymousQuoteFragment] that shows anonymous quotes.
 *
 * Please refer to the design guide for more information about each screen:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
internal open class QuoteFragment : Fragment() {

    private lateinit var binding: FragmentQuoteBinding
    private val viewModel by viewModels<QuoteViewModel> { getFactory() }

    private val args: QuoteFragmentArgs by navArgs()
    protected open val isAnonymous = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentQuoteBinding.inflate(inflater)

        viewModel.uiState.observe(viewLifecycleOwner, Observer(::refreshUi))
        binding.sourceAmount.doAfterEdit { viewModel.doOnNewQuote(it.toFloatOrNull()) }
        binding.targetAmount.doAfterEdit { viewModel.doOnNewFixedTargetQuote(it.toFloatOrNull()) }

        binding.buttonContinue.setOnClickListener { viewModel.doOnContinue() }
        binding.targetCurrency.setOnClickListener { viewModel.doOnSelectCurrency() }
        binding.sourceAmount.limitDecimalsTo(2)
        binding.targetAmount.limitDecimalsTo(2)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.doOnStart()
    }

    private fun refreshUi(state: QuoteUiState): Unit = when (state) {
        is Available -> {
            applyStateToAllViews(state)
            binding.buttonContinue.isEnabled = true
        }
        is Failed -> {
            applyStateToAllViews(state)
            binding.buttonContinue.isEnabled = false
            toast(state.errorMessage)
        }
        is Fetching -> {
            applyStateToAllViews(state)
            binding.buttonContinue.isEnabled = false
        }
    }

    private fun applyStateToAllViews(state: QuoteUiState) {
        binding.sourceAmount.setTextSafe(state.sourceAmount)
        binding.sourceCurrency.text = state.sourceCurrency
        binding.sourceFlag.load(state.sourceFlagUri)
        binding.targetAmount.setTextSafe(state.targetAmount)
        binding.targetCurrency.text = state.targetCurrency
        binding.targetFlag.load(state.targetFlagUri)
        binding.fee.text = state.fee
        binding.rate.text = state.rate
        binding.arrivalTime.text = if (state.arrivalTime.isNotEmpty()) state.arrivalTime.boldTimeHighlight() else ""

        if(state.rateType.isFloating()) {
            binding.notice.text = state.notice
            binding.notice.isVisible = true
            binding.noticeBackground.isVisible = true
        }
    }

    private fun String.boldTimeHighlight(): SpannableStringBuilder {
        val start = resources.getText(R.string.quote_arrival_time).indexOf('%')
        val spannable = SpannableStringBuilder(this)
        spannable.setSpan(StyleSpan(Typeface.BOLD), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun getFactory() = QuoteViewModelFactory(sharedViewModel, resources, args.customerId, args.quote, isAnonymous)
}

/**
 * Updates the text in an [EditText] while making sure:
 *
 * - The current cursor position is kept (e.g. don't update when has focus)
 * - No text changed callback is invoked (to avoid recursive calls on the [QuoteViewModel])
 * - The cursor is removed after setting the text for the first time to avoid the cursor being at the beginning of the text
 */
private fun SingleTextWatcherEditText.setTextSafe(text: String) {
    if (!this.hasFocus()) {
        this.setTextWithNoTextChangeCallback(text)
    } else if (this.text.isNullOrEmpty()) {
        // Required for when navigating back to screen from recipients and FixedTarget quote, then the source amount has focus
        this.setTextWithNoTextChangeCallback(text)
    } else if (text.isNotEmpty()) {
        this.doAfterFocusChange { this.setTextWithNoTextChangeCallback(text) }
    }
}
