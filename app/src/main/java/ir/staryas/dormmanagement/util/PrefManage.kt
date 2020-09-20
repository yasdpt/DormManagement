package ir.staryas.dormmanagement.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class PrefManage @SuppressLint("CommitPrefEdits") constructor(context: Context) {
    var pref:SharedPreferences? = null
    var editor:SharedPreferences.Editor? = null
    var _context:Context? = context

    var PRIVATE_MODE = 0
    private val PREF_NAME = "hozurghiyab-user"
    private val prefUsername = "username"
    private val prefUserId = "id"
    private val prefFullName = "fullname"
    private val prefisUserLoggedIn = "isUserLoggedIn"
    private val prefUserRole = "userRole"
    private val prefTuition = "tuitionAmount"
    private val prefTerm = "term"
    private val prefT8 = "prefT8"
    private val prefT10 = "prefT10"
    private val prefT12 = "prefT12"


    init {
        pref = _context!!.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref!!.edit()
    }

    fun setUsername(username: String) {
        editor!!.putString(prefUsername, username)
        editor!!.commit()
    }

    fun getUsername(): String? {
        return pref!!.getString(prefUsername, "null")
    }

    fun setTerm(term: String) {
        editor!!.putString(prefTerm, term)
        editor!!.commit()
    }

    fun getTerm(): String? {
        return pref!!.getString(prefTerm, "null")
    }

    fun setTuitionAmount(tuition: Int) {
        editor!!.putInt(prefTuition, tuition)
        editor!!.commit()
    }

    fun getTuition(): Int? {
        return pref!!.getInt(prefTuition, 0)
    }

    fun setIsUserLoggedIn(isUserLoggedIn:Boolean) {
        editor!!.putBoolean(prefisUserLoggedIn,isUserLoggedIn)
        editor!!.commit()
    }
    fun getIsUserLoggedIn():Boolean? {
        return pref!!.getBoolean(prefisUserLoggedIn,false)
    }

    fun setUserId(userId: Int) {
        editor!!.putInt(prefUserId, userId)
        editor!!.commit()
    }


    fun getUserId(): Int? {
        return pref!!.getInt(prefUserId, 0)
    }

    fun getT8(): Int? {
        return pref!!.getInt(prefT8, 0)
    }


    fun setT8(t8: Int) {
        editor!!.putInt(prefT8, t8)
        editor!!.commit()
    }

    fun getT10(): Int? {
        return pref!!.getInt(prefT10, 0)
    }


    fun setT10(t10: Int) {
        editor!!.putInt(prefT10, t10)
        editor!!.commit()
    }

    fun getT12(): Int? {
        return pref!!.getInt(prefT12, 0)
    }


    fun setT12(t12: Int) {
        editor!!.putInt(prefT12, t12)
        editor!!.commit()
    }


    fun setFullName(fullName: String) {
        editor!!.putString(prefFullName, fullName)
        editor!!.commit()
    }

    fun getFullName(): String? {
        return pref!!.getString(prefFullName, "null")
    }

    fun setUserRole(userRole:String) {
        editor!!.putString(prefUserRole,userRole)
        editor!!.commit()
    }

    fun getUserRole(): String? {
        return pref!!.getString(prefUserRole, "admin")
    }

}