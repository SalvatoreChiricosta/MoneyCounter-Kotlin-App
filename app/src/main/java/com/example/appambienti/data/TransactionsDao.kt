package com.example.appambienti.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransactionsDao {
    @Query(value = "SELECT* FROM transaction_table")
    fun getAll(): List<Transactions>

    @Insert
    fun insertAll(vararg transaction: Transactions)

    @Delete
    fun delete(transaction: Transactions)

    @Update
    fun update(vararg transaction: Transactions)
}