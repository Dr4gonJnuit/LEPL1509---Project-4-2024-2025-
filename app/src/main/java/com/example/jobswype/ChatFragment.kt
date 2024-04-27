package com.example.jobswype

import ChatRecyclerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobswype.model.ChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ChatFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: FirebaseDatabase

    private lateinit var sharedViewModel: SharedViewModel


    private lateinit var recipientUserId: String
    private lateinit var matchmakingID: String

    private val messageList = mutableListOf<ChatModel>()
    private lateinit var adapter: ChatRecyclerAdapter
    private lateinit var recyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Cacher la Toolbar lorsqu'on arrive sur le ChatFragment
        val mainToolbar: Toolbar = requireActivity().findViewById(R.id.toolbar)
        mainToolbar.visibility = View.GONE

        // back button
        val backButton = view.findViewById<ImageView>(R.id.backArrow)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val adapter = ChatRecyclerAdapter(requireContext(), listOf())
        recyclerView = view.findViewById(R.id.chat_recycler_view)
        recyclerView.adapter = adapter
        val tempLayoutManager = LinearLayoutManager(requireContext())
        tempLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = tempLayoutManager

        // Access to FireStore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance()

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        recipientUserId = sharedViewModel.selectedContact
        loadContactData(view, recipientUserId)
        // Initialize views
        val buttonSendMessage = view.findViewById<ImageView>(R.id.send_button)
        val messageEditText = view.findViewById<TextView>(R.id.message_edit_text)

        buttonSendMessage.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageEditText.text = ""
            } else {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Réafficher la Toolbar lorsqu'on quitte le ChatFragment
        val mainToolbar: Toolbar =
            requireActivity().findViewById(R.id.toolbar)
        mainToolbar.visibility = View.VISIBLE
    }

    private fun loadContactData(view: View?, recipientUserId: String) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val userRef = firestore.collection("users").document(userId!!)

        userRef.get().addOnSuccessListener { user ->
            if (user != null) {
                // Get user data
                val userRole = user.getString("role")
                val otherRole = if (userRole == "JobSeeker") "Recruiter" else "JobSeeker"

                userRole?.let {
                    firestore.collection("matchmaking").whereEqualTo(it, userId).get()
                        .addOnSuccessListener { matchs ->
                            for (match in matchs) {
                                val otherID = match.getString(otherRole)
                                if (otherID != null) {
                                    if (otherID == recipientUserId) {
                                        matchmakingID = match.id
                                        val otherUserRef = firestore.collection("users").document(otherID)
                                        otherUserRef.get().addOnSuccessListener { otherUser ->
                                            val nameContact = view?.findViewById<TextView>(R.id.username)
                                            nameContact?.text = otherUser.getString("username")
                                            if (nameContact?.text == "none") {
                                                nameContact.text =
                                                    otherUser.getString("email")?.substring(
                                                        0,
                                                        otherUser.getString("email")!!.indexOf("@")
                                                    )
                                            }
                                            val profilepicContact = view?.findViewById<ImageView>(R.id.profilePic)
                                            val profileImageUrl = otherUser.getString("profilePic")
                                            if (profileImageUrl == "none"){
                                                profilepicContact?.setImageResource(R.drawable.default_pdp)
                                            } else {
                                                profileImageUrl?.let {
                                                    if (profilepicContact != null) {
                                                        context?.let { it1 ->
                                                            Glide.with(it1)
                                                                .load(it)
                                                                .apply(
                                                                    RequestOptions.bitmapTransform(
                                                                        CircleCrop()
                                                                    )
                                                                ) // Apply a transform circle
                                                                .placeholder(R.drawable.default_pdp) // Placeholder image while loading
                                                                .error(R.drawable.default_pdp) // Image to show if loading fails
                                                                .into(profilepicContact)
                                                        }
                                                    }
                                                }
                                            }
                                            // Configure RecyclerView layout manager
                                            val linearLayoutManager = LinearLayoutManager(requireContext())
                                            linearLayoutManager.stackFromEnd = true
                                            recyclerView.layoutManager = linearLayoutManager

                                            val reference = database.getReference("messages")
                                                .child(matchmakingID)
                                            reference.addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    messageList.clear()
                                                    for (data in snapshot.children) {
                                                        val message = data.getValue(ChatModel::class.java)
                                                        if (message != null) {
                                                            message.currentUserID = userId
                                                            messageList.add(message)
                                                        }
                                                    }
                                                    adapter = ChatRecyclerAdapter(requireContext(), messageList)
                                                    adapter.notifyItemInserted(messageList.size - 1)
                                                    recyclerView.adapter = adapter
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Fail to get the messages",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            })
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "No match find", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                }
            }
        }
    }

    private fun sendMessage(messageText: String) {
        val currentUser = auth.currentUser
        val senderUserId = currentUser?.uid
        val reference = database.getReference("messages").child(matchmakingID)
        val currentDate: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val currentTime: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val map = hashMapOf(
            "sender" to senderUserId,
            "recipient" to recipientUserId,
            "message" to messageText,
            "currentDate" to currentDate,
            "currentTime" to currentTime,
        )

        // Add the message to Firestore
        reference.child(reference.push().key!!)
            .setValue(map).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Message sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    sendNotification(messageText)

                } else {
                    Toast.makeText(requireContext(), "Fail to send the message", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun sendNotification(messageText: String) {
        val currentUser = auth.currentUser
        val senderUserId = currentUser?.uid
        val userRef = firestore.collection("users").document(senderUserId!!)
        userRef.get().addOnSuccessListener { user ->
            if (user != null) {
                val userRole = user.getString("role")
                val otherRole = if (userRole == "JobSeeker") "Recruiter" else "JobSeeker"
                firestore.collection("matchmaking").document(matchmakingID).get()
                    .addOnSuccessListener { match ->
                        val otherID = match.getString(otherRole)
                        val otherUserRef = firestore.collection("users").document(otherID!!)
                        otherUserRef.get().addOnSuccessListener { otherUser ->
                            val otherToken = otherUser.getString("fcmToken")
                            val senderName = user.getString("username")
                            try {
                                val jsonObject = JSONObject()
                                val notificationObject = JSONObject()
                                notificationObject.put("title", senderName)
                                notificationObject.put("body", messageText)
                                jsonObject.put("notification", notificationObject)
                                jsonObject.put("to", otherToken)
                                callApi(jsonObject)
                            } catch (e: Exception) {
                                Log.e("Error", e.toString())
                            }
                        }
                    }
            }
        }
    }

    fun callApi(jsonObject: JSONObject){
        val JSON: MediaType = "application/json".toMediaType()
        val client = OkHttpClient()

        val url = "https://fcm.googleapis.com/fcm/send"
        val body = jsonObject.toString().toRequestBody(JSON)
        val request = okhttp3.Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer AAAA_Tn40MY:APA91bFcMiUxV8S6FYE8J_cX3Vhn_ZQsbRaAJQcJPRsbOUW4zP1vWAQpZ4QvbEBI9Nwk1IImNnR1K1H8Sza2SpNW5GR1Ugk70s6OvX0QgewVlchOXdfAMUWIEVL5_tqFK7p4G-2ydLNS")
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Log.e("Response Code", response.code.toString())
                try {
                    val responseBody = response.body?.string()
                    Log.e("Response Body", responseBody ?: "Empty response body")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        })
    }
}