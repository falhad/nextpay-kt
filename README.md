<h1 align="center">Welcome to nextpay-kt 👋</h1>
<p>
  <img alt="Version" src="https://img.shields.io/badge/version-1.0.2-blue.svg?cacheSeconds=2592000" />
  <a href="https://github.com/falhad/nextpay-kt/wiki" target="_blank">
    <img alt="Documentation" src="https://img.shields.io/badge/documentation-yes-brightgreen.svg" />
  </a>
  <a href="https://opensource.org/licenses/MIT" target="_blank">
    <img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-yellow.svg" />
  </a>

  <a href="https://twitter.com/iarcxxi" target="_blank">
    <img alt="Twitter: iarcxxi" src="https://img.shields.io/twitter/follow/iarcxxi.svg?style=social" />
  </a>
</p>

> Connect to  <a href="https://nextpay.ir" target="_blank">NextPay.ir</a> payment gateway in easy way.

### 🏠 [Homepage](https://github.com/falhad/nextpay-kt)

## Setup

### Kotlin KTS

1- Add `mavenCentral()` to your `repositories` section of build.gradle.kts

```kotlin
repositories {
    mavenCentral()
}
```

2- Add dependency to your project.

```kotlin
dependencies {
    implementation("dev.falhad:nextpay:1.0.2")
}
```

### Groovy

1- Add `maven()` to your `repositories` section of build.gradle.kts

```groovy
repositories {
    maven()
}
```

2- Add dependency to your project.

```groovy
dependencies {
    implementation 'dev.falhad:nextpay:1.0.1'
}
```

## 🚀 Basic Usage

If you are not familiar with the transaction processing flow its good to
see [Nextpay.ir documents](https://nextpay.org/nx/docs) first.

### Initialize Nextpay-KT

```kotlin
val nextPay = NextPay(apiKey = NEXTPAY_API_KEY, logging = false)
```

### Step 1 - Generate Token

```kotlin
nextPay
    .requestToken("your-order-id", 1000, "your-website-callback-api")
    .fold(onSuccess = { response ->
        println("hooray! transaction (${response.transId} generated.")
        println("redirect user to ${response.paymentURL()}")
    }, onFailure = { error ->
        when (error) {
            is NextPayException -> println("${error.msg} | ${error.code}")
            else -> println("something went wrong. ${error.message}")
        }
    })
```

You may want to save `transId`, `orderId` and `amount` for
prevent [Double-spending](https://en.wikipedia.org/wiki/Double-spending) problem.

* you also can modify `autoVerify`, `currency`, `customerPhone`, `allowedCard` and `customJsonFields` if you needed.
* If you set `autoVerify = true` the transaction will be auto verify, and you don't need to verify the transaction
  manually.

### Step 2 - Verify/Reject Payment

If you set autoVerify to `false` in step 1 you should verify the transaction within 10 minutes or transaction will be
reverted automatically.

#### Verify Payment

```kotlin
nextPay.verifyPayment("tranId", 1000)
    .fold({ response ->
        println("payment verified.\n$response")
    }, { error ->
        when (error) {
            is NextPayException -> println("${error.msg} | ${error.code}")
            else -> println("something went wrong. ${error.message}")
        }
    })
```

#### Reject Payment

```kotlin
    nextPay.rejectPayment("tranId", 1000)
    .fold({ response ->
        println("payment rejected.\n$response")
    }, { error ->
        when (error) {
            is NextPayException -> println("${error.msg} | ${error.code}")
            else -> println("something went wrong. ${error.message}")
        }
    })
```

### Checkout / Other Apis

> See [Nextpay-KT Wiki](https://github.com/falhad/nextpay-kt/wiki) for docs and more examples.

## Author

👤 **Farhad Navayazdan**

* Website: https://falhad.dev
* Twitter: [@iarcxxi](https://twitter.com/iarcxxi)
* Github: [@falhad](https://github.com/falhad)
* LinkedIn: [@farhadarcxx](https://linkedin.com/in/farhadarcxx)

## Show your support

Give a ⭐️ if this project helped you!

My Public Address to Receive `USDT (TRC20)` is `TQ43UdYtFAHQCNVciNCd9ndE4TQaMMzboX`

## 📝 License

Copyright © 2021 [Farhad Navayazdan](https://github.com/falhad).

This project is [MIT](https://opensource.org/licenses/MIT) licensed.

***
_This README was generated with ❤️ by [readme-md-generator](https://github.com/kefranabg/readme-md-generator)_