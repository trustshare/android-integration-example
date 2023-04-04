# android-integration-example

An example showing how to integrate trustshare using Android.

## How it works
This example shows how an Android application can be integrated with the trustshare web sdk.

The app uses a `WebView` to load a page specifically developed for mobile integration.

Please refer to the [web sdk documentation](https://docs.trustshare.io/guides/sdks/web-sdk) for further information and definitions for the various actions that can be carried out.

### Getting started

Clone the repo and cd into the server directory.

Create a `.env` file and add the following:

```bash
TRUSTSHARE_PRIVATE_API_KEY=your-private-key
TRUSTSHARE_PUBLIC_API_KEY=your-public-key
```
Then run the following commands:
```bash
yarn # Install dependencies
yarn start
```

Open up the project in Android Studio and click build. It will open up an emulator with the example app running.

The example app
When the app launches, it will generate a create a payment intent by requesting a client_secret from the example server. The client secret is then used to load the webview with the url https://checkout.trustshare.io.

### The WebView
The webview loads a route at https://checkout.trustshare.io, with your generated client secret on the end as a query parameter.

The resulting url should look similar to this:

https://checkout.trustshare.io/process?s=CLIENT_SECRET

### State updates
The example app receives state updates from the webview using the WebViewClient class.
If the message name is the same as the provided handler, which defaults to "trustshareHandler", the message is passed on to the provided callback.

From here, we can use a Gson library to deserialize the json messages and pass them into objects.

### Closing the webview
When the webview is closed, a handler will be called to let the Android app know. If the checkout has been successful, there will be a project_id and a checkout_id in the message.

### Help and support
Please feel free to reach out to us on slack or contact support if you need further guidance on integration with Android, and we'll do our best to help.

### Improvements
We are always looking for feedback, so we can provide the best possible developer experience.
If you have any comments, suggestions, questions or feature requests, please let us know.


