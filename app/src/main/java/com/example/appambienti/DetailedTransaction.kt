package com.example.appambienti

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.example.appambienti.data.Transactions
import com.example.appambienti.data.TransactionsDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

class DetailedTransaction : AppCompatActivity(), SensorEventListener {
    private lateinit var transactions: Transactions
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0
    private var shakeThresholdGravity = 2.7f
    private var shakeTimeThreshold = 1000L // 1 second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_transaction)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_UI)
        }

        val labelInput = findViewById<EditText>(R.id.label_input)
        val amountInput = findViewById<EditText>(R.id.amount_input)
        val labelLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.label_layout)
        val amountLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.amount_layout)
        val updateButton = findViewById<Button>(R.id.updateTransaction_button)
        val deleteButton = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.deleteTransaction_button)
        val categoryLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.category_layout)
        val categorie = resources.getStringArray(R.array.categorie)
        val categoryInput = findViewById<AutoCompleteTextView>(R.id.category_input)
        val adapterItems = ArrayAdapter<String>(this, R.layout.drop_down_item, categorie)
        val descriptionInput = findViewById<EditText>(R.id.description_input)
        val dateLabel = findViewById<TextView>(R.id.date_text)

        transactions = intent.getSerializableExtra("transaction_table") as Transactions

        labelInput.setText(transactions.label)
        amountInput.setText(transactions.amount.toString())
        categoryInput.setText(transactions.category)
        descriptionInput.setText(transactions.description)

        categoryInput.setAdapter(adapterItems)

        val rootView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootView)
        rootView.setOnClickListener {
            this.window.decorView.clearFocus()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        labelInput.addTextChangedListener {
            updateButton.visibility = View.VISIBLE
            if (it!!.count() > 0)
                labelLayout.error = null
        }
        amountInput.addTextChangedListener {
            updateButton.visibility = View.VISIBLE

            if (it!!.count() > 0)
                amountLayout.error = null
        }
        categoryInput.addTextChangedListener {
            updateButton.visibility = View.VISIBLE

            if (it!!.count() > 0)
                categoryLayout.error = null
        }
        descriptionInput.addTextChangedListener {
            updateButton.visibility = View.VISIBLE
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(transactions.date))
        dateLabel.text = formattedDate

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        updateButton.setOnClickListener {
            val label = labelInput.text.toString()
            val amount = amountInput.text.toString().toDouble()
            val category = categoryInput.text.toString()
            val description = descriptionInput.text.toString()

            if (label.isEmpty())
                labelLayout.error = "Inserisci un nome valido"
            else if (amount == null)
                amountLayout.error = "Inserisci una quantità valida"
            else if (category.isEmpty())
                categoryLayout.error = "Inserisci un nome valido"
            else {
                val transactions = Transactions(transactions.id, label, amount, category, description, transactions.date/*, transactions.position*/)
                update(transactions)
            }

        }

        val clsButton = findViewById<ImageButton>(R.id.closeButton)
        clsButton.setOnClickListener {
            finish()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Conferma Eliminazione")
        builder.setMessage("Sei sicuro di voler cancellare questa transazione?")

        builder.setPositiveButton("Sì") { dialog, _ ->
            delete(transactions)
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun update(transactions: Transactions) {
        val db = Room.databaseBuilder(this,
            TransactionsDatabase::class.java,
            "transactions_table").build()
        GlobalScope.launch {
            db.transactionsDao().update(transactions)
            finish()
        }
    }

    private fun delete(transactions: Transactions) {
        val db = Room.databaseBuilder(this,
            TransactionsDatabase::class.java,
            "transactions_table").build()
        GlobalScope.launch {
            db.transactionsDao().delete(transactions)
            finish()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

            if (gForce > shakeThresholdGravity) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime < shakeTimeThreshold) {
                    return
                }
                lastShakeTime = now
                showDeleteConfirmationDialog()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}
