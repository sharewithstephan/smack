package Controller

import Model.Channel
import Model.Message
import Services.AuthService
import Services.MessageService
import Services.UserDataService
import Utilities.BROADCAST_USER_DATA_CHANGE
import Utilities.SOCKET_URL
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smackthat.R
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket= IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    var selectedChannel: Channel?= null

    private fun setupAdapters(){
        channelAdapter= ArrayAdapter(this, android.R.layout.simple_list_item_1,MessageService.channels)
        channel_list.adapter= channelAdapter
    }


    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        socket.connect()
        socket.on("channelCreated",onNewChannel)
        socket.on("messageCreated", onNewMessage )

      //  val fab: FloatingActionButton = findViewById(R.id.fab)

        //hideKeyboard()

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home,
            R.id.nav_gallery,
            R.id.nav_slideshow
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setupAdapters()
        channel_list.setOnItemClickListener { _, _, i, _->
            selectedChannel= MessageService.channels[i]
            drawerLayout.closeDrawer(GravityCompat.START)
            updateWithChannel()


        }

        if (App.prefs.isLoggedIn){        // for getting the loged in user on restart of app
            AuthService.findUserByEmail(this){}
        }






    }


    override fun onResume() {


        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))
//        socket.connect()
//        socket.on("channelCreated",onNewChannel)
        super.onResume()

    }

//    override fun onPause() {
//
//
//
//        super.onPause()
//    }

    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        super.onDestroy()

    }

    private val userDataChangeReceiver= object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent?) {

            if(App.prefs.isLoggedIn){
                userNameNavHeader.text=UserDataService.name
                userEmailNavHeader.text=UserDataService.email
                val resourceId= resources.getIdentifier(UserDataService.avatarName,"drawable",packageName)
                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))

                loginBtnNavHeader.text="Logout"

                MessageService.getChannels(){complete->
                    if(complete){
                        if(MessageService.channels.count()>0)
                        {   // for showing the chats of first channel when app is started
                            selectedChannel= MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }
                        //reload the list view

                    }

                }






            }
        }
    }
    fun updateWithChannel(){
        // called when a channel is selected
        mainChannelName.text= "#${selectedChannel?.name}"
        // download messages for channel
    }



//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    fun loginBtnNavClicked(view:View)
    {
        if(App.prefs.isLoggedIn)
        {
            //logout

            UserDataService.logout()
           // userNameNavHeader.text= "Login"
            userEmailNavHeader.text= ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginBtnNavHeader.text= "Login"
        }
        else
        {

        val loginIntent= Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        }

    }

    fun addChannelBtnClicked(view: View)
    {
        if(App.prefs.isLoggedIn){
            val builder= AlertDialog.Builder(this)        // for an alert dialog
            val dialogView= layoutInflater.inflate(R.layout.add_channel_dialog,null)

            builder.setView(dialogView)
                .setPositiveButton("Add"){ _, _ ->
                    // perform some logic when clicked

                    val nameTextField= dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                    val descTextField= dialogView.findViewById<EditText>(R.id.addChannelDescTxt)

                    val channelName= nameTextField.text.toString()
                    val channelDesc= descTextField.text.toString()

                    //Create channel with channel name and description
                    socket.emit("newChannel",channelName,channelDesc)
                    //hideKeyboard()
                }
                .setNegativeButton("Cancel"){ _, _ ->
                    //Cancel and close the dialog
                   // hideKeyboard()
                }
                .show()

        }


    }

    private val onNewChannel= Emitter.Listener { args ->

        runOnUiThread { // switching to UI thread from worker thread
            val channelName= args[0] as String
            val channelDescription= args[1] as String
            val channelId= args[2] as String

            val newChannel= Channel(channelName,channelDescription,channelId)
            MessageService.channels.add(newChannel)
//            println(newChannel.name)
//            println(newChannel.description)
//            println(newChannel.id)

             channelAdapter.notifyDataSetChanged()

        }
    }

    private val onNewMessage= Emitter.Listener { args ->

        runOnUiThread {
            val msgBody= args[0] as String
            val channelId= args[2] as String
            val userName= args[3] as String
            val userAvatar= args[4] as String
            val userAvatarColor= args[5] as String
            val id= args[6] as String
            val timeStamp= args[7] as String

            val newMessage= Message(msgBody,userName,channelId,userAvatar,userAvatarColor,id,timeStamp)
            MessageService.messages.add(newMessage)
            println(newMessage.message)



        }
    }





    fun sendMsgBtnClicked(view: View)
    {
        if(App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel!= null)
        {
            val userId= UserDataService.id
            val channelId= selectedChannel!!.id

            socket.emit("newMessage", messageTextField.text.toString() ,userId, channelId, UserDataService.name,
            UserDataService.avatarName, UserDataService.avatarColor)
            messageTextField.text.clear()
            hideKeyboard()

        }



    }

    fun hideKeyboard(){         //To hide keyboard when not needed

        val inputManager= getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken,0)
        }
    }

}