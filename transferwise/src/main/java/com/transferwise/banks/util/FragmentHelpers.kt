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
package com.transferwise.banks.util

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.transferwise.banks.InternationalTransferActivity

internal val Fragment.sharedViewModel get() = (activity!! as InternationalTransferActivity).viewModel

internal fun Fragment.toast(@StringRes message: Int) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
internal fun Fragment.toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
