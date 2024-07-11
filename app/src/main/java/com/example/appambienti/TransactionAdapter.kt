package com.example.appambienti

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appambienti.data.Transactions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class TransactionAdapter(private var transactions: List<Transactions>) :RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {
    class TransactionHolder(view: View): RecyclerView.ViewHolder(view){
        val label :TextView = view.findViewById<TextView>(R.id.label)
        val amount :TextView = view.findViewById<TextView>(R.id.amount)
        val category: TextView = view.findViewById<TextView>(R.id.category)
        val date: TextView = view.findViewById<TextView>(R.id.date)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transazioni_layout,parent,false)
        return TransactionHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.amount.context

        if(transaction.amount >= 0){
            holder.amount.text = "+ %.2f €".format(transaction.amount)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green))
        }else{
            holder.amount.text = "- %.2f €".format(Math.abs(transaction.amount))
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red))
        }

        holder.label.text = transaction.label
        holder.category.text = transaction.category

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(transaction.date))
        holder.date.text =formattedDate

        holder.itemView.setOnClickListener{
            val intent = Intent(context, DetailedTransaction::class.java)
            intent.putExtra("transaction_table",transaction)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    fun setData(transactions: List<Transactions>){
        this.transactions = transactions
        notifyDataSetChanged()
    }

}