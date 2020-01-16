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

/**
 * While both the quote and anonymous quote are very similar in screen and logic, they do call a different
 * [BanksWebService] endpoint and they navigate to a different screen.
 *
 * Because of the navigation differences, the navigation graph requires two different classes to define the
 * different [NavigationDirection] and hence a it made most sense to make the anonymous quote a subclass
 * of the regular quotes.
 *
 * Thanks to the [QuoteWebService] and [QuoteNavigationFactory], these screens can use the same [QuoteViewModel],
 * yet still talk to a different endpoint and navigate to a different screen. Both of these are injected into
 * the [QuoteViewModel] by the [QuoteViewModelFactory] based on the value of [isAnonymous].
 *
 * Please refer to the design guide for more information about each screen:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
internal class AnonymousQuoteFragment : QuoteFragment() {
    override val isAnonymous: Boolean = true
}
