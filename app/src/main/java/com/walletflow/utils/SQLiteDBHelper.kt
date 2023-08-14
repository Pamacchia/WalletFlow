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
        const val TYPE = "type"
        val CATEGORY_TABLE = "category_table"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + CATEGORY_TABLE + " ("
                + "id" + " INTEGER PRIMARY KEY, " +
                "file_path" + " TEXT," +
                "icon_name" + " TEXT," +
                ISADDED + " INTEGER," +
                TYPE + " TEXT" + ");")

        db.execSQL(query)

        val values = ContentValues()

        val arrayExpenseDefault = arrayOf("bills.png", "food.png", "transports.png",
            "clothes.png", "health.png", "technology.png",
            "entertainment.png", "sports.png", "gifts.png")
        val arrayEarningDefault = arrayOf("salary.png","item-sold.png")
        val arrayDefault = arrayExpenseDefault+arrayEarningDefault
        val maskDefault = IntArray(arrayDefault.size) { 1 }
        val maskCategoryExpenseDefault = Array(arrayExpenseDefault.size) { "expense" }
        val maskCategoryEarningDefault = Array(arrayEarningDefault.size) { "earning" }
        val maskTypeDefault = maskCategoryExpenseDefault + maskCategoryEarningDefault

        val arrayExpenseNotDefault = arrayOf("car.png", "gambling.png", "games.png", "pets.png", "home.png")
        val arrayEarningNotDefault = arrayOf("coin.png","pay.png","investments.png")
        val arrayNotDefault = arrayExpenseNotDefault+arrayEarningNotDefault
        val maskNotDefault = IntArray(arrayNotDefault.size) { 0 }
        val maskCategoryExpenseNotDefault = Array(arrayExpenseNotDefault.size) { "expense" }
        val maskCategoryEarningNotDefault = Array(arrayEarningNotDefault.size) { "earning" }
        val maskTypeNotDefault = maskCategoryExpenseNotDefault + maskCategoryEarningNotDefault

        val concatenatedArray = arrayDefault + arrayNotDefault
        val concatenatedMaskDefault = maskDefault + maskNotDefault
        val concatenatedMaskType = maskTypeDefault + maskTypeNotDefault

        val concatenatedLabelArray = concatenatedArray.map { fileName ->
            fileName.removeSuffix(".png")
        }.toTypedArray()

        for (i in concatenatedArray.indices) {
            values.put("file_path", concatenatedArray[i])
            values.put(ISADDED, concatenatedMaskDefault[i])
            values.put(TYPE, concatenatedMaskType[i])
            values.put("icon_name", concatenatedLabelArray[i])

            db.insert(CATEGORY_TABLE, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE)
        onCreate(db)
    }

    fun addCategory(selected: String?, name: String) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put("file_path", "$selected.png")
        values.put("icon_name", name)
        values.put(ISADDED, 1)

        db.update(CATEGORY_TABLE, values, "file_path=?", arrayOf<String>("$selected.png"))
        db.close()
    }

    fun getCategories(default: Int, type: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $CATEGORY_TABLE WHERE $ISADDED = $default AND `$TYPE` = '$type'", null)
    }

}