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
const val URL_REJECT_PAYMENT = "https://nextpay.org/nx/gateway/verify"
const val URL_CHECKOUT = "https://nextpay.org/nx/gateway/checkout"

/**
 * @param IRT Toman
 * @param IRR Rial
 */
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
                encodeDefaults = true
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
     *
     * 1- ایجاد توکن
     *
     * @param orderId    شماره سفارش
     * @param amount    مبلغ (پیش فرض تومان)
     * @param callbackURI    آدرس بازگشت
     * @param currency    واحد پولی		IRT یا IRR
     * @param customerPhone    موبایل پرداخت کننده	09121234567
     * @param customJsonFields    اطلاعات دلخواه	json	اختیاری	{ "productName":"Shoes752" , "id":52 }
     * @param autoVerify    تایید خودکار بدون نیاز به فراخوانی وریفای - اگر پارامتر با مقدار yes ارسال شود ، تراکنش بدون تایید از طرف شما ، به طور خودکار تایید میشود
     * @param allowedCard    شماره کارت مجاز - اگر پارامتر با مقدار 16 رقمی کارت خاصی مقدار دهی شود ، اگر تراکنش با شماره کارتی غیر از شماره کارتی که شما اعلام میکنید انجام شود ، برگشت میخورد
     *
     * جهت پیگیری تراکنش در مراحل بعدی و جلوگیری از برخی خطرات مانند دابل اسپندینگ ، لازم است در دیتابیس خود ، trans_id مذکور را به همراه مبلغ و شماره سفارش ، ذخیره کنید.
     */
    fun requestToken(
        orderId: String,
        amount: Int,
        callbackURI: String,
        autoVerify: Boolean = false,
        currency: NextPayCurrency = NextPayCurrency.IRT,
        customerPhone: String? = null,
        customJsonFields: String? = null,
        allowedCard: String? = null
    ): Result<TokenResponse> = runBlocking {
        try {
            val response: TokenResponse = client.post(URL_GENERATE_TOKEN) {
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
            if (response.tokenGenerated()) {
                Result.success(response)
            } else {
                Result.failure(
                    NextPayException(
                        code = response.code,
                        msg = codeMessage(code = response.code)
                    )
                )
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }


    /**
     *
     * استعلام و تایید تراکنش
     *
     *
     * برای تایید تراکنش و اطلاع از صحت آن باید وضعیت تراکنش را از وب سرویس نکست پی استعلام بگیرید تا متوجه شوید که تراکنش موفق بوده یا ناموفق
     *  اگر این مرحله را طی مدت 10 دقیقه انجام ندهید ، حتی درصورتی که پرداخت کننده مبلغ را با موفقیت پرداخت کرده باشد ، مبلغ به حساب پرداخت کننده برگشت میخورد
     *  هر مقداری غیر از صفر به معنی ناموفق بودن تراکنش است.
     *
     */
    fun verifyPayment(
        transId: String,
        amount: Int,
    ): Result<VerifyResponse> = runBlocking {
        try {
            val response: VerifyResponse = client.post(URL_VERIFY_PAYMENT) {
                contentType(ContentType.Application.Json)
                body = VerifyRequest(
                    apiKey = apiKey,
                    amount = amount,
                    transId = transId,
                )
            }
            if (response.verified()) {
                Result.success(response)
            } else {
                Result.failure(
                    NextPayException(
                        code = response.code,
                        msg = codeMessage(code = response.code)
                    )
                )
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }


    /**
     *  عودت و لغو یک تراکنش موفق
     *
     *ممکن است به هر دلیلی شما بخواهید یک تراکنش موفق را (( عودت )) دهید و آنرا لغو کنید تا به حساب پرداخت کننده برگردد . دقت فرمایید که برای عودت یک تراکنش موفق ، باید پارامتر های زیر را از طریق متد POST به اندپوینت زیر ارسال کنید.
     * * نکته : اگر پارامتر code در پاسخ دارای مقدار 90- باشد، یعنی تراکنش (( برگشت خورده )) و لغو است . هر مقداری غیر از 90- به معنی کنسل نشدن تراکنش است.
     */
    fun rejectPayment(
        transId: String,
        amount: Int,
    ): Result<RejectResponse> = runBlocking {
        try {
            val response: RejectResponse = client.post(URL_REJECT_PAYMENT) {
                contentType(ContentType.Application.Json)
                body = RejectRequest(
                    apiKey = apiKey,
                    amount = amount,
                    transId = transId,
                    refundRequest = "yes_money_back"
                )
            }
            if (response.rejected()) {
                Result.success(response)
            } else {
                Result.failure(
                    NextPayException(
                        code = response.code,
                        msg = codeMessage(code = response.code)
                    )
                )
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    /**
     * وب سرویس تسویه حساب ، برداشت وجه و تسهیم
     *  اگر مقدار code برابر با 200 باشد ، یعنی عملیات با موفقیت انجام شده و درخواست شما ثبت شده است. در غیر اینصورت درخواست شامل خطا بوده است .
     */
    fun checkout(
        wid: Int, auth: String, amount: Int, sheba: String, name: String
    ): Result<CheckoutResponse> = runBlocking {
        try {
            val response: CheckoutResponse = client.post(URL_CHECKOUT) {
                contentType(ContentType.Application.Json)
                body = CheckoutRequest(
                    wid = wid, auth = auth, amount = amount, sheba = sheba, name = name
                )
            }
            if (response.ok()) {
                Result.success(response)
            } else {
                Result.failure(
                    NextPayException(
                        code = response.code,
                        msg = codeMessage(code = response.code)
                    )
                )
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }


    /**
     *
     * @return error message by returned code by api
     */
    fun codeMessage(code: Int): String = nextPayCodes.getOrDefault(code, "خطایی پیش آمد.")


    /**
     * @return error message by returned code by api
     */
    fun Int.message() = codeMessage(this)

}

data class NextPayException(val code: Int, val msg: String) : Throwable(message = msg)