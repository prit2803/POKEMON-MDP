package com.example.proyek_mdp.UI.Payment

data class PaymentResult(

    val orderId: String,

    val redirectUrl: String,

    val snapToken: String

)