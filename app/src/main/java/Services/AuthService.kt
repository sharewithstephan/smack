package Services

import Controller.App
import Utilities.*
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

object AuthService {

//    var isLoggedIn= false
//    var userEmail= ""
//    var authToken= ""

    fun registerUser(context:Context, email: String, password: String, complete:(Boolean) ->Unit)
    {
        val jsonBody = JSONObject()
        jsonBody.put("email",email)
        jsonBody.put("password",password)

        val requestBody=jsonBody.toString()

        val registerRequest= object :StringRequest(Method.POST, URL_REGISTER, Response.Listener {response ->
            println(response)
            complete(true)},
            Response.ErrorListener {error ->
            Log.d("ERROR","Could not register the user: $error")
            complete(false) }) {

            override fun getBodyContentType(): String {

                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }


        }
       App.prefs.requestQueue.add(registerRequest)


    }


    fun loginUser(context: Context,email: String,password: String,complete: (Boolean) -> Unit)
    {
        val jsonBody = JSONObject()
        jsonBody.put("email",email)
        jsonBody.put("password",password)

        val requestBody=jsonBody.toString()

        val loginRequest= object: JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener {response ->


            // incase user never exist
            try{
          App.prefs.userEmail=response.getString("user")
          App.prefs.authToken=response.getString("token")     // getting the token and email to confirm the user is logged in
          App.prefs.isLoggedIn= true
            complete(true)
          } catch (e: JSONException){
              Log.d("JSON","EXC:" + e.localizedMessage)
              complete(false)
          }



        },Response.ErrorListener {error ->
            Log.d("ERROR","Could not login the user: $error")
            complete(false)  }){

            override fun getBodyContentType(): String {

                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

        }

        App.prefs.requestQueue.add(loginRequest)


    }

    fun createUser(context: Context, name: String, email: String, avatarName: String,avatarColor: String,complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("email",email)
        jsonBody.put("name",name)
        jsonBody.put("avatarColor",avatarColor)
        jsonBody.put("avatarName",avatarName)

        val requestBody=jsonBody.toString()

         val createRequest= object : JsonObjectRequest(Method.POST, URL_CREATE_USER,null,Response.Listener { response ->

             try {

                 UserDataService.name= response.getString("name")
                 UserDataService.email= response.getString("email")
                 UserDataService.avatarName= response.getString("avatarName")
                 UserDataService.avatarColor= response.getString("avatarColor")
                 UserDataService.id=response.getString("_id")
                 complete(true)


             }catch (e:JSONException){
               Log.d("ERROR","EXC" + e.localizedMessage)
                 complete(false)
             }
         },
          Response.ErrorListener {error ->
              Log.d("ERROR","Could not add user: $error")
              complete(false)


          }){

             override fun getBodyContentType(): String {

                 return "application/json; charset=utf-8"
             }

             override fun getBody(): ByteArray {
                 return requestBody.toByteArray()
             }

             override fun getHeaders(): MutableMap<String, String> {   //authorisation required for adding a user
                 val headers= HashMap<String, String>()  //Hash map is a data structure where key, value pairs are stored
                 headers.put("Authorization","Bearer ${App.prefs.authToken}")
                 return headers
             }


         }
        App.prefs.requestQueue.add(createRequest)


    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit){

        val findUserRequest= object : JsonObjectRequest(Method.GET,"$URL_GET_USER${App.prefs.userEmail}",null,Response.Listener {response->


            try {
                UserDataService.name=response.getString("name")
                UserDataService.email=response.getString("email")
                UserDataService.avatarName=response.getString("avatarName")
                UserDataService.avatarColor=response.getString("avatarColor")
                UserDataService.id=response.getString("_id")

                val userDataChange= Intent(BROADCAST_USER_DATA_CHANGE)
                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                complete(true)


            } catch (e: JSONException){
                Log.d("JSON","EXC:" + e.localizedMessage)

            }
        },
            Response.ErrorListener {error ->
                Log.d("ERROR","could not find user")
                complete(false)
            }){


            override fun getBodyContentType(): String {

                return "application/json; charset=utf-8"
            }



            override fun getHeaders(): MutableMap<String, String> {   //authorisation required for adding a user
                val headers= HashMap<String, String>()  //Hash map is a data structure where key, value pairs are stored
                headers.put("Authorization","Bearer ${App.prefs.authToken}")
                return headers
            }


        }

        App.prefs.requestQueue.add(findUserRequest)

    }






}