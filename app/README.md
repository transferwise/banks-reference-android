# Demo banks application
> This module doesn't contain any reference code and shouldn't be reused.

A very basic module that mimics a bank application and integrates the TransferWise for banks reference implementation. This is only part of the repository for demo purposes, but has no value as reference code.

<img src="../readme/main_activity_1.png" width="260">&emsp;<img src="../readme/main_activity_2.png" width="260">

## Mock banks WebServer
In order to be able to test out (and demo) the TransferWise for banks reference implementation without spinning up an instance of [the reference backend](https://github.com/transferwise/t4b-backend), the app module offers an offline mode built upon a `MockBanksWebServer`.

This mock webserver is based on [OkHttp](https://github.com/square/okhttp)'s [MockWebserver](https://github.com/square/okhttp/tree/master/mockwebserver) and runs a local clear text webserver inside of the app. 

Note that it's not recommended to include `MockWebServer` as a non testing dependency as it's developed as a testing tool. However in this example, it allows offline testing without having to make any changes to the reference code. This ensures the `transferwise` module only contains essential code for the TransferWise for banks integration.
