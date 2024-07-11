package com.example.appambienti

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class Filter : AppCompatActivity() {
    private lateinit var categorySpinner: Spinner
    private lateinit var applyFilterButton: Button

    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        categorySpinner = findViewById(R.id.category_spinner)
        applyFilterButton = findViewById(R.id.apply_filter_button)

        setupCategorySpinner()

        applyFilterButton.setOnClickListener {
            applyFilters()
        }
    }

    private fun setupCategorySpinner() {
        val categories = resources.getStringArray(R.array.categorie_filter)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = if (position == 0) null else parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null
            }
        }
    }

    private fun applyFilters() {
        val intent = Intent().apply {
            putExtra("category", selectedCategory)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}
