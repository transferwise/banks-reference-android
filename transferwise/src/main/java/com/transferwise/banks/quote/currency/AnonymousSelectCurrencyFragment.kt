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
package com.transferwise.banks.quote.currency

/**
 * While both the select currency and anonymous select currency are identical in screen and logic, they do
 * navigate back to a different screen.
 *
 * Because the selected currency has to be passed back to the quote screen, the back navigation actually
 * removes the existing [QuoteFragment] and creates a new [QuoteFragment] on top. That makes it easy to
 * pass around the target currency, but requires currency selection to navigate to different screens.
 *
 * Because of the navigation differences, the navigation graph requires two different classes to define the
 * different [NavigationDirection] and hence a it made most sense to make the anonymous currency selection a
 * subclass of the regular currency selection.
 *
 * Thanks to the [QuoteNavigationFactory], both screens can use the same [QuoteViewModel], yet still
 * navigate to a different screen. The [QuoteNavigationFactory] is injected into the [QuoteViewModel] by
 * the [QuoteViewModelFactory] based on the value of [isAnonymous].
 *
 * This is an extra screen that isn't explicitly in the design guide:
 * https://www.notion.so/Bank-Integrations-Design-Guide-8c375c5c5f1e4c64953b4b601ff6abc6
 */
internal class AnonymousSelectCurrencyFragment : SelectCurrencyFragment() {
    override val isAnonymous: Boolean = true
}
