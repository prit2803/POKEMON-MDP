package com.example.proyek_mdp.UI.History

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.database.PaymentHistory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val items = ArrayList<PaymentHistory>()

    fun submitList(data: List<PaymentHistory>) {

        items.clear()
        items.addAll(data)
        notifyDataSetChanged()

    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val tvMethod: TextView = v.findViewById(R.id.tvMethod)
        val tvCoin: TextView = v.findViewById(R.id.tvCoin)
        val tvPrice: TextView = v.findViewById(R.id.tvPrice)
        val tvDate: TextView = v.findViewById(R.id.tvDate)
        val tvStatus: TextView = v.findViewById(R.id.tvStatus)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)

        return ViewHolder(view)

    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items[position]

        holder.tvMethod.text = item.paymentMethod

        holder.tvCoin.text = "${item.coinAmount} Coin"

        holder.tvPrice.text =
            NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                .format(item.totalPrice)

        holder.tvStatus.text = item.status

        holder.tvDate.text =
            SimpleDateFormat(
                "dd MMM yyyy HH:mm",
                Locale("id")
            ).format(Date(item.transactionDate))

    }

}