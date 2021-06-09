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


class MainActivity : AppCompatActivity() {
  private var webView: WebView? = null
  private var webViewPopup: WebView? = null
  private var mContext: Context? = null
  private var builder: AlertDialog? = null
  private val callback = fun(message: String) {
    Log.d("trustshare.message", message)
    return
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    // The arguments needed to integrate with the trustshare web sdk.
    val webviewArgs = WebViewArgs(
      checkoutAction,
      "demo",
      "trustshareAndroid",
      callback
    )

    super.onCreate(savedInstanceState)
    createPrimaryWebView(webviewArgs)
    setContentView(webView)
    mContext = this
  }


  private fun <T> createPrimaryWebView(args: WebViewArgs<T>) {
    webView = WebView(this)
    webView?.settings?.userAgentString =
      webView?.settings?.userAgentString + " trustshare-sdk/android/1.0"
    webView?.settings?.javaScriptEnabled = true
    webView?.settings?.domStorageEnabled = true
    webView!!.webViewClient = UriWebViewClient()
    webView!!.webChromeClient = UriChromeClient()
    webView?.settings?.setSupportMultipleWindows(true)
    webView?.addJavascriptInterface(JSBridge(args.callback), args.handlerName)
    val url = makeURL(args)
    webView?.loadUrl(url)
  }

  private fun <T> makeURL(args: WebViewArgs<T>): String {
    val builder = Uri.Builder()
    val subdomain = args.subdomain
    val action = args.action

    builder.scheme("https")
      .authority("${subdomain}.nope.sh")
      .appendPath("mobile-sdk")
      .appendQueryParameter("subdomain", subdomain)
      .appendQueryParameter("handlerName", args.handlerName)

    if (action is CheckoutArgs) {
      builder.appendQueryParameter("type", "checkout")
      if (action.to != null) {
        builder.appendQueryParameter("to", action.to)
      }
      if (action.currency != null) {
        builder.appendQueryParameter("currency", action.currency.currencyVal)
      }
      if (action.amount != null) {
        builder.appendQueryParameter("amount", action.amount)
      }
      if (action.description != null) {
        builder.appendQueryParameter("description", action.description)
      }
    }

    if (action is DisputeArgs) {
      builder.appendQueryParameter("type", "dispute")
      builder.appendQueryParameter("token", action.token)
    }

    if (action is TopupArgs) {
      builder.appendQueryParameter("type", "topup")
      builder.appendQueryParameter("token", action.token)
      if (action.amount != null) {
        builder.appendQueryParameter("amount", action.amount)
      }
    }

    if (action is ReturnArgs) {
      builder.appendQueryParameter("type", "return")
      builder.appendQueryParameter("token", action.token)
      if (action.amount != null) {
        builder.appendQueryParameter("amount", action.amount)
      }
    }

    if (action is ReleaseArgs) {
      builder.appendQueryParameter("type", "release")
      builder.appendQueryParameter("token", action.token)
      if (action.amount != null) {
        builder.appendQueryParameter("amount", action.amount)
      }
    }

    val url = builder.build().toString()
    return url
  }

  // This is needed to deal with the open banking flow popup flow.
  // TODO: Check if only needed for local dev only. May be able to be removed.
  private class UriWebViewClient : WebViewClient() {
    override fun onReceivedSslError(
      view: WebView, handler: SslErrorHandler,
      error: SslError
    ) {
      Log.d("onReceivedSslError", "onReceivedSslError")
    }
  }

  inner class UriChromeClient : WebChromeClient() {
    override fun onCreateWindow(
      view: WebView?, isDialog: Boolean,
      isUserGesture: Boolean, resultMsg: Message
    ): Boolean {
      webViewPopup = mContext?.let { WebView(it) }
      webViewPopup?.isVerticalScrollBarEnabled = false
      webViewPopup?.isHorizontalScrollBarEnabled = false
      webViewPopup?.webViewClient = UriWebViewClient()
      webViewPopup?.webChromeClient = UriChromeClient()
      webViewPopup?.settings?.javaScriptEnabled = true

      builder =
        AlertDialog.Builder(this@MainActivity, AlertDialog.BUTTON_POSITIVE).create()
      builder!!.setView(webViewPopup)
      builder!!.show()
      builder!!.window!!.clearFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
          or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
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
