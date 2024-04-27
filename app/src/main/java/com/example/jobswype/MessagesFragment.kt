package com.example.jobswype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessagesFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

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
                                    val count = matchs.indexOf(match) + 1
                                    val otherUserRef = firestore.collection("users").document(otherID)
                                    otherUserRef.get().addOnSuccessListener { otherUser ->
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


                                        val user_item_image = resources.getIdentifier("user_item_image_$count", "id", requireContext().packageName)
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
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "No match find", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "No match find :$exception", Toast.LENGTH_SHORT)
                                .show()
                        }

                }
            }
        }

    return view
    }
}
class SharedViewModel : ViewModel() {
    var selectedContact : String = "none"
}