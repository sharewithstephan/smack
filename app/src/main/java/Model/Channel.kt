//Receivind data send through sockets


package Model

class Channel (val name: String, val description: String, val id: String){

    override fun toString(): String {    // displaying using a simple listview
        //return super.toString()
      return "#$name"
    }
}