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
package com.banks.demo.offline

import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

/*
 * To make it easier to test and learn about the TransferWise for banks integration, the reference implementation also has an
 * offline mode. This is built upon OkHttp's MockWebserver and works by running runs a local clear text webserver inside of the app.
 *
 * Note that this is not the recommended approach to use MockWebServer as it's designed as a testing dependency. But in this example
 * it allows offline testing without having to make any changes to the reference code. That ensures the TransferWise module isn't
 * cluttered with non essential code for the TransferWise for banks integration.
 *
 * ⚠️ This is only part of the repository for demo purposes, but has no value as reference code. ⚠️
 */
internal class MockBanksWebServer {

    private val server = MockWebServer()
    var baseUrl: String

    init {
        server.dispatcher = object : Dispatcher() {

            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                return handleRequest(request)
            }
        }
        server.start()
        baseUrl = server.url("").toString()
    }

    private fun handleRequest(request: RecordedRequest) = when (request.banksPath) {
        "customers" -> successResponse(FAKE_CUSTOMER)
        "user-credentials/sign-up" -> successResponse()
        "user-credentials/existing" -> successResponse()
        "currencies" -> successResponse(FAKE_CURRENCIES)
        "anonymous-quotes" -> successResponse(FAKE_ANONYMOUS_QUOTE)
        "quotes" -> successResponse(FAKE_QUOTE)
        "recipient/requirements" -> successResponse(FAKE_RECIPIENTS_REQUIEMENTS)
        "recipients" -> if (request.method == "GET") successResponse(FAKE_RECIPIENTS) else successResponse()
        "transfers/summary" -> successResponse(FAKE_TRANSFER_SUMMARY)
        "transfers/requirements" -> successResponse(FAKE_TRANSFER_REQUIREMENTS)
        "transfers" -> successResponse()
        else -> MockResponse().setResponseCode(404)
    }

    private fun successResponse(body: String = "") =
        MockResponse().setResponseCode(200).setBodyDelay(800, TimeUnit.MILLISECONDS).setBody(body)

    private val RecordedRequest.banksPath get() = path!!.split("?")[0].substring(1)
}
