package dev.falhad.nextpay.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * @param code    کد وضعیت تراکنش
 * @param amount    مبلغ (تومان)
 * @param orderId    شماره سفارش
 * @param cardHolder    کارت پرداخت کننده
 * @param customerPhone    موبایل پرداخت کننده
 * @param shaparakRefId    کد پیگیری شاپرک
 * @param customJsonFields    اطلاعات دلخواه	json	{ "productName":"Shoes752" , "id":52 }
 */
@Serializable
data class VerifyResponse(
    @SerialName("code") val code: Int,
    @SerialName("amount") val amount: Int,
    @SerialName("order_id") var orderId: String,
    @SerialName("card_holder") var cardHolder: String? = null,
    @SerialName("customer_phone") var customerPhone: String? = null,
    @SerialName("custom") var customJsonFields: String? = null,
    @SerialName("Shaparak_Ref_Id") var shaparakRefId: String,
) {
    /**
     * تراکنش با موفقیت برگشت خورده است
     * این متد برای لغو و عودت تراکنش کاربرد دارد.
     */
    fun rejected(): Boolean {
        return code == -90
    }

    /**
     * تراکنش با موفقیت پرداخت و تایید شده است.
     * این متد برای تایید تراکنش کاربرد دارد.
     */
    fun verified(): Boolean {
        return code == 0
    }
}