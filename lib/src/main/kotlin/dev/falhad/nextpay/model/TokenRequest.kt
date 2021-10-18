package dev.falhad.nextpay.model

import kotlinx.serialization.*

/**
 * @param  apiKey    کلید مجوز دهی
 * @param orderId    شماره سفارش
 * @param amount    مبلغ (پیش فرض تومان)
 * @param callbackURI    آدرس بازگشت
 * @param currency    واحد پولی		IRT یا IRR
 * @param customerPhone    موبایل پرداخت کننده	09121234567
 * @param customJsonFields    اطلاعات دلخواه	json	اختیاری	{ "productName":"Shoes752" , "id":52 }
 * @param autoVerify    تایید خودکار بدون نیاز به فراخوانی وریفای
 * @param allowedCard    شماره کارت مجاز
 */
@Serializable
data class TokenRequest(
    @SerialName("api_key") var apiKey: String,
    @SerialName("order_id") var orderId: String,
    @SerialName("amount") var amount: Int,
    @SerialName("callback_uri") var callbackURI: String,
    @SerialName("currency") var currency: String? = null,
    @SerialName("customer_phone") var customerPhone: String? = null,
    @SerialName("custom_json_fields") var customJsonFields: String? = null,
    @SerialName("auto_verify") var autoVerify: Boolean? = null,
    @SerialName("allowed_card") var allowedCard: String? = null,
)