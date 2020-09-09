package Utilities

import android.content.Context
import android.content.SharedPreferences
import com.android.volley.toolbox.Volley

class SharedPrefs(context: Context) {  // this class is used to save the default or existingl oged in user key value pairs

    val PREFS_FILENAME= "prefs"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME,0)// 0 means content is private


    val IS_LOGGED_IN= "prefs" //constants for keys
    val AUTH_TOKEN= "authToken"
    val USER_EMAIL= "userEmail"

    var isLoggedIn: Boolean
       get()= prefs.getBoolean(IS_LOGGED_IN,false)
       set(value) = prefs.edit().putBoolean(IS_LOGGED_IN,value).apply()

    var authToken: String?
        get() = prefs.getString(AUTH_TOKEN,"")
        set(value) = prefs.edit().putString(AUTH_TOKEN,value).apply()

    var userEmail: String?
        get() = prefs.getString(USER_EMAIL,"")
        set(value) = prefs.edit() .putString(USER_EMAIL,value).apply()


    var requestQueue= Volley.newRequestQueue(context)
}