package com.example.jobswype

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val context = LocalContext.current
                //Start fetching images
                val imageUrls = remember { mutableStateListOf<String>() }
                LaunchedEffect(key1 = true) {
                    fetchImageUrls(context){ urls ->
                        imageUrls.clear()
                        imageUrls.addAll(urls)
                    }
                }
                MyAppContent(context, imageUrls)

            }
        }
    }

    private fun fetchImageUrls(context: Context, callback: (List<String>) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
                val role = document.getString("role") ?: ""
                val targetRole = if (role.equals("Recruiter", ignoreCase = true)) "JobSeeker" else "Recruiter"
                val field = if (role.equals("Recruiter", ignoreCase = true)) "cv" else "job_offer"

                firestore.collection("users").whereEqualTo("role", targetRole).get().addOnSuccessListener { querySnapshot ->
                    val userId = currentUser.uid
                    val likedMap = document.get("liked") as? HashMap<String, Boolean> ?: hashMapOf()
                    val likedImages = likedMap.filterValues { it == true }.keys // Get the IDs of liked images

                    val urls = querySnapshot.documents.mapNotNull { document ->
                        val imageUrl = document.getString(field)
                        val imageId = document.id

                        // Check if the image has already been liked or disliked
                        if (!likedImages.contains(imageId)) {
                            imageUrl
                        } else {
                            null // Exclude the image URL if it has been liked or disliked
                        }
                    }

                    if (urls.isNotEmpty()) {
                        callback(urls)
                    } else {
                        // Handling case where no documents were found for the role
                        Toast.makeText(context, "No more documents found for $role", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    // Handling case where fetching documents fails
                    Toast.makeText(context, "Failed to fetch data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                // Handling case where fetching the current user document fails
                Toast.makeText(context, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

}
