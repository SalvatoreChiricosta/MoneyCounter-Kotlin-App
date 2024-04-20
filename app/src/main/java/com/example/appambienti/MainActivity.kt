package com.example.appambienti

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.appambienti.data.TransactionAdapter
import com.example.appambienti.data.Transactions
import com.example.appambienti.data.TransactionsDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transactions
    private lateinit var transactionsList : List<Transactions>
    private lateinit var oldTransactions : List<Transactions>

    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db : TransactionsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transactionsList = arrayListOf()

        transactionAdapter = TransactionAdapter(transactionsList)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(this,
            TransactionsDatabase::class.java,
            "transactions_table").build()

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.apply {
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }

        val itemTouchHepler = object :ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactionsList[viewHolder.adapterPosition])
            }
        }

        val swipeHelper = ItemTouchHelper(itemTouchHepler)
        swipeHelper.attachToRecyclerView(recyclerview)

        val button = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.addTransaction_button )
        button.setOnClickListener{
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchAll(){
        GlobalScope.launch {
            transactionsList = db.transactionsDao().getAll()

            runOnUiThread{
                updateDashBoard()
                transactionAdapter.setData(transactionsList)
            }
        }
    }

    private fun updateDashBoard(){
        val totalAmount = transactionsList.map { it.amount }.sum()
        val budgetAmount = transactionsList.filter { it.amount>0 }.map { it.amount }.sum()
        val expenseAmount = totalAmount - budgetAmount
        val bilancio = findViewById<TextView>(R.id.balance)
        val budget = findViewById<TextView>(R.id.budget)

        val spese = findViewById<TextView>(R.id.expense)
        bilancio.text = "%.2f €".format(totalAmount)
        budget.text = "%.2f €".format(budgetAmount)
        spese.text = "%.2f €".format(expenseAmount)

    }
    private fun undoDelete(){
        GlobalScope.launch {
            db.transactionsDao().insertAll(deletedTransaction)

            transactionsList = oldTransactions
            runOnUiThread{
                updateDashBoard()
                transactionAdapter.setData(transactionsList)
            }
        }
    }
    private fun showSnackbar(){
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view,"Transazione eliminata",Snackbar.LENGTH_LONG)
        snackbar.setAction("Annullare"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this,R.color.red))
            .setTextColor(ContextCompat.getColor(this,R.color.white))
            .show()
    }


    private fun deleteTransaction(transactions: Transactions){
        deletedTransaction = transactions
        oldTransactions = transactionsList

        GlobalScope.launch {
            db.transactionsDao().delete(transactions)
            transactionsList = transactionsList.filter { it.id != transactions.id }       }
            runOnUiThread{
                updateDashBoard()
                transactionAdapter.setData(transactionsList)
                showSnackbar()
            }
    }
    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}