package com.example.jobswype.session

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.jobswype.LoginActivity

class LoginSession {
    lateinit var sess : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var context : Context
    var PRIVATE_MODE = 0

    constructor(context: Context){
        this.context = context
        sess = context.getSharedPreferences(SESS_NAME, PRIVATE_MODE)
        editor = sess.edit()
    }

    companion object {
        val SESS_NAME = "Login_Session"
        val IS_LOGIN = "isLoggedin"
        val KEY_PASSWORD = "password"
        val KEY_EMAIL = "email"
    }

    fun createLoginSession(password: String, email: String){
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_PASSWORD, password)
        editor.putString(KEY_EMAIL, email)
        editor.commit()
    }

    /*
    fun checkLogin() {
        if (!this.isLoggedIn()) {
            val i = Intent(context, LoginActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }*/

    fun getUserDetails(): HashMap<String, String> {
        val user: Map<String, String> = HashMap<String, String>()
        (user as HashMap).put(KEY_PASSWORD, sess.getString(KEY_PASSWORD, null)!!)
        user.put(KEY_EMAIL, sess.getString(KEY_EMAIL, null)!!)
        return user
    }

    fun logoutUser() {
        editor.clear()
        editor.commit()
        val i = Intent(context, LoginActivity::class.java) //change init var to val
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }

    fun isLoggedIn(): Boolean {
        return sess.getBoolean(IS_LOGIN, false)
    }

}