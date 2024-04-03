package com.example.roomdatabaseexample

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.room.Room
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var db: ContactRoomDatabase

    // Create instances of EditText as global variables so that other methods can access them
    // lateinit allows us to declare a variable first and then initialize it
    // some point in the future during our program's execution cycle.
    private lateinit var idText: EditText
    private lateinit var nameText : EditText
    private lateinit var emailText : EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the id, name and email EditText fields from the corresponding views in the layout
        idText = findViewById(R.id.text_id)
        nameText = findViewById(R.id.text_name)
        emailText = findViewById(R.id.text_email)

        // Build a db instance
        db = Room.databaseBuilder(
            applicationContext,
            ContactRoomDatabase::class.java, "contacts.db"
        ).build()

        // Insert a contact for testing purpose -- this version uses a coroutine
        // GlobalScope is used for coroutines that should continue executing even after
        // the activity/fragment on which they are running is destroyed
        /*
        GlobalScope.launch {
            val contact = ContactEntity(0, "John", "john@gmail.com")
            db.contactDAO().insertContact(contact)
        }*/



        // Insert a contact for testing purpose and query the database to show the record in the logcat
        // This version uses thread
        /*
            val contact = ContactEntity(0, "John", "john@gmail.com")
               Thread {
                   db.contactDAO().insertContact(contact)
               }.start()

               Thread {
                   val contacts = db.contactDAO().viewAllContacts()
                   for (contact in contacts){
                       Log.d(TAG, "${contact.id},${contact.name}, ${contact.email}")
                   }
               }.start()*/
    }



    /**
     * Inserts a new contact in the database
     */
    fun addButton(view: View) {

        // Insert a record (by using a Thread)

        val name = nameText.text.toString()
        val email = emailText.text.toString()

        if (name.isEmpty() || email.isEmpty()){
            showToast("Please enter name and email")
            return
        }

        Thread {
            // Insert methods treat 0 as not-set while inserting the item.
            // This allows autoincrement primary key to get autoincremented values
            val contact  = ContactEntity(0, name, email)
            Log.d(TAG, "Inserting a contact...")
            db.contactDAO().insertContact(contact)


            // We cannot call showDialog from a non-UI thread, instead we can call it from a runOnUiThread to access our views
            runOnUiThread {
                // Do your UI operations
                showToast("Successfully added a record")
                clearEditTexts()
            }

        }.start()


    }



    /*
        This is  an alternative version of the above function. This version uses coroutine instead of thread
        A coroutine is lightweight compared to a thread and should be chosen if the function does not
        take much time to run or consume too much resources
     */

/*    fun addButton(view: View) {
        // Insert a record (by using a Coroutine)
        val name = nameText.text.toString()
        val email = emailText.text.toString()

        if (name.isEmpty() || email.isEmpty()){
            showToast("Please enter name and email")
            return
        }

        // This is probably not the best approach as it uses Main thread
        CoroutineScope(Dispatchers.Main + CoroutineName("MyScope")).launch {

            val contact  = ContactEntity(0,name, email)
            Log.d(TAG, "Inserting a contact...")
            db.contactDAO().insertContact(contact)
            clearEditTexts()
            showToast("Successfully added a record")
        }
     }*/





    /**
     * Views all contacts in the database
     */

    fun viewAllDataButton(view: View) {

        Thread {
            // Read all the records
            val contacts = db.contactDAO().viewAllContacts()

            val buffer = StringBuffer()
            for (item in contacts){
                Log.d(TAG, "contact: ${item.id}, ${item.name}, ${item.email}")

                buffer.append("ID : ${item.id}" + "\n")
                buffer.append("NAME : ${item.name}" + "\n")
                buffer.append("EMAIL :  ${item.email}" + "\n\n")
            }


            // We cannot call showDialog from a non-UI thread, instead we can call it from a runOnUiThread to access our views
            runOnUiThread {
                // Do your UI operations
                showDialog("Data Listing", buffer.toString())
            }

        }.start()
    }


    /**
     * Deletes a contact in the database based on the given id
     */
    fun deleteButton(view: View) {

        // Delete a contact using id
        val id = idText.text.toString()
        if (id.isEmpty()){
            showToast("An ID must be entered!")
            return
        }

        Thread {
            // First find the contact and then delete
            val contact  = db.contactDAO().findContact(id.toInt())
            Log.d(TAG, "${contact.id}, ${contact.name}, ${contact.email}")

            // Delete the contact
            db.contactDAO().deleteContact(contact)

            // We cannot call showDialog from a non-UI thread, instead we can call it from a runOnUiThread to access our views
            runOnUiThread {
                // Do your UI operations
                showToast("Record has been deleted.")
                clearEditTexts()
            }

        }.start()
    }


    /**
     * Updates a contact in the database based on the given id
     */
    fun updateButton(view: View) {

        val id = idText.text.toString()
        if (id.isEmpty()){
            showToast("An ID must be entered!")
            return
        }

        Thread {
            // First find the contact and then update
            val contact  = db.contactDAO().findContact(id.toInt())
            Log.d(TAG, "${contact.id}, ${contact.name}, ${contact.email}")

            // Update the contact
            contact.name = nameText.text.toString()
            contact.email = emailText.text.toString()

            db.contactDAO().updateContact(contact)

            // We cannot call showDialog from a non-UI thread, instead we can call it from a runOnUiThread to access our views
            runOnUiThread {
                // Do your UI operations
                showToast("Record Updated Successfully")
                clearEditTexts()
            }

        }.start()

    }


    /**
     * A helper function to show Toast message
     */
    private fun showToast(text: String){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    /**
     * show an alert dialog with data dialog.
     */
    private fun showDialog(title: String, Message : String){
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(title)
        builder.setMessage(Message)
        builder.show()
    }


    /**
     * A helper function to clear our editTexts
     */
    private fun clearEditTexts() {
        val editTextFields = listOf(idText, nameText, emailText)
        for (editTextField in editTextFields) {
            editTextField.text.clear()
        }
    }
}