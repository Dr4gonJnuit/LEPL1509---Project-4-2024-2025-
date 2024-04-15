package com.example.jobswype

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.security.MessageDigest

class MessagesFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Stock the user ID with it's number
    private val userIDandInt = HashMap<Int, String>()

    private lateinit var recipientUserId: String // TODO : should take the userID of the user to whom we want to send a message.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        // Access to FireStore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val navigationView = (requireActivity() as MainActivity).getNavigationView()
        loadContacts(navigationView, view, requireContext())

        // Initialize views
        val buttonSendMessage = view.findViewById<Button>(R.id.send_message_button)
        val messageEditText = view.findViewById<TextView>(R.id.write_message)

        buttonSendMessage.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(requireContext(), messageText)
            }
        }

        return view
    }

    private fun sendMessage(context: Context, messageText: String) {
        val currentUser = auth.currentUser
        val senderUserId = currentUser?.uid

        // Create a new message document in Firestore
        val message = hashMapOf(
            "sender" to senderUserId,
            "recipient" to recipientUserId,
            "text" to messageText,
            "timestamp" to System.currentTimeMillis()
        )

        // Add the message to Firestore
        firestore.collection("messages")
            .add(message)
            .addOnSuccessListener {
                // Message sent successfully
                //messageEditText.text.clear()
                Toast.makeText(
                    requireContext(),
                    "Message sent with success",
                    Toast.LENGTH_SHORT
                ).show()
                // Add UI update logic if needed
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Toast.makeText(
                    requireContext(),
                    "Fail to send the message : ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadContacts(navigationView: NavigationView, view: View, context: Context) {
        val currentUser = auth.currentUser
        val userID = currentUser?.uid

        firestore.collection("users").document(userID!!)
            .get()
            .addOnSuccessListener { result ->
                val userRole = result.getString("role")

                firestore.collection("matchmaking").whereEqualTo(userRole!!, userID)
                    .get()
                    .addOnSuccessListener { matchs ->
                        for (match in matchs) {
                            val otherID =
                                if (userRole == "JobSeeker") match.getString("Recruiter") else match.getString(
                                    "JobSeeker"
                                )

                            val matchMakingID = match.id

                            firestore.collection("users").document(otherID!!)
                                .get()
                                .addOnSuccessListener { otherUser ->
                                    val otherName = otherUser.getString("username")

                                    val itemID = generateIntIdFromStringId(otherID)
                                    userIDandInt[itemID] = otherID

                                    val menuItem = navigationView.menu.add(
                                        R.id.contacts_group, // group id
                                        itemID, // item id
                                        Menu.NONE, // order
                                        otherName!! // title
                                    )

                                    // Add a click listener to the menu item
                                    menuItem.setOnMenuItemClickListener { clickedMenuItem ->
                                        // Retrieve the user ID from the map
                                        val clickedUserId = userIDandInt[clickedMenuItem.itemId]
                                        loadUserAndMessages(context, userID, clickedUserId!!, view)
                                        // Display a Toast with the user ID
                                        //Toast.makeText(context, "User ID: $clickedUserId", Toast.LENGTH_SHORT).show()
                                        true // Indicate that the click event has been handled
                                    }
                                }
                        }
                    }
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }
    }

    private fun loadUserAndMessages(context: Context, userID: String, otherID: String, view: View) {
        firestore.collection("users").document(otherID)
            .get()
            .addOnSuccessListener { user ->
                recipientUserId = otherID
                val nameContact = view.findViewById<TextView>(R.id.name_user)
                nameContact.text = user.getString("username")

                // Load existing messages
                firestore.collection("messages")
                    .whereEqualTo("sender", userID)
                    .whereEqualTo("recipient", otherID)
                    //.orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { messages ->
                        val messageView = view.findViewById<TextView>(R.id.messages_view)

                        for (message in messages) {
                            val messageText = message.getString("text")

                            messageView.text = "${messageView.text}\n$messageText\n"
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            context,
                            "Fail to get your conversation : $exception",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Fail to get the user name: $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun generateIntIdFromStringId(stringId: String): Int {
        // Use SHA-256 hashing algorithm
        val bytes = MessageDigest.getInstance("SHA-256").digest(stringId.toByteArray())
        // Convert the hash bytes to an integer ID
        val id = bytes.fold(0) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xFF) }
        // Return the integer ID
        return id
    }

}
