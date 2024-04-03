package com.example.jobswype

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
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
                val targetRole = if (role.equals("Recrutor", ignoreCase = true)) "JobSeeker" else "Recrutor"
                val field = if (role.equals("Recrutor", ignoreCase = true)) "cv" else "job_offer"

                firestore.collection("users").whereEqualTo("role", targetRole).get().addOnSuccessListener { querySnapshot ->
                    val urls = querySnapshot.documents.mapNotNull { it.getString(field) }.toList()
                    if (urls.isNotEmpty()) {
                        callback(urls)
                    } else {
                        // Handling case where no documents were found for the role
                        Toast.makeText(context, "No documents found for role $targetRole", Toast.LENGTH_SHORT).show()
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
