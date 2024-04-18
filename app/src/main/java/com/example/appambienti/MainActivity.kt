package com.example.appambienti

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Intent
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appambienti.data.TransactionAdapter
import com.example.appambienti.data.Transactions



class MainActivity : AppCompatActivity() {
    private lateinit var transactions : List<Transactions>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transactions = arrayListOf()

        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)



        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.apply {
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }

        updateDashBoard()
        val button = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.addTransaction_button )
        button.setOnClickListener{
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }



    private fun updateDashBoard(){
        val totalAmount = transactions.map { it.amount }.sum()
        val budgetAmount = transactions.filter { it.amount>0 }.map { it.amount }.sum()
        val expenseAmount = totalAmount - budgetAmount

        val bilancio = findViewById<TextView>(R.id.balance)
        val budget = findViewById<TextView>(R.id.budget)
        val spese = findViewById<TextView>(R.id.expense)
        bilancio.text = "%.2f €".format(totalAmount)
        spese.text = "%.2f €".format(expenseAmount)
        budget.text = "%.2f €".format(budgetAmount)
    }


}