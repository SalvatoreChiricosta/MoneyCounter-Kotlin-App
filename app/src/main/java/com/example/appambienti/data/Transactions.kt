package com.example.appambienti.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transaction_table")
data class Transactions(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val label: String,
    val amount: Double,
    val category: String,
    val description: String,
    val date : Long /* ,
   val position: String*/): Serializable{
    }

