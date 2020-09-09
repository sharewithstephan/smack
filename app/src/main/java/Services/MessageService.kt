package Services

import Controller.App
import Model.Channel
import Utilities.URL_GET_CHANNELS
import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

object MessageService {

    val channels= ArrayList<Channel>()

    fun getChannels(context: Context, complete: (Boolean)->Unit){

        val channelsRequest= object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS,null,Response.Listener {response ->
            try {
                for(x in 0 until response.length()){ // geting from the array of channels passed

                    val channel= response.getJSONObject(x)
                    val name= channel.getString("name")
                    val channelDesc= channel.getString("description")
                    val channelId= channel.getString("_id")

                    val newChannel= Channel(name,channelDesc,channelId)
                    this.channels.add(newChannel) //Adding new channel to the channel list



                }
                complete(true)

            } catch (e: JSONException){
                Log.d("JSON","EXC:" + e.localizedMessage)
                complete(false)

            }


        }, Response.ErrorListener {error ->
            Log.d("ERROR","Could not read Channels")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"   //tells type of encoding
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers= HashMap<String,String>()  //key,value api
                headers.put("Authorization","Bearer ${App.prefs.authToken}")
                return headers
            }

        }
        App.prefs.requestQueue.add(channelsRequest)

    }

}