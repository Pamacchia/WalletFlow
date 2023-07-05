package com.walletflow.utils

import android.content.ContentValues
import android.database.Cursor
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteDBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private val DATABASE_NAME = "ICONS.sqlite"
        private val DATABASE_VERSION = 1
        val CATEGORY_TABLE = "category_table"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + CATEGORY_TABLE + " ("
                + "id" + " INTEGER PRIMARY KEY, " +
                "file_path" + " TEXT," +
                "added" + " INTEGER" + ");")

        db.execSQL(query)

        val values = ContentValues()

        val array1 =
            arrayOf("food.png", "popcorn.png", "technology.png", "tshirt.png", "transport.png")
        val array2 = arrayOf(1, 1, 1, 0, 0)

        for (i in array1.indices) {
            values.put("file_path", array1[i])
            values.put("added", array2[i])

            db.insert(CATEGORY_TABLE, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE)
        onCreate(db)
    }

    fun getAlreadyAdded(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT file_path FROM " + CATEGORY_TABLE + " WHERE added = 1", null)
    }

    fun getToAdd(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT file_path FROM " + CATEGORY_TABLE + " WHERE added = 0", null)
    }
}