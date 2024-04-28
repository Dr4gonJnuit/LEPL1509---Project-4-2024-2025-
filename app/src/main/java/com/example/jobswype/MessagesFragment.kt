package com.example.jobswype

import MessageUserAdapter
import OnContactItemClickListener
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobswype.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessagesFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val contactList = mutableListOf<UserModel>()
    private lateinit var adapter: MessageUserAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        adapter = MessageUserAdapter(requireContext(), listOf(), object : OnContactItemClickListener {
            override fun onContactItemClicked(position: Int) {
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragment_container, ChatFragment())?.addToBackStack(null)?.commit()
            }
        })
        recyclerView = view.findViewById(R.id.messages_recycler_view)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        contactList.clear()

        val context = requireActivity()
        getContatcs(context)

        return view
    }

    private fun getContatcs(context: Context?){
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
                                    // Accéder aux informations de l'utilisateur correspondant
                                    val otherUserRef = firestore.collection("users").document(otherID)
                                    otherUserRef.get().addOnSuccessListener { otherUser ->

                                        // Configure RecyclerView layout manager
                                        recyclerView.layoutManager = LinearLayoutManager(context)

                                        // Accéder aux données de l'utilisateur correspondant dans Firestore et afficher les matchs
                                        val userModel = otherUser.toObject(UserModel::class.java)
                                        userModel?.let {
                                            contactList.add(it)

                                            adapter = context?.let { it1 ->
                                                MessageUserAdapter(it1, contactList, object : OnContactItemClickListener {
                                                    override fun onContactItemClicked(position: Int) {
                                                        // Récupérer le contact correspondant à l'index cliqué
                                                        val clickedContact = contactList[position]

                                                        // Passer le contact sélectionné à votre ViewModel
                                                        sharedViewModel = ViewModelProvider(
                                                            requireActivity()
                                                        )[SharedViewModel::class.java]
                                                        sharedViewModel.selectedContact = clickedContact.userId.toString()

                                                        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragment_container, ChatFragment())?.addToBackStack(null)?.commit()
                                                    }
                                                })
                                            }!!
                                            recyclerView.adapter = adapter
                                        }
                                    }.addOnFailureListener { exception ->
                                        Toast.makeText(
                                            context,
                                            "Failed to get other user data: $exception",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(context, "No match found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "No match found: $exception", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to get current user data: $exception", Toast.LENGTH_SHORT).show()
        }

    }
}
class SharedViewModel : ViewModel() {
    var selectedContact : String = "none"
}

/*
val layout = resources.getIdentifier("msg_layout_$count", "id", requireContext().packageName)
val msgLayout = view?.findViewById<View>(layout)
msgLayout?.visibility = View.VISIBLE
msgLayout?.setOnClickListener {
   sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
   sharedViewModel.selectedContact = otherID
   getActivity()?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragment_container, ChatFragment())?.addToBackStack(null)?.commit()
}
val user_item_username = resources.getIdentifier("user_item_username_$count", "id", requireContext().packageName)
val itemTitle = view?.findViewById<TextView>(user_item_username)
itemTitle?.text = otherUser.getString("username")
if (otherUser.getString("username") == "none"){
   itemTitle?.text = otherUser.getString("email")?.substring(0, otherUser.getString("email")!!.indexOf("@"))
}
val user_item_image = resources.getIdentifier("user_item_image", "id", requireContext().packageName)
val itemImage = view?.findViewById<ImageView>(user_item_image)
val profileImageUrl = otherUser.getString("profilePic")
if (profileImageUrl == "none") {
   itemImage?.setImageResource(R.drawable.default_pdp)
} else {

   // Load profile image using Glide
   profileImageUrl?.let {
       if (itemImage != null) {
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
                   .into(itemImage)
           }
       }
   }
}*/