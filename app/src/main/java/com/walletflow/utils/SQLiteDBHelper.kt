package com.walletflow.utils

import android.content.ContentValues
import android.database.Cursor
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteDBHelper(context: Context, factory : SQLiteDatabase.CursorFactory?)
    : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object{
        // below is variable for database name
        private val DATABASE_NAME = "USER_DATA"

        // below is the variable for database version
        private val DATABASE_VERSION = 1

        // below is the variable for table name
        val CATEGORY_TABLE = "category_table"
    }

        // below is the method for creating a database by a sqlite query
        override fun onCreate(db: SQLiteDatabase) {
            // below is a sqlite query, where column names
            // along with their data types is given
            val query = ("CREATE TABLE " + CATEGORY_TABLE + " ("
                    + "id" + " INTEGER PRIMARY KEY, " +
                    "file_path" + " TEXT," +
                    "added" + "BOOLEAN " + ");" +
                    "INSERT INTO" + CATEGORY_TABLE + "(id, file_path, added) VALUE (1, 'path/to/file1.txt', 1), " +
                    "(2, 'path/to/file2.txt', 0), " +
                    "(3, 'path/to/file3.txt', 1);")

            // we are calling sqlite
            // method for executing our query
            db.execSQL(query)
        }

        override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
            // this method is to check if table already exists
            db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE)
            onCreate(db)
        }

        // This method is for adding data in our database
        fun addName(path : String, added : Boolean ){

            // below we are creating
            // a content values variable
            val values = ContentValues()

            // we are inserting our values
            // in the form of key-value pair
            values.put("file_path", path)
            values.put("added", added)

            // here we are creating a
            // writable variable of
            // our database as we want to
            // insert value in our database
            val db = this.writableDatabase

            // all values are inserted into database
            db.insert(CATEGORY_TABLE, null, values)

            // at last we are
            // closing our database
            db.close()
        }

        // below method is to get
        // all data from our database
        fun getName(): Cursor? {

            // here we are creating a readable
            // variable of our database
            // as we want to read value from it
            val db = this.readableDatabase

            // below code returns a cursor to
            // read data from the database
            return db.rawQuery("SELECT * FROM " + CATEGORY_TABLE, null)

        }

        fun emptyTable() {
            val db = this.writableDatabase
            db.delete(CATEGORY_TABLE, null, null)
        }
}