# International transfer UI flow
>This module can be copied to bootstrap your integration of the TransferWise for Banks API. Note that only the full flow can easily be reused, not the different steps independently.

This module represents the reference implementation of the TransferWise for Banks API and contains all screens to handle an international payment flow.

![Navigation graph](../readme/navigation_graph.png)

The full flow consists of the following steps (from left to right):

1. Check if the customer already has a connected TransferWise account
2. Create an estimated (= anonymous) quote
3. Connect an existing TransferWise account or create a new one
4. Create a final quote, based on customer information
5. Create and select the recipient
6. Provide extra details about the transfer
7. Review and confirm the transfer
8. Payment is done

Note that when step 1 detects that a customer already has a connected TransferWise account, then steps 2 and 3 are skipped.

# Architecture
As this repositories main objective is to show how to integrate the [TransferWise for Banks API](https://transferwise.github.io/api-docs-banks/#transferwise-for-banks-api), the architecture is kept very standard. This has the added advantage of lowering the barrier to understanding the code to non-Android developers.

Main technology choices:

- [Kotlin](https://kotlinlang.org/) as the programming language
- [Android navigation components](https://developer.android.com/guide/navigation/navigation-getting-started) to handle navigation
- [View binding](https://developer.android.com/topic/libraries/view-binding) to access views from XML
- [View models](https://developer.android.com/topic/libraries/architecture/viewmodel) to make Ui logic testable
- [Live data](https://developer.android.com/topic/libraries/architecture/livedata) as a lifecycle-aware observable
- [Retrofit](https://square.github.io/retrofit/) to communicate to the RESTful API
- [Kotlin coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) for multi threading
- [Coil](https://github.com/coil-kt/coil) for image loading


## Android navigation components
The `transferwise` module uses a single `Activity` that displays several `Fragments` using the [Android navigation component](https://developer.android.com/guide/navigation/navigation-getting-started). This simplifies navigation and also allows to create a visual representation of the entire module UI.

Opening the [international transfer graph](https://github.com/transferwise/banksDemo-Android/blob/master/transferwise/src/main/res/navigation/international_transfer_graph.xml) in Android Studio, yields the following:

![Navigation graph](../readme/navigation_graph.png)

Note that some advanced navigation use cases are handled by popping the back stack. As such, navigation from "currency selection" back to "quotes" doesn't actually refresh the previous "quotes" screen, but instead pops the old screen off the back stack and pops a new "quotes" screen on top.

## Ui architecture
### MVVM
Every screen consists out of a `Fragment` (xxxFragment) and `ViewModel` (xxxViewModel). The `ViewModel` contains all business logic for the `Fragment` and exposes a single `LiveData` property called `uiState`. 

The `Fragment` initializes the `ViewModel` and updates the UI by observing changes in the `uiState`. Because this state is modelled as a [sealed class](https://kotlinlang.org/docs/reference/sealed-classes.html), the Kotlin compiler forces the `Fragment` to handle every possible state in its `when` expression. And because the `uiState` is a `LiveData`, observing it is fully lifecycle safe.

When the user interacts with the screen, the `Fragment` passes that interaction down to the `ViewModel`.

All code in the `ViewModels` is tested using unit tests. The `Fragments` doesn't have any tests as they only map the `uiState` to the actual UI. Testing that would require to run tests on an actual Android device/emulator.

### Data flow
There is no singleton or central repository that saves the global state of the payments flow. Instead, `Fragments` get all dynamic information they need passed to them as arguments in an Android `Bundle`. The lack of a global state makes the entire payment flow robust against any lifecycle changes.

There is however global information that is passed into the `transferwise` module when it gets started (e.g. the URL of the backend service). This information is stored into the `Bundle` of the `InternationalTransferActivity` and passed on to the `Fragments` using the `SharedViewModel`.

### Navigation
To ensure different `Fragments` don't know about each other and to avoid coupling them to `InternationalTransferActivity`, all navigation happens through a `SharedViewModel`. This works using a `navigationAction` property where all `ViewModels` can post `NavigationActions` to.

The `InternationalTransferActivity` observes the `navigationAction` of the `SharedViewModel` and handles the resulting navigation. Note that the `SharedViewmodel` is scoped to the lifecycle of the `InternationalTransferActivity`, whereas other `ViewModels` are scoped to the lifecycle of the Fragments.

### Deeplinks
When logging in to a TransferWise account, the user is directed to an external browser and returns using a deep link to the application. 

This deep link is mostly handled by the Android navigation component, with one very important customization: the deep link result is handled directly by the `InternationalTransferActivity` `onNewIntent` method. This ensures that the existing back stack is preserved, whereas the Android navigation component would have cleared the back stack by default.

Another important detail is that the current app state is stored in the `InternationalTransferActivity` so it can be accessed again after the app is resumed. This makes the deep link handling also fully lifecycle safe.

## Coroutines
As the threading mechanism Kotlin Coroutines made the most sense:

- they allow expressing concurrent operations in a sequential manner (without callbacks)
- they are automatically canceled when not needed thanks to structured concurrency

All coroutines are launched in either a `lifecycleScope` or `viewModelScope` which ensures that they will get canceled automatically when the screen is no longer needed.
