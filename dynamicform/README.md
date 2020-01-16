# Dynamic forms
> This module can reused to render dynamic forms returned by the TransferWise for Banks API. 

> Note that while all code is fully tested and has a simple, generic API, the interface needs more polish to distribute it as a Maven artifact (e.g. thread safety, review contracts,...).

<center><a href="../readme/dynamic_forms_example.gif" target="blank"><img src="../readme/dynamic_forms_example.gif" width="260"></a></center>

## About
Some endpoints in the TransferWise API return a dynamic UI that needs to be rendered to the user.

These endpoints are:

- [Recipient account requirements](https://transferwise.github.io/api-docs-banks/#recipient-accounts-requirements), used to get the necessary fields to create a new recipient.
- [Transfer requirements](https://transferwise.github.io/api-docs-banks/#transfers-requirements), needed to get the necessary fields to complete a transfer.  

Rendering such a dynamic UI is quite challenging and therefore, the reference implementation contains a separate module dynamicforms that handles all that complexity for you.

## Advantages
When using the `dynamicforms` module, the following problems are solved for you:

- Shows several sections as separate UI tabs
- Renders text input fields with local error validations (min/max length, regex,... )
- Renders selection inputs with refreshes of the form (e.g. choosing the USA as the country adds a "state" input field to the form)
- Summarizes the current form state into either InComplete, ValidationError or Complete
- Allows to add static components on top of the dynamic ones
- Processes the form output to collections that can be sent back to the backend

## Key classes
The main classes involved in displaying a dynamic form UI are:

- `DynamicFormView`: can be included in XML to render a dynamic form
- `DynamicFormController`: main interface with the dynamic form component. Allows to show the form, exposes the current `DynamicFormState` and allows to get the processed form input.
- `DynamicFormsWebservice`: tells the `DynamicFormController` how to load and refresh the form for the current endpoint
- `DynamicFormState`: indicates whether the form is `Loading`, `InComplete`, `ValidationError` or `Complete`
- `StaticFormGenerator`: allows displaying static content on top of the dynamic content

## Getting started 
In order to integrate a dynamic form into a new screen:

1. Include a `DynamicFormView` into your `layout.xml`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >

    <com.transferwise.dynamicform.view.DynamicFormView
        android:id="@+id/dynamic_form"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        />

</androidx.constraintlayout.widget.ConstraintLayout>
```
2. Implement a `DynamicFormsWebService` and implement `getForm()`and `refreshForm()`.

```kotlin
interface DynamicFormsWebService {
    suspend fun getForm(): List<DynamicSection>
    suspend fun refreshForm(attributes: Map<String, String?>, details: Map<String, Any?>): List<DynamicSection>
}
```

3. Get the `DynamicFormController` from the `DynamicFormView` and call `showForm()` on it. As the `DynamicFromController` actually does network requests in the background, it needs a `CoroutineScope` to properly clean up its resources. (typically the [viewModelScope](https://developer.android.com/topic/libraries/architecture/coroutines#viewmodelscope))

```kotlin
val dynamicForm = view.findViewById<DynamicFormView>(R.id.dynamic_form)
        dynamicForm.controller.showForm(scope = myCoroutineScope, dynamicWebService = MyDynamicFormsWebService())
```

4. Subscribe to the `DynamicFormController` `uiState` to get updates about the status of the form. Notice how the subscription needs to be done within a `CoroutineScope` to avoid leaking any subscriptions. (typically the [viewModelScope](https://developer.android.com/topic/libraries/architecture/coroutines#viewmodelscope))

```kotlin
myCoroutineScope.launch { 
    dynamicFormController.uiState.consumeEach { 
        dynamicFormState -> when (dynamicFormState) {
            is Loading -> TODO()
            is DynamicFormState.Incomplete -> TODO()
            is DynamicFormState.ValidationError -> TODO()
            is Complete -> TODO()
        }   
    }
}
```

5. Get the `currentAttributes()` and `currentDetails()` from the `DynamicFormController` to process the form input.

```kotlin
val attributes: Map<String, String?> = dynamicFormController.currentAttributes()
val details: Map<String, Any> = dynamicFormController.currentDetails()
```

6. Specify theme attributes for the dynamic form elements in your `styles.xml` to ensure they form looks as expected.

```xml
<style name="MyTheme" parent="Theme.MaterialComponents.Light">
    ...
    <item name="dynamicFormText">@style/MyDynamicFormText</item>
    <item name="dynamicFormSelect">@style/MyDynamicFormSelect</item>
    <item name="dynamicFormTab">@style/MyDynamicFormTab</item>
</style>
```

```xml
<!-- Use these styles to get the default dynamic form style -->

<style name="MyDynamicFormText" parent="Widget.MaterialComponents.TextInputLayout.OutlinedBox" />

<style name="MyDynamicFormSelect" parent="Widget.MaterialComponents.TextInputLayout.OutlinedBox.ExposedDropdownMenu" />

<style name="MyDynamicFormTab" parent="Widget.MaterialComponents.TabLayout" />
```

7. Optionally you can provide a StaticFormGenerator which will prepend static elements to the dynamic form (e.g. name and email field)

```kotlin
interface StaticFormGenerator {
    fun generate(
        userInput: Map<UniqueKey, String>,
        serverErrors: Map<UniqueKey, String>,
        showRequired: Boolean
    ): List<UserInput>
}
```