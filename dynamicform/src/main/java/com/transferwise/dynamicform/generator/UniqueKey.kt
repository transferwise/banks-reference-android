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
package com.transferwise.dynamicform.generator

inline class UniqueKey constructor(val value: String) {
    val parentKey get() = value.split(DELIMITER).first()
    val childKey get() = if (value.contains(DELIMITER)) value.split(DELIMITER).last() else null
    val isTopLevelKey get() = (childKey == null)

    companion object {
        fun create(parentKey: String, childKey: String = "") =
            if (childKey.isEmpty()) UniqueKey("$parentKey") else UniqueKey(
                "$parentKey$DELIMITER$childKey"
            )

        private const val DELIMITER = "#"
    }
}
