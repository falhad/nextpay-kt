package dev.falhad.nextpay.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param code برابر با 200 باشد ، یعنی عملیات با موفقیت انجام شده
 */
@Serializable
data class CheckoutResponse(
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String
) {

    /**
     *      *  اگر مقدار code برابر با 200 باشد ، یعنی عملیات با موفقیت انجام شده و درخواست شما ثبت شده است. در غیر اینصورت درخواست شامل خطا بوده است .
     */
    fun ok(): Boolean = code == 200
}