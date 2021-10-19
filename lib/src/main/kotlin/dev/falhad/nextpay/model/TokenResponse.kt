package dev.falhad.nextpay.model

import dev.falhad.nextpay.URL_PAYMENT_URL
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param code مقدار 1- باشد، یعنی توکن با موفقیت صادر شده است
 * @param transId توکن مورد نیاز برای مراحل بعدی است.
 */
@Serializable
data class TokenResponse(
    @SerialName("code") val code: Int,
    @SerialName("trans_id") val transId: String
) {

    /**
     * نشان دهنده این است که توکن تراکنش با موفقیت ساخته شده است.
     */
    fun tokenGenerated(): Boolean = code == -1

    /**
     * لینک هدایت کاربر به صفحه بانک
     */
    fun paymentURL(): String = "$URL_PAYMENT_URL/${this.transId}"

}