package com.example.jobswype

import android.os.Bundle
import com.bumptech.glide.Glide
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var profileImg: ImageView
    private lateinit var profileUsername: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profilePhone: TextView
    private lateinit var profileAboutMe: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        profileImg = view.findViewById(R.id.profileImg)
        profileUsername = view.findViewById(R.id.profileUsername)
        profileEmail = view.findViewById(R.id.profileEmail)
        profilePhone = view.findViewById(R.id.profilePhone)
        profileAboutMe = view.findViewById(R.id.profileAboutMe)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Load user data
        loadUserData()

        return view
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val userRef = firestore.collection("users").document(userId!!)

        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                // Get user data
                val username = document.getString("username")
                val email = document.getString("email")
                val phone = document.getString("phone")
                val aboutMe = document.getString("aboutMe")
                val profileImageUrl = document.getString("profileImageUrl")

                // Set user data to views
                profileUsername.text = username
                profileEmail.text = email
                profilePhone.text = phone
                profileAboutMe.text = aboutMe

                profileImageUrl?.let {
                    Glide.with(requireContext())
                        .load(it)
                        .placeholder(R.drawable.default_pdp) // Image de remplacement pendant le chargement
                        .error(R.drawable.default_pdp) // Image de remplacement en cas d'erreur de chargement
                        .into(profileImg)
                }
            } else {
                // Document does not exist
            }
        }.addOnFailureListener { e ->
            Toast.makeText(
                requireContext(),
                "Error updating field(s): $e",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
