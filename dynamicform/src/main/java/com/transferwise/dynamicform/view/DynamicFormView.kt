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
package com.transferwise.dynamicform.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.transferwise.dynamicform.DynamicFormController
import com.transferwise.dynamicform.DynamicFormState
import com.transferwise.dynamicform.databinding.ViewDynamicFormBinding
import com.transferwise.dynamicform.generator.UserInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

/**
 * The [DynamicFormView] is designed to be directly included into the layout.xml of the screen
 * where you wish to display a dynamic form. It won't render anything untill you call [showForm]
 * on the [DynamicFormController]
 *
 * Under the hood this view combines two things:
 * - a [RecyclerView] to display the dynamic form elements, represented as [UserInput]
 * - a [ProgressBar] to indicate when the dynamic form is loading
 *
 * To know what state to show, it subscribes itself to the [DynamicFormController], which exposes
 * the [DynamicFormState] of the form. This observer pattern decouples the [DynamicFormView] from
 * the [DynamicFormController] allowing the latter to be used in any business logic without risking
 * to leak the view.
 *
 * This is also why the [DynamicFormView] creates the [DynamicFormController] internally and exposes
 * it to the owner of the view. It's an intentionally different pattern than the [RecyclerView] -
 * [RecyclerViewAdapter] relation, making it clear it's a different kind of relation.
 * (a [RecyclerViewAdapter] does hold a reference to the [RecyclerView], a [DynamicFormController] doesn't)
 */
class DynamicFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    val controller: DynamicFormController =
        DynamicFormController(context)

    private val adapter = InputAdapter(::onUserInput)
    private val binding: ViewDynamicFormBinding
    private lateinit var refreshUiJob: Job

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewDynamicFormBinding.inflate(inflater, this, true)

        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        refreshUiJob = GlobalScope.launch(Dispatchers.Main) {
            controller.uiState.consumeEach { refreshUi(it) }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        refreshUiJob.cancel()
    }

    override fun clearFocus() {
        super.clearFocus()
        binding.recyclerview.children.forEach { it.clearFocus() }
    }

    private fun refreshUi(it: DynamicFormState) {
        when (it) {
            is DynamicFormState.Complete, is DynamicFormState.Incomplete, is DynamicFormState.ValidationError -> {
                binding.recyclerview.animate().alpha(1f)
                binding.loading.isVisible = false
            }
            is DynamicFormState.Loading -> {
                binding.recyclerview.alpha = 0.5f
                binding.loading.isVisible = true
            }
        }
        adapter.submitList(it.items)
    }

    private fun onUserInput(item: UserInput, input: String) {
        if (item is UserInput.Tabs) {
            clearFocus()
        }
        controller.doOnUserInput(item, input)
    }
}
