package com.example.android_integration_example

import android.content.Context
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.WindowManager
import android.webkit.*
import android.webkit.WebView.WebViewTransport
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URL


suspend fun fetchClientSecret(): String {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://10.0.2.2:9987/createPaymentIntent")
        .build()

    val response = withContext(Dispatchers.IO) {
        client.newCall(request).execute()
    }

    if (response.isSuccessful) {
        val json = JSONObject(response.body?.string())
        val clientSecret = json.getString("client_secret")
        return clientSecret
    } else {
        // Handle error
        throw Exception("Error fetching client secret")
    }
}

class MainActivity : AppCompatActivity(), LifecycleOwner {
    var clientSecret = "your_client_secret_will_be_here"
    private var handlerName = "trustshareHandler"
    private var webView: WebView? = null
    private var webViewPopup: WebView? = null
    private var mContext: Context? = null
    private var builder: AlertDialog? = null
    private val callback = fun(message: String) {
        Log.d("trustshare.message", message)
        return
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {

        // The arguments needed to integrate with the trustshare web sdk.
        super.onCreate(savedInstanceState)
        mContext = this

        Log.d("start", clientSecret)
        coroutineScope.launch {
            Log.d("Launch", clientSecret)
            try {
                Log.d("TRY", clientSecret)
                clientSecret = fetchClientSecret()
                // The client secret has been fetched successfully.
                // print the client secret here
                Log.d("client_secret", clientSecret)
                val webviewArgs = WebViewArgs("trustshareAndroid", callback)
                createPrimaryWebView(webviewArgs)
                setContentView(webView)
                // The client secret has been fetched successfully.
                // You can now show the WebView.
            } catch (e: Exception) {
                // There was an error fetching the client secret.
                // You can show an error message or retry the request.
            }
        }
    }


    private fun createPrimaryWebView(args: WebViewArgs) {
        webView = WebView(this)
        webView?.settings?.userAgentString =
            webView?.settings?.userAgentString + " trustshare-sdk/android/1.0"
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView!!.webViewClient = UriWebViewClient()
        webView!!.webChromeClient = UriChromeClient()
        webView?.settings?.setSupportMultipleWindows(true)
        webView?.addJavascriptInterface(JSBridge(args.callback), args.handlerName)
        val url = makeURL()
        // print the url here
        Log.d("url", url)
        webView?.loadUrl(url)
    }

    fun makeURL(): String {
        val builder = Uri.Builder()
        builder.scheme("https").authority("checkout.trustshare.io").path("/process")
            .appendQueryParameter("s", clientSecret).appendQueryParameter("handler", handlerName)
        val encodedQuery = builder.build().encodedQuery?.replace("+", "%2B")
        val urlString = "https://checkout.trustshare.io/process?$encodedQuery"
        return URL(urlString).toString()
    }

    // This is needed to deal with the open banking flow popup flow.
    // TODO: Check if only needed for local dev only. May be able to be removed.
    private class UriWebViewClient : WebViewClient() {
        override fun onReceivedSslError(
            view: WebView, handler: SslErrorHandler, error: SslError
        ) {
            Log.d("onReceivedSslError", "onReceivedSslError")
        }
    }

    inner class UriChromeClient : WebChromeClient() {
        override fun onCreateWindow(
            view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message
        ): Boolean {
            webViewPopup = mContext?.let { WebView(it) }
            webViewPopup?.isVerticalScrollBarEnabled = false
            webViewPopup?.isHorizontalScrollBarEnabled = false
            webViewPopup?.webViewClient = UriWebViewClient()
            webViewPopup?.webChromeClient = UriChromeClient()
            webViewPopup?.settings?.javaScriptEnabled = true

            builder =
                mContext?.let { AlertDialog.Builder(it, AlertDialog.BUTTON_POSITIVE).create() }
            builder!!.setView(webViewPopup)
            builder!!.show()
            builder!!.window!!.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
            )

            val transport = resultMsg.obj as WebViewTransport
            transport.webView = webViewPopup
            resultMsg.sendToTarget()
            return true
        }

        // When then window is closed, destroy the popup view and builder.
        override fun onCloseWindow(window: WebView) {
            webViewPopup?.destroy()
            builder?.dismiss()
        }
    }

    inner class JSBridge(val cb: (message: String) -> Unit) {
        @JavascriptInterface
        // This function must be called postMessage.
        // This is the function trustshare will call from the JS interface.
        // The call back passed in is the callback defined at the entry point for the web view.
        fun postMessage(message: String) {
            cb(message)
            if (message == "trustshareCloseWebView") {
                destroyWebView()
            }
        }

        private fun destroyWebView() {
            // Destroy web view and return back to app containing web view.
            Log.d("trustshare.message", "Destroy function called.")
        }
    }
}
