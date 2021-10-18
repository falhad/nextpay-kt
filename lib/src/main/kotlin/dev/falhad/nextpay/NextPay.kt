/*
 * Copyright (c) 2021.
 * Farhad Navayazdan
 * cs.arcxx@gmail.com
 */
package dev.falhad.nextpay

import dev.falhad.nextpay.model.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking


const val URL_GENERATE_TOKEN = "https://nextpay.org/nx/gateway/token"
const val URL_PAYMENT_URL = "https://nextpay.org/nx/gateway/payment"
const val URL_VERIFY_PAYMENT = "https://nextpay.org/nx/gateway/verify"
const val URL_CHECKOUT = "https://nextpay.org/nx/gateway/checkout"


enum class NextPayCurrency {
    IRT,
    IRR
}

class NextPay(private val apiKey: String, logging: Boolean = false) {

    private val client = HttpClient(CIO) {
        expectSuccess = false
        engine {}

        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        if (logging) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }

    }


    /**
     * 1- ایجاد توکن
     *
     * * نکته : با ارسال (( شماره موبایل پرداخت کننده )) ، اگر شخص پرداخت کننده قبلا در درگاه بانک اطلاعات کارت خود را ذخیره کرده باشد ، دیگر نیازی به ورود شماره 16 رقمی کارت و تاریخ انقضا نخواهد داشت
     * اگر پارامتر auto_verify با مقدار yes ارسال شود ، تراکنش بدون تایید از طرف شما ، به طور خودکار تایید میشود . دقت فرمایید که درصورت استفاده از این پارامتر اگر ارتباط شما در هنگام کالبک با نکست پی برقرار نشود ، تراکنش به طور خودکار تایید میشود و ممکن است شما از نتیجه ی آن آگاه نشوید بنابراین این پارامتر را فقط در صورتی استفاده کنید که به هر ترتیب ( حتی در صورت قطعی سرور شما ) میخواهید تراکنش تایید شود.
     * اگر پارامتر allowed_card با مقدار 16 رقمی کارت خاصی مقدار دهی شود ، اگر تراکنش با شماره کارتی غیر از شماره کارتی که شما اعلام میکنید انجام شود ، برگشت میخورد . بنابراین اگر میخواهید تراکنش با هر شماره کارتی پذیرفته شود ، این پارامتر را خالی بگذارید یا مقدار دهی نکنید .
     *
     * پاسخ
     *
     * * نکته : اگر پارامتر code در پاسخ دارای مقدار 1- باشد، یعنی توکن با موفقیت صادر شده است و trans_id همان توکن مورد نیاز برای مراحل بعدی است.
     * نکته : جهت پیگیری تراکنش در مراحل بعدی و جلوگیری از برخی خطرات مانند دابل اسپندینگ ، لازم است در دیتابیس خود ، trans_id مذکور را به همراه مبلغ و شماره سفارش ، ذخیره کنید.
     */
    fun requestToken(
        orderId: String,
        amount: Int,
        callbackURI: String,
        autoVerify: Boolean = true,
        currency: NextPayCurrency = NextPayCurrency.IRT,
        customerPhone: String? = null,
        customJsonFields: String? = null,
        allowedCard: String? = null
    ): TokenResponse = runBlocking {
        val tokenResponse: TokenResponse = client.post(URL_GENERATE_TOKEN) {
            contentType(ContentType.Application.Json)
            body = TokenRequest(
                apiKey = apiKey,
                orderId = orderId,
                amount = amount,
                callbackURI = callbackURI,
                currency = currency.name,
                customerPhone = customerPhone,
                customJsonFields = customJsonFields,
                autoVerify = autoVerify,
                allowedCard = allowedCard
            )
        }
        tokenResponse
    }


    private fun gVerifyPayment(
        transId: String,
        amount: Int,
        refund: Boolean = false
    ): VerifyResponse = runBlocking {
        val verifyResponse: VerifyResponse = client.post(URL_VERIFY_PAYMENT) {
            contentType(ContentType.Application.Json)
            body = VerifyRequest(
                apiKey = apiKey,
                amount = amount,
                transId = transId,
            ).apply {
                refundRequest = when {
                    refund -> "yes_money_back"
                    else -> null
                }
            }
        }
        verifyResponse
    }

    /**
     * پس از دریافت 3 پارامتر موجود در مرحله قبل، برای تایید تراکنش و اطلاع از صحت آن باید وضعیت تراکنش را از وب سرویس نکست پی استعلام بگیرید تا متوجه شوید که تراکنش موفق بوده یا ناموفق . اگر این مرحله را طی مدت 10 دقیقه انجام ندهید ، حتی درصورتی که پرداخت کننده مبلغ را با موفقیت پرداخت کرده باشد ، مبلغ به حساب پرداخت کننده برگشت میخورد زیرا شما تراکنش را تایید نکرده اید. بنابراین برای تایید تراکنش و همچنین استعلام جزییات آن کافیست پارامتر های درج شده در پایین را با متد POST به اندپوینت زیر ارسال کنید. به اندپوینت درج شده ارسال گردد .
     * * نکته : اگر پارامتر code در پاسخ دارای مقدار 0 باشد، یعنی تراکنش (( موفق )) بوده است . هر مقداری غیر از صفر به معنی ناموفق بودن تراکنش است.
     */
    fun verifyPayment(
        transId: String,
        amount: Int,
    ): VerifyResponse = gVerifyPayment(transId, amount, false)


    /**
     *  عودت و لغو یک تراکنش موفق
     *ممکن است به هر دلیلی شما بخواهید یک تراکنش موفق را (( عودت )) دهید و آنرا لغو کنید تا به حساب پرداخت کننده برگردد . دقت فرمایید که برای عودت یک تراکنش موفق ، باید پارامتر های زیر را از طریق متد POST به اندپوینت زیر ارسال کنید.
     * * نکته : اگر پارامتر code در پاسخ دارای مقدار 90- باشد، یعنی تراکنش (( برگشت خورده )) و لغو است . هر مقداری غیر از 90- به معنی کنسل نشدن تراکنش است.
     */
    fun rejectPayment(
        transId: String,
        amount: Int,
    ): VerifyResponse = gVerifyPayment(transId, amount, true)

    /**
     * وب سرویس تسویه حساب ، برداشت وجه و تسهیم
     *  اگر مقدار code برابر با 200 باشد ، یعنی عملیات با موفقیت انجام شده و درخواست شما ثبت شده است. در غیر اینصورت درخواست شامل خطا بوده است .
     */
    private fun checkout(
        wid: Int, auth: String, amount: Int, sheba: String, name: String
    ): CheckoutResponse = runBlocking {
        val checkoutResponse: CheckoutResponse = client.post(URL_CHECKOUT) {
            contentType(ContentType.Application.Json)
            body = CheckoutRequest(
                wid = wid, auth = auth, amount = amount, sheba = sheba, name = name
            )
        }
        checkoutResponse
    }


    /**
     * @return error message by returned code by api
     */
    fun codeMessage(code: Int): String? {
        return nextPayCodes[code]
    }

}