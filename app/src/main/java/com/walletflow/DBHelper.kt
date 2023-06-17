package com.walletflow

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                USERNAME_COL + " TEXT," +
                EMAIL_COL + " TEXT," +
                PASSWORD_COL + " TEXT" + ")")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    // This method is for adding data in our database
    fun addUser(username : String, email: String, password : String){

        // below we are creating
        // a content values variable
        val values = ContentValues()

        // we are inserting our values
        // in the form of key-value pair
        values.put(USERNAME_COL, username)
        values.put(EMAIL_COL, email)
        values.put(PASSWORD_COL, password)

        // here we are creating a
        // writable variable of
        // our database as we want to
        // insert value in our database
        val db = this.writableDatabase

        // all values are inserted into database
        db.insert(TABLE_NAME, null, values)

        // at last we are
        // closing our database
        db.close()
    }

    // below method is to get
    // all data from our database
    fun checkLogin(username: String, password: String): Boolean {
        val db = this.readableDatabase

        val selection = USERNAME_COL + " = ? AND " + PASSWORD_COL + " = ?" // Define the selection criteria

        val selectionArgs = arrayOf(username, password) // Specify the username as the selection argument

        val cursor = db.query(
            TABLE_NAME,
            null, // Retrieve all columns
            selection, // Apply the selection criteria
            selectionArgs, // Specify the selection arguments
            null, // No group by
            null, // No having
            null // No order by
        )

        return (cursor != null && cursor.moveToFirst())
    }

    fun emptyTable() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
    }

    companion object{
        // here we have defined variables for our database

        // below is variable for database name
        private val DATABASE_NAME = "GEEKS_FOR_GEEKS"

        // below is the variable for database version
        private val DATABASE_VERSION = 1

        // below is the variable for table name
        val TABLE_NAME = "gfg_table"

        // below is the variable for id column
        val ID_COL = "id"

        // below is the variable for name column
        val USERNAME_COL = "username"

        // below is the variable for name column
        val EMAIL_COL = "email"

        // below is the variable for age column
        val PASSWORD_COL = "password"
    }
}