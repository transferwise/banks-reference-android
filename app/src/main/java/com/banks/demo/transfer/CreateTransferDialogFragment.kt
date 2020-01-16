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
package com.banks.demo.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.banks.demo.MainActivity
import com.banks.demo.databinding.FragmentCreateTransferBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/*
 *  ⚠️ This is only part of the repository for demo purposes, but has no value as reference code. ⚠️
 */
internal class CreateTransferDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentCreateTransferBinding.inflate(inflater, container, false)

        binding.national.setOnClickListener {
            Toast.makeText(context, "Not part of demo application", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        binding.international.setOnClickListener {
            (activity as MainActivity).showInternationalTransfer()
            dismiss()
        }

        return binding.root
    }
}
