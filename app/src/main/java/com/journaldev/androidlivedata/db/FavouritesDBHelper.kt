package com.journaldev.androidlivedata.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavouritesDBHelper(context: Context?) : SQLiteOpenHelper(context, DbSettings.DB_NAME, null, DbSettings.DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE " + DbSettings.DBEntry.TABLE + " ( " +
                DbSettings.DBEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DbSettings.DBEntry.COL_FAV_IMAGE + " TEXT NOT NULL, " +
                DbSettings.DBEntry.COL_FAV_URL + " TEXT NOT NULL, " +
                DbSettings.DBEntry.COL_FAV_DATE + " INTEGER NOT NULL," +
                DbSettings.DBEntry.COL_FAV_EMAIL + " TEXT NOT NULL," +
                DbSettings.DBEntry.COL_FAV_PHONENUMBER + " TEXT NOT NULL);"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + DbSettings.DBEntry.TABLE)
        onCreate(db)
    }
}