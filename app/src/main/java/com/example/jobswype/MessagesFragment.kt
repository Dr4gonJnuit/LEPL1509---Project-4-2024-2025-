package com.example.jobswype

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessagesFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var recipientUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        /*
        val buttonSendMessage = view.findViewById<Button>(R.id.send_message_button)
        val messageEditText = view.findViewById<TextView>(R.id.write_message)

        buttonSendMessage.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(requireContext(), messageText)
            }
        }
        */
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
                // Add UI update logic if needed
            }
            .addOnFailureListener { exception ->
                // Handle errors
                // Add error handling logic if needed
            }
    }
}