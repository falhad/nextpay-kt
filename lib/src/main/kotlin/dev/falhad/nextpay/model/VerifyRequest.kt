package dev.falhad.nextpay.model

import kotlinx.serialization.*

/**
 *  @param apiKey    کلید مجوز دهی
 *  @param transId    توکن تراکنش
 *  @param amount    مبلغ (تومان)
 */

@Serializable
data class VerifyRequest(
    @SerialName("api_key") var apiKey: String,
    @SerialName("amount") var amount: Int,
    @SerialName("trans_id") var transId: String,
)