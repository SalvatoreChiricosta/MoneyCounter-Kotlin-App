package com.example.appambienti.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf( Transactions::class),version = 1)
abstract class TransactionsDatabase : RoomDatabase(){

    abstract fun transactionsDao(): TransactionsDao

}