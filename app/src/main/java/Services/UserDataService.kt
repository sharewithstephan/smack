package Services

import Controller.App
import android.graphics.Color
import java.util.*

object UserDataService {

    var id= ""
    var avatarColor= ""
    var avatarName= ""
    var email= ""
    var name= ""


//[0.25098039215686274,0.6745098039215687,0.9333333333333333]  color format saved in mongodb
//0.25098039215686274 0.6745098039215687 0.9333333333333333
    fun returnAvatarColor(compnents: String): Int{
    val strippedColor= compnents
        .replace("[","")
        .replace("]","")
        .replace(","," ")

    var r=0
    var g=0
    var b=0

    val scanner= Scanner(strippedColor)   // scanner scans a string
    if(scanner.hasNext()){
        r= (scanner.nextDouble() * 255).toInt()    // in android colours are saved as intigers btwn 0 and 255
        g= (scanner.nextDouble() * 255).toInt()
        b= (scanner.nextDouble() * 255).toInt()

    }

    return Color.rgb(r,g,b)
}



    fun logout(){

        id=""
        avatarColor=""
        avatarName=""
        email=""
        name=""
        App.prefs.authToken=""
        App.prefs.userEmail=""
        App.prefs
            .isLoggedIn=false
        MessageService.clearMessages()
        MessageService.clearChannels()


    }



}