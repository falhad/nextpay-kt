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
    fun ok(): Boolean {
        return code == 200
    }
}