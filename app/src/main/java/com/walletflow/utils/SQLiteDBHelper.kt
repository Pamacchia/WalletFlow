package com.walletflow.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteDBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private val DATABASE_NAME = "ICONS.sqlite"
        private val DATABASE_VERSION = 1
        const val ISADDED = "isAdded"
        val CATEGORY_TABLE = "category_table"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + CATEGORY_TABLE + " ("
                + "id" + " INTEGER PRIMARY KEY, " +
                "file_path" + " TEXT," +
                "icon_name" + " TEXT," +
                ISADDED + " INTEGER" + ");")

        db.execSQL(query)

        val values = ContentValues()

        val array1 =
            arrayOf("food.png", "popcorn.png", "technology.png", "tshirt.png", "transport.png", "food.png", "popcorn.png", "technology.png", "tshirt.png", "transport.png", "food.png", "popcorn.png", "technology.png", "tshirt.png", "transport.png", "food.png", "popcorn.png", "technology.png", "tshirt.png", "transport.png", "food.png", "popcorn.png", "technology.png", "tshirt.png", "transport.png")
        val array2 = arrayOf(1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0)

        val array3 = array1.map { fileName ->
            fileName.removeSuffix(".png")
        }.toTypedArray()

        for (i in array1.indices) {
            values.put("file_path", array1[i])
            values.put(ISADDED, array2[i])
            values.put("icon_name", array3[i])

            db.insert(CATEGORY_TABLE, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE)
        onCreate(db)
    }

    fun addCategory(selected: String, name: String) {
        val db = this.writableDatabase
        val values = ContentValues()


        values.put("file_path", "$selected.png")
        values.put("icon_name", name)
        values.put(ISADDED, 1)

        db.update(CATEGORY_TABLE, values,"file_path=?", arrayOf<String>("$selected.png"))
        db.close()
    }

    fun getCategories(type : Int): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM " + CATEGORY_TABLE + " WHERE $ISADDED = $type", null)
    }

}