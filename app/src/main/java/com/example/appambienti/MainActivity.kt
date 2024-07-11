package com.example.appambienti

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.appambienti.data.Transactions
import com.example.appambienti.data.TransactionsDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transactions
    private lateinit var transactionsList: List<Transactions>
    private lateinit var oldTransactions: List<Transactions>

    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db: TransactionsDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private val CATEGORY_FILTER_KEY = "category_filter"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

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

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
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

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerview)


        val button = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.addTransaction_button)
        button.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        val filterButton = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.filter_button)
        filterButton.setOnClickListener {
            val intent = Intent(this, Filter::class.java)
            startActivityForResult(intent, FILTER_REQUEST_CODE)
        }

        // Restore saved filter
        val savedCategory = sharedPreferences.getString(CATEGORY_FILTER_KEY, null)
        fetchFiltered(savedCategory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILTER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedCategory = data?.getStringExtra("category")
            with(sharedPreferences.edit()) {
                putString(CATEGORY_FILTER_KEY, selectedCategory)
                apply()
            }
            fetchFiltered(selectedCategory)
        }
    }

    private fun fetchAll() {
        GlobalScope.launch {
            transactionsList = db.transactionsDao().getAll().sortedByDescending { it.date }

            runOnUiThread {
                updateDashBoard()
                transactionAdapter.setData(transactionsList)
            }
        }
    }

    private fun fetchFiltered(category: String?) {

        GlobalScope.launch {
            transactionsList = if (category != null && category != "Tutte") {
                db.transactionsDao().getTransactionsByCategory(category).sortedByDescending { it.date }


            } else {
                db.transactionsDao().getAll().sortedByDescending { it.date }
            }

            runOnUiThread {

                updateDashBoard()
                transactionAdapter.setData(transactionsList)
                val recyclerViewTitle = findViewById<TextView>(R.id.recyclerview_title)
                recyclerViewTitle.text = if (category != null && category != "Tutte") {
                    "Transazioni filtrate per $category"
                } else {
                    "Tutte le transazioni"
                }

            }

        }
    }

    private fun updateDashBoard() {
        val totalAmount = transactionsList.map { it.amount }.sum()
        val budgetAmount = transactionsList.filter { it.amount > 0 }.map { it.amount }.sum()
        val expenseAmount = totalAmount - budgetAmount
        val bilancio = findViewById<TextView>(R.id.balance)
        val budget = findViewById<TextView>(R.id.budget)
        val spese = findViewById<TextView>(R.id.expense)
        bilancio.text = "%.2f €".format(totalAmount)
        budget.text = "%.2f €".format(budgetAmount)
        spese.text = "%.2f €".format(expenseAmount)
    }

    private fun undoDelete() {
        GlobalScope.launch {
            db.transactionsDao().insertAll(deletedTransaction)

            transactionsList = oldTransactions
            runOnUiThread {
                updateDashBoard()
                transactionAdapter.setData(transactionsList)
            }
        }
    }

    private fun showSnackbar() {
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "Transazione eliminata", Snackbar.LENGTH_LONG)
        snackbar.setAction("Annullare") {
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun deleteTransaction(transactions: Transactions) {
        deletedTransaction = transactions
        oldTransactions = transactionsList

        GlobalScope.launch {
            db.transactionsDao().delete(transactions)
            transactionsList = transactionsList.filter { it.id != transactions.id }

            runOnUiThread {
                updateDashBoard()
                transactionAdapter.setData(transactionsList)

                showSnackbar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val savedCategory = sharedPreferences.getString(CATEGORY_FILTER_KEY, null)
        fetchFiltered(savedCategory)
    }

    companion object {
        const val FILTER_REQUEST_CODE = 1
    }
}
