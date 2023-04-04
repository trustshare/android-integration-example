package com.example.android_integration_example

data class WebViewArgs(
  val handlerName: String,
  val callback: (message: String) -> Unit
)
