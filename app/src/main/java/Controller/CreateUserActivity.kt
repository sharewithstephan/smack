package Controller

import Services.AuthService
import Services.UserDataService
import Utilities.BROADCAST_USER_DATA_CHANGE
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smackthat.R
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar= "profileDefault"
    var avatarColour= "[0.5,0.5,0.5,1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        createSpinner.visibility=View.INVISIBLE
    }

  fun generateUserAvatar(view: View)
  {

      var random = Random()
      var colour= random.nextInt(2)
      var avatar= random.nextInt(28)

      if(colour==0)
      {
          userAvatar="light$avatar"

      }
      else{
          userAvatar="dark$avatar"
      }

      val resourseid= resources.getIdentifier(userAvatar,"drawable",packageName)
      createAvatarImageView.setImageResource(resourseid)

  }

  fun generateColourClicked(view: View)
  {

      val random=Random()
      val r= random.nextInt(255)
      val g= random.nextInt(255)
      val b= random.nextInt(255)

      createAvatarImageView.setBackgroundColor(Color.rgb(r,g,b))

      val savedR= r.toDouble()/255
      val savedG= g.toDouble()/255
      val savedB= b.toDouble()/255

      avatarColour="[$savedR,$savedG,$savedB]"



  }

  fun createUserClicked(view: View)
  {   enableSpinner(true)
      val email=createEmailtext.text.toString()
      val password= createPasswordText.text.toString()
      val userName= createUserNameTxt.text.toString()

     if(userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
         AuthService.registerUser(this, email, password) { registersuccess ->
             if (registersuccess) {
                 AuthService.loginUser(this, email, password) { loginsuccess ->
                     if (loginsuccess) {
                         AuthService.createUser(this, userName, email, userAvatar, avatarColour) { createSuccess ->
                             if (createSuccess) {

                                  val userDataChange= Intent(BROADCAST_USER_DATA_CHANGE)
                                  LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)

                                 enableSpinner(false)
                                 finish()
                             } else {
                                 errorToast()
                             }

                         }

                     } else {
                         errorToast()
                     }

                 }

             } else {
                 errorToast()
             }
         }
     }
      else{
         Toast.makeText(this,"Please make sure username, email & password is filled in",Toast.LENGTH_LONG).show()
         enableSpinner(false)
     }
  }

     fun errorToast(){
         Toast.makeText(this,"Something went wrong, please try again",Toast.LENGTH_LONG).show()
         enableSpinner(false)
     }

    fun enableSpinner(enable: Boolean)
    {
        if(enable){
            createSpinner.visibility=View.VISIBLE


        }
        else{
            createSpinner.visibility=View.INVISIBLE
        }
        createUserBtn.isEnabled= !enable
        createAvatarImageView.isEnabled= !enable
        backgroundColourBtn.isEnabled= !enable
    }


 }