package dev.falhad.nextpay.model

import kotlinx.serialization.*

/**
 * @param wid    شماره وب سرویس شما
 * @param auth    کد محرمانه وب سرویس
 * @param amount    مبلغ (تومان)
 * @param sheba    شماره شبا - بدون IR
 * @param name    نام صاحب حساب
 */
@Serializable
data class CheckoutRequest(
    @SerialName("wid") var wid: Int,
    @SerialName("auth") var auth: String,
    @SerialName("amount") var amount: Int,
    @SerialName("sheba") var sheba: String,
    @SerialName("name") var name: String,
)