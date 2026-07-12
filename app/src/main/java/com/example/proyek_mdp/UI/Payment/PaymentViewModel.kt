package com.example.proyek_mdp.UI.Payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.UI.Network.Midtrans.SnapTransactionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val repository: MidtransRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _transaction =
        MutableStateFlow<Result<PaymentResult>?>(null)

    val transaction: StateFlow<Result<PaymentResult>?> =
        _transaction

    fun createTransaction(
        coin: Int,
        price: Long
    ) {

        viewModelScope.launch {

            _loading.value = true

            val result =
                repository.createTransaction(
                    coin,
                    price
                )

            _transaction.value = result

            _loading.value = false

        }

    }

}

class PaymentViewModelFactory(
    private val repository: MidtransRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(repository) as T

        }

        throw IllegalArgumentException("Unknown ViewModel")

    }

}