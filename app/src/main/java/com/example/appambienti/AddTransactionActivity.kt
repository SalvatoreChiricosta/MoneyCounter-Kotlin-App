package com.example.appambienti

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.example.appambienti.data.Transactions
import com.example.appambienti.data.TransactionsDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class AddTransactionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)


        val labelInput = findViewById<EditText>(R.id.label_input)
        val amountInput = findViewById<EditText>(R.id.amount_input)
        val labelLayout =  findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.label_layout)
        val amountLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.amount_layout)
        val addButton = findViewById<Button>(R.id.addTransaction_button )
        val categoryLayout =  findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.category_layout)
        val categorie = resources.getStringArray(R.array.categorie)
        val categoryInput = findViewById<AutoCompleteTextView>(R.id.category_input)
        val adapterItems = ArrayAdapter<String> (this,R.layout.drop_down_item, categorie )
        val descriptionInput = findViewById<EditText>(R.id.description_input)



        categoryInput.setAdapter(adapterItems)

        labelInput.addTextChangedListener{
            if(it!!.count()>0)
                labelLayout.error = null
        }
        amountInput.addTextChangedListener{
            if(it!!.count()>0)
                amountLayout.error = null
        }
        categoryInput.addTextChangedListener{
            if(it!!.count()>0)
                categoryLayout.error = null
        }
        addButton.setOnClickListener{
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
                val transactions = Transactions(0, label, amount, category, description, System.currentTimeMillis())
                insert(transactions)
            }

        }




        val clsButton = findViewById<ImageButton>(R.id.closeButton)
        clsButton.setOnClickListener{
            finish()
        }
    }

    private fun insert(transactions: Transactions){
        val db = Room.databaseBuilder(this,
            TransactionsDatabase::class.java,
            "transactions_table").build()
        GlobalScope.launch{
            db.transactionsDao().insertAll(transactions)
            finish()
        }
    }


}