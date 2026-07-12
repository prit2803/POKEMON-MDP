package com.example.proyek_mdp.UI.Payment

import com.example.proyek_mdp.UI.Network.Midtrans.Callbacks
import com.example.proyek_mdp.UI.Network.Midtrans.CustomerDetails
import com.example.proyek_mdp.UI.Network.Midtrans.ItemDetail
import com.example.proyek_mdp.UI.Network.Midtrans.MidtransClient
import com.example.proyek_mdp.UI.Network.Midtrans.SnapTransactionRequest
import com.example.proyek_mdp.UI.Network.Midtrans.SnapTransactionResponse
import com.example.proyek_mdp.UI.Network.Midtrans.TransactionDetails
import java.util.UUID

class MidtransRepository {

    suspend fun createTransaction(
        coin: Int,
        price: Long
    ): Result<PaymentResult> {

        return try {

            val orderId = UUID.randomUUID().toString()

            val request = SnapTransactionRequest(

                transaction_details = TransactionDetails(
                    order_id = orderId,
                    gross_amount = price
                ),

                item_details = listOf(

                    ItemDetail(
                        id = "coin_$coin",
                        price = price,
                        quantity = 1,
                        name = "$coin Coin"
                    )

                ),

                customer_details = CustomerDetails(
                    first_name = "Pokemon User"
                ),

                callbacks = Callbacks()

            )

            val response =
                MidtransClient.snapApi.createTransaction(request)

            if (response.isSuccessful && response.body() != null) {

                Result.success(

                    PaymentResult(

                        orderId = orderId,

                        redirectUrl = response.body()!!.redirect_url,

                        snapToken = response.body()!!.token

                    )

                )

            } else {

                Result.failure(
                    Exception("Gagal membuat transaksi")
                )

            }

        } catch (e: Exception) {

            Result.failure(e)

        }

    }

}