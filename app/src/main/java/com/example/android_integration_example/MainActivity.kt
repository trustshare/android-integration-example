package com.example.android_integration_example

import android.content.Context
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.webkit.WebView.WebViewTransport
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.example.trustshare_android_integration.R
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
    val request = Request.Builder().url("http://10.0.2.2:9987/createPaymentIntent").build()

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
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mainLayout: ConstraintLayout
    private var webView: WebView? = null
    private var webViewPopup: WebView? = null
    private var mContext: Context? = null
    private var builder: AlertDialog? = null
    private lateinit var jsonText: TextView
    private var button: Button? = null

    private val callback = fun(message: String) {
        Log.d("trustshare.message", message)
        val json = JSONObject(message)
        val type = json.getString("type")
        Log.d("trustshare.type", type)
        if (type == "complete") {
            mainLayout.post {
                webView?.visibility = WebView.GONE
            }
            handler.post {
                // Hide the create intent button
                button?.visibility = View.GONE
                // Destroy the web view
                webView?.destroy()
                webView = null
                // Show the JSON in the text view
                setContentView(R.layout.activity_main)
                jsonText = findViewById(R.id.json_text)
                jsonText.setText(message)
            }
        }
        return
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_main)
        mainLayout = findViewById(R.id.main_layout) // Add this line
        jsonText = findViewById(R.id.json_text) // Add this line
        button = findViewById<Button>(R.id.button)
        button?.setOnClickListener {
            coroutineScope.launch {
                try {
                    clientSecret = fetchClientSecret()
                    val webviewArgs = WebViewArgs(handlerName, callback)
                    createPrimaryWebView(webviewArgs)
                    setContentView(webView)
                } catch (e: Exception) {
                    Log.e("error_fetching_secret", e.message.toString())
                }
            }
        }
    }

    private fun createPrimaryWebView(args: WebViewArgs) {
        val url = makeURL()
        runOnUiThread {
            webView = WebView(this)
            webView?.settings?.userAgentString =
                webView?.settings?.userAgentString + " trustshare-sdk/android/1.0"
            webView?.settings?.javaScriptEnabled = true
            webView?.settings?.domStorageEnabled = true
            webView?.webViewClient = UriWebViewClient()
            webView?.webChromeClient = UriChromeClient()
            webView?.settings?.setSupportMultipleWindows(true)
            webView?.addJavascriptInterface(JSBridge(args.callback), args.handlerName)
            setContentView(webView)
            webView?.loadUrl(url)
        }
    }

    fun makeURL(): String {
        val builder = Uri.Builder()
        builder.scheme("https").authority("checkout.trustshare.io").path("/process")
            .appendQueryParameter("s", clientSecret).appendQueryParameter("handler", handlerName)
        val encodedQuery = builder.build().encodedQuery?.replace("+", "%2B")
        val urlString = "https://checkout.trustshare.io/process?$encodedQuery"
        return URL(urlString).toString()
    }

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
