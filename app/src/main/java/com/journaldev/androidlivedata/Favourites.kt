package com.journaldev.androidlivedata

class Favourites {
    @JvmField
    var mId: Long
    var mImage: String
    var mUrl: String
    var mDate: Long
    var mEmail: String
    var mPhoneNumber: String

    constructor(id: Long, image: String, name: String, date: Long, email: String, phoneNumber: String) {
        mId = id
        mImage = image
        mUrl = name
        mDate = date
        mEmail = email
        mPhoneNumber = phoneNumber
    }

    constructor(favourites: Favourites) {
        mId = favourites.mId
        mImage = favourites.mImage
        mUrl = favourites.mUrl
        mDate = favourites.mDate
        mEmail = favourites.mEmail
        mPhoneNumber = favourites.mPhoneNumber
    }
}