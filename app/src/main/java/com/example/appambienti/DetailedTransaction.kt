package com.example.appambienti

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.example.appambienti.data.Transactions
import com.example.appambienti.data.TransactionsDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DetailedTransaction : AppCompatActivity() {
    private lateinit var transactions:Transactions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_transaction)
        val labelInput = findViewById<EditText>(R.id.label_input)
        val amountInput = findViewById<EditText>(R.id.amount_input)
        val labelLayout =  findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.label_layout)
        val amountLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.amount_layout)
        val updateButton = findViewById<Button>(R.id.updateTransaction_button )
        val categoryLayout =  findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.category_layout)
        val categorie = resources.getStringArray(R.array.categorie)
        val categoryInput = findViewById<AutoCompleteTextView>(R.id.category_input)
        val adapterItems = ArrayAdapter<String> (this,R.layout.drop_down_item, categorie )
        val descriptionInput = findViewById<EditText>(R.id.description_input)

        transactions = intent.getSerializableExtra("transaction_table") as Transactions

        labelInput.setText(transactions.label)
        amountInput.setText(transactions.amount.toString())
        categoryInput.setText(transactions.category)
        descriptionInput.setText(transactions.description)

        categoryInput.setAdapter(adapterItems)

        val rootView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootView)
        rootView.setOnClickListener{
            this.window.decorView.clearFocus()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        labelInput.addTextChangedListener{
            updateButton.visibility = View.VISIBLE
            if(it!!.count()>0)
                labelLayout.error = null
        }
        amountInput.addTextChangedListener{
            updateButton.visibility = View.VISIBLE

            if(it!!.count()>0)
                amountLayout.error = null
        }
        categoryInput.addTextChangedListener{
            updateButton.visibility = View.VISIBLE

            if(it!!.count()>0)
                categoryLayout.error = null
        }
        descriptionInput.addTextChangedListener {
            updateButton.visibility = View.VISIBLE
        }

        updateButton.setOnClickListener{
            val label = labelInput.text.toString()
            val amount = amountInput.text.toString().toDouble()
            val category = categoryInput.text.toString()
            val description = descriptionInput.text.toString()

            if(label.isEmpty())
                labelLayout.error="Inserisci un nome valido"
            else if(amount==null)
                amountLayout.error="Inserisci una quantità valida"
            else if(category.isEmpty())
                categoryLayout.error="Inserisci un nome valido"
            else{
                val transactions = Transactions(transactions.id, label, amount, category, description)
                update(transactions)
            }

        }




        val clsButton = findViewById<ImageButton>(R.id.closeButton)
        clsButton.setOnClickListener{
            finish()
        }
    }

    private fun update(transactions: Transactions){
        val db = Room.databaseBuilder(this,
            TransactionsDatabase::class.java,
            "transactions_table").build()
        GlobalScope.launch{
            db.transactionsDao().update(transactions)
            finish()
        }
    }
}