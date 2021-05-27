# android-integration-example
An example showing how to integrate trustshare using android.

## How it works
This example shows how an android application can be integrated with the trustshare web sdk.

The app uses a `WebView` to load a page specifically developed for mobile integration. 

Please refer to the [web sdk documentation](https://docs.trustshare.co/sdk/web-sdk) for further information and definitions for the various actions that can be carried out.

### The WebView
The webview loads a route at `https://${YOUR_SUBDOMAIN}.trustshare.co/mobile-sdk`.

The url requires a query parameter of at least `type` and `handlerName`.

The `type` parameter tells us what sort of `Action` you would like to do and the `handlerName` tells us where to post the state updates to.

The resulting url should look similar to this: 

`https://demo.trustshare.co/mobile-sdk?type=checkout&handlerName=myHandlerName`.

### Actions

The `mobile-sdk` route can handle a variety of [Actions](/android-integration-example/app/src/main/java/com/example/android_integration_example/Definitions.kt#L9-L35) which can be implemented. The enum of `Actions` is below. 

```
Checkout
Topup
Dispute
Return
Release
```

Each action has its own required parameters defined in their respective data classes. See [Definitions](/android-integration-example/app/src/main/java/com/example/android_integration_example/Definitions.kt#L9-L35) for types.

### State updates
The example android app receives state updates from the webview using the [JSBridge](/android-integration-example/app/src/main/java/com/example/android_integration_example/MainActivity#L161-L171) class. 
This class requires a function called postMessage which is used by trustshare to send messages back to the android app. The messages passed will be JSON which is documented in the [web sdk documentation](https://docs.trustshare.co/sdk/web-sdk). 

### Query strings
The webview should use a query string to communicate to the webview which [Action](/android-integration-example/app/src/main/java/com/example/android_integration_example/Definitions.kt#L67) is intended to carry out. 

### Custom user agent
The webview needs to set a custom user agent containing the string `"trustshare-sdk/android"`, otherwise the page will not load.

This can be done with the following code: 

```kotlin
    webView?.settings?.userAgentString =
      webView?.settings?.userAgentString + " trustshare-sdk/android/1.0"
```

## Help and support

Please feel free to reach out to us on slack or [contact support](mailto:support@trustshare.co) if you need further guidance on integration with iOS, and we'll do our best to help.

## Improvements
We are always looking for feedback, so we can provide the best possible developer experience.
If you have any comments, suggestions, questions or feature requests, please [let us know](mailto:engineers@trustshare.co).
