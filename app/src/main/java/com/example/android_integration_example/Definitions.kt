package com.example.android_integration_example

enum class Currency(val currencyVal: String) {
  GBP("gbp"),
  USD("usd"), // For usd payments please contact support (support@trustshare.co)
  EUR("eur")
}

data class CheckoutArgs(
    var to: String?,
    val from: String?,
    val amount: String?,
    val currency: Currency?,
    val depositAmount: String?,
    val description: String?,
)

data class DisputeArgs(
  val token: String
)

data class TopupArgs(
  var token: String,
  var amount: String?
)

data class ReturnArgs(
  var token: String,
  var amount: String?
)

data class ReleaseArgs(
  var token: String,
  var amount: String?
)

val checkoutAction = CheckoutArgs(
  "simon+seller@trustshare.co",
  "simon+buyer@trustshare.co",
  "23000",
  Currency.GBP,
  "",
  "here is a description from args",
)

val topupAction = TopupArgs(
  "D9SyFaZThQ2mdJnK",
  "10000"
)

val releaseAction = ReleaseArgs(
  "D9SyFaZThQ2mdJnK",
  "100"
)

val returnAction = ReturnArgs(
  "D9SyFaZThQ2mdJnK",
  "100"
)

val disputeAction = DisputeArgs(
  "D9SyFaZThQ2mdJnK"
)

data class WebViewArgs<T>(
  val action: T,
  val subdomain: String,
  val handlerName: String,
  val callback: (message: String) -> Unit
)
