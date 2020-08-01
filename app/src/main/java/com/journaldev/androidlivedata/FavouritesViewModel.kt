package com.journaldev.androidlivedata

import android.app.Application
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.journaldev.androidlivedata.db.DbSettings
import com.journaldev.androidlivedata.db.FavouritesDBHelper
import java.lang.Exception
import java.util.*

class FavouritesViewModel(application: Application?) : AndroidViewModel(application!!) {
    private val mFavHelper: FavouritesDBHelper = FavouritesDBHelper(application)
    private var mFavs: MutableLiveData<List<Favourites>>? = null
    val favs: MutableLiveData<List<Favourites>>
        get() {
            if (mFavs == null) {
                mFavs = MutableLiveData()
                loadFavs()
            }
            return mFavs!!
        }

    private fun loadFavs() {
        val newFavs: MutableList<Favourites> = ArrayList()
        val db = mFavHelper.readableDatabase
        val cursor = db.query(DbSettings.DBEntry.TABLE, arrayOf(
                DbSettings.DBEntry._ID,
                DbSettings.DBEntry.COL_FAV_IMAGE,
                DbSettings.DBEntry.COL_FAV_URL,
                DbSettings.DBEntry.COL_FAV_DATE,
                DbSettings.DBEntry.COL_FAV_EMAIL,
                DbSettings.DBEntry.COL_FAV_PHONENUMBER
        ),
                null, null, null, null, null)
        while (cursor.moveToNext()) {
            val idxId = cursor.getColumnIndex(DbSettings.DBEntry._ID)
            val idxImage = cursor.getColumnIndex(DbSettings.DBEntry.COL_FAV_IMAGE)
            val idxUrl = cursor.getColumnIndex(DbSettings.DBEntry.COL_FAV_URL)
            val idxDate = cursor.getColumnIndex(DbSettings.DBEntry.COL_FAV_DATE)
            val idxEmail = cursor.getColumnIndex(DbSettings.DBEntry.COL_FAV_EMAIL)
            val idxPhoneNumber = cursor.getColumnIndex(DbSettings.DBEntry.COL_FAV_PHONENUMBER)
            newFavs.add(Favourites(cursor.getLong(idxId), cursor.getString(idxImage), cursor.getString(idxUrl), cursor.getLong(idxDate), cursor.getString(idxEmail), cursor.getString(idxPhoneNumber)))
        }
        cursor.close()
        db.close()
        mFavs!!.value = newFavs
    }

    fun addFav(image: String?, url: String?, date: Long, email: String?, phoneNumber: String?) {
        val db = mFavHelper.writableDatabase
        val values = ContentValues()
        values.put(DbSettings.DBEntry.COL_FAV_IMAGE, image)
        values.put(DbSettings.DBEntry.COL_FAV_URL, url)
        values.put(DbSettings.DBEntry.COL_FAV_DATE, date)
        values.put(DbSettings.DBEntry.COL_FAV_EMAIL, email)
        values.put(DbSettings.DBEntry.COL_FAV_PHONENUMBER, phoneNumber)
        val id = db.insertWithOnConflict(DbSettings.DBEntry.TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        val favourites = mFavs!!.value
        val clonedFavs: ArrayList<Favourites>
        if (favourites == null) {
            clonedFavs = ArrayList()
        } else {
            clonedFavs = ArrayList(favourites.size)
            for (i in favourites.indices) {
                clonedFavs.add(Favourites(favourites[i]))
            }
        }
        val fav = Favourites(id, image!!, url!!, date, email!!, phoneNumber!!)
        clonedFavs.add(fav)
        mFavs!!.value = clonedFavs
    }

    fun removeFav(id: Long) {
        val db = mFavHelper.writableDatabase
        db.delete(
                DbSettings.DBEntry.TABLE,
                DbSettings.DBEntry._ID + " = ?", arrayOf(id.toString()))
        db.close()
        val favs = mFavs!!.value!!
        val clonedFavs = ArrayList<Favourites>(favs.size)
        for (i in favs.indices) {
            clonedFavs.add(Favourites(favs[i]))
        }
        var index = -1
        for (i in clonedFavs.indices) {
            val favourites = clonedFavs[i]
            if (favourites.mId == id) {
                index = i
            }
        }
        if (index != -1) {
            clonedFavs.removeAt(index)
        }
        mFavs!!.value = clonedFavs
    }

    fun updateFav(imageTxt: String, nameTxt: String, date: Long, emailTxt: String, phoneTxt: String, id: Long): Boolean {
        val db = mFavHelper.writableDatabase
        val values = ContentValues()
        var result: Boolean = false
        values.put(DbSettings.DBEntry.COL_FAV_IMAGE, imageTxt)
        values.put(DbSettings.DBEntry.COL_FAV_URL, nameTxt)
        values.put(DbSettings.DBEntry.COL_FAV_DATE, date)
        values.put(DbSettings.DBEntry.COL_FAV_EMAIL, emailTxt)
        values.put(DbSettings.DBEntry.COL_FAV_PHONENUMBER, phoneTxt)
        try {
            db.update(DbSettings.DBEntry.TABLE, values, DbSettings.DBEntry._ID + " = ?", arrayOf(id.toString()))
            result = true
        } catch (e: Exception) {
            result = false
        }
        return result
    }

}